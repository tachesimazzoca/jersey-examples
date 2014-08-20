package app.controllers;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import java.util.Set;
import java.util.Map;

import com.google.common.base.Optional;

import app.core.FormHelper;
import app.core.PaginationHelper;
import app.core.View;
import app.models.Account;
import app.models.AccountDao;
import app.models.AccountQuestionDao;
import app.models.AnswersResult;
import app.models.AnswerDao;
import app.models.UserContext;
import app.models.Question;
import app.models.QuestionsResult;
import app.models.QuestionDao;
import app.models.QuestionEditForm;

import static app.core.Util.params;
import static app.core.Util.safeURI;

@Path("/questions")
@Produces(MediaType.TEXT_HTML)
public class QuestionsController {
    private final Validator validator;
    private final QuestionDao questionDao;
    private final AnswerDao answerDao;
    private final AccountDao accountDao;
    private final AccountQuestionDao accountQuestionDao;

    public QuestionsController(
            QuestionDao questionDao,
            AnswerDao answerDao,
            AccountDao accountDao,
            AccountQuestionDao accountQuestionDao) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.questionDao = questionDao;
        this.answerDao = answerDao;
        this.accountDao = accountDao;
        this.accountQuestionDao = accountQuestionDao;
    }

    @GET
    public Response index(
            @Context UserContext userContext,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit) {
        PaginationHelper<QuestionsResult> questions = new PaginationHelper<QuestionsResult>(
                questionDao.selectPublicQuestions(offset, limit),
                "questions?offset=%d&limit=%d");
        return Response.ok(new View("questions/index", params(
                "account", userContext.getAccount().orNull(),
                "questions", questions))).build();
    }

    @GET
    @Path("{id}")
    public Response detail(
            @Context UserContext userContext,
            @Context UriInfo uinfo,
            @PathParam("id") Long id,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("5") int limit) {

        // account
        Optional<Account> accountOpt = userContext.getAccount();
        Account account = accountOpt.orNull();

        // question
        Optional<Question> questionOpt = questionDao.find(id);
        if (!questionOpt.isPresent())
            return redirectToIndex(uinfo);
        Question question = questionOpt.get();
        if (question.getStatus() != Question.Status.PUBLISHED)
            return redirectToIndex(uinfo);

        // author
        Optional<Account> authorOpt = accountDao.find(question.getAuthorId());
        if (!authorOpt.isPresent())
            return redirectToIndex(uinfo);
        Account author = authorOpt.get();

        // questionInfo
        int numPoints = accountQuestionDao.sumPositivePoints(question.getId());
        boolean starred = false;
        if (account != null) {
            starred = accountQuestionDao.getPoint(account.getId(), question.getId()) > 0;
        }
        Map<String, Object> questionInfo = params(
                "numPoints", numPoints,
                "starred", starred);

        // answers
        PaginationHelper<AnswersResult> answers = new PaginationHelper<AnswersResult>(
                answerDao.selectByQuestionId(id, offset, limit),
                id + "?offset=%d&limit=%d");

        View view = new View("questions/detail", params(
                "account", account,
                "questionInfo", questionInfo,
                "question", question,
                "author", author,
                "answers", answers));
        return Response.ok(view).build();
    }

    private Response vote(UserContext userContext, UriInfo uinfo, Long id, int point) {
        Optional<Question> questionOpt = questionDao.find(id);
        if (!questionOpt.isPresent())
            return redirectToIndex(uinfo);
        Question question = questionOpt.get();
        if (question.getStatus() != Question.Status.PUBLISHED)
            return redirectToIndex(uinfo);

        Optional<Account> accountOpt = userContext.getAccount();
        if (!accountOpt.isPresent())
            return redirect(uinfo, "/questions/" + id);
        Account account = accountOpt.get();

        accountQuestionDao.log(account.getId(), question.getId(), point);

        return redirect(uinfo, "/questions/" + id);
    }

    @GET
    @Path("star")
    public Response star(
            @Context UserContext userContext,
            @Context UriInfo uinfo,
            @QueryParam("id") Long id) {
        return vote(userContext, uinfo, id, 1);
    }

    @GET
    @Path("unstar")
    public Response unstar(
            @Context UserContext userContext,
            @Context UriInfo uinfo,
            @QueryParam("id") Long id) {

        return vote(userContext, uinfo, id, 0);
    }

    @GET
    @Path("edit")
    public Response edit(
            @Context UserContext userContext,
            @Context UriInfo uinfo,
            @QueryParam("id") @DefaultValue("") Long id) {

        Optional<Account> accountOpt = userContext.getAccount();
        if (!accountOpt.isPresent())
            return redirectToLogin(uinfo, id);
        Account account = accountOpt.get();

        Question question = null;
        QuestionEditForm form;
        if (id != null) {
            Optional<Question> questionOpt = questionDao.find(id);
            if (!questionOpt.isPresent())
                return redirectToIndex(uinfo);
            question = questionOpt.get();
            if (!question.isSameAuthor(account))
                return redirectToIndex(uinfo);
            form = QuestionEditForm.bindFrom(question);
        } else {
            form = QuestionEditForm.defaultForm();
        }

        String flash = userContext.getFlash().orNull();
        View view = new View("questions/edit", params(
                "account", account,
                "form", new FormHelper<QuestionEditForm>(form),
                "question", question,
                "flash", flash));
        return Response.ok(view).cookie(userContext.toCookie()).build();
    }

    @POST
    @Path("edit")
    @Consumes("application/x-www-form-urlencoded")
    public Response postEdit(
            @Context UserContext userContext,
            @Context UriInfo uinfo,
            @FormParam("id") Long id,
            MultivaluedMap<String, String> formParams) {

        Optional<Account> accountOpt = userContext.getAccount();
        if (!accountOpt.isPresent())
            return redirectToLogin(uinfo, id);
        Account account = accountOpt.get();

        Question question = null;
        QuestionEditForm form = QuestionEditForm.bindFrom(formParams);

        if (id != null) {
            Optional<Question> questionOpt = questionDao.find(id);
            if (!questionOpt.isPresent())
                return redirectToIndex(uinfo);
            question = questionOpt.get();
            if (!question.isSameAuthor(account))
                return redirectToIndex(uinfo);
        }

        Set<ConstraintViolation<QuestionEditForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("questions/edit", params(
                    "account", account,
                    "form", new FormHelper<QuestionEditForm>(form, errors),
                    "question", question));
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(view).build();
        }

        if (question == null) {
            question = new Question();
            question.setAuthorId(account.getId());
            question.setPostedAt(new java.util.Date());
            userContext.setFlash("created");
        } else {
            userContext.setFlash("updated");
        }
        question.setSubject(form.getSubject());
        question.setBody(form.getBody());
        question.setStatus(Question.Status.fromValue(form.getStatus()));
        questionDao.save(question);

        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/questions/edit")
                .queryParam("id", question.getId())
                .build()).cookie(userContext.toCookie()).build();
    }

    @GET
    @Path("delete")
    public Response delete(
            @Context UserContext userContext,
            @Context UriInfo uinfo,
            @QueryParam("id") @DefaultValue("") Long id) {

        Optional<Account> accountOpt = userContext.getAccount();
        if (!accountOpt.isPresent())
            return redirectToLogin(uinfo, id);
        Account account = accountOpt.get();

        if (id == null)
            return redirectToDashboard(uinfo);

        Optional<Question> questionOpt = questionDao.find(id);
        if (!questionOpt.isPresent())
            return redirectToDashboard(uinfo);
        Question question = questionOpt.get();
        if (!question.isSameAuthor(account))
            return redirectToIndex(uinfo);

        questionDao.updateStatus(id, Question.Status.DELETED);
        return redirectToDashboard(uinfo);
    }

    private Response redirect(UriInfo uinfo, String path) {
        return Response.seeOther(safeURI(uinfo, path)).build();
    }

    private Response redirectToIndex(UriInfo uinfo) {
        return redirect(uinfo, "/questions");
    }

    private Response redirectToDashboard(UriInfo uinfo) {
        return redirect(uinfo, "/dashboard/questions");
    }

    private Response redirectToLogin(UriInfo uinfo, String returnTo) {
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/accounts/signin")
                .queryParam("returnTo", returnTo)
                .build()).build();
    }

    private Response redirectToLogin(UriInfo uinfo, Long id) {
        String url = "/questions/edit";
        if (id != null)
            url += "?id=" + id;
        return redirectToLogin(uinfo, url);
    }
}
