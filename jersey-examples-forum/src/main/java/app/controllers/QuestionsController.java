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

import com.google.common.base.Optional;

import app.core.*;
import app.models.*;

import static app.core.Util.params;

@Path("/questions")
@Produces(MediaType.TEXT_HTML)
public class QuestionsController {
    private final Validator validator;
    private final QuestionDao questionDao;
    private final AnswerDao answerDao;
    private final AccountDao accountDao;

    public QuestionsController(
            QuestionDao questionDao,
            AnswerDao answerDao,
            AccountDao accountDao) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.questionDao = questionDao;
        this.answerDao = answerDao;
        this.accountDao = accountDao;
    }

    @GET
    public Response index(
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit) {
        PaginationHelper<QuestionsResult> pagination = new PaginationHelper<QuestionsResult>(
                questionDao.select(offset, limit),
                "questions?offset=%d&limit=%d");
        return Response.ok(new View("questions/index", params(
                "pagination", pagination))).build();
    }

    @GET
    @Path("{id}")
    public Response detail(
            @Context UriInfo uinfo,
            @PathParam("id") Long id,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("5") int limit) {

        // question
        Optional<Question> questionOpt = questionDao.find(id);
        if (!questionOpt.isPresent())
            return redirectToIndex(uinfo);
        Question question = questionOpt.get();

        // account
        Optional<Account> accountOpt = accountDao.find(question.getAuthorId());
        if (!accountOpt.isPresent())
            return redirectToIndex(uinfo);
        Account author = accountOpt.get();

        // answers
        PaginationHelper<AnswersResult> answers = new PaginationHelper<AnswersResult>(
                answerDao.selectByQuestionId(id, offset, limit),
                id + "?offset=%d&limit=%d");

        View view = new View("questions/detail", params(
                "question", question,
                "author", author,
                "answers", answers));
        return Response.ok(view).build();
    }

    @GET
    @Path("edit")
    public Response edit(
            @Context User user,
            @Context UriInfo uinfo,
            @QueryParam("id") @DefaultValue("") Long id) {
        if (!user.getAccount().isPresent()) {
            return redirectToLogin(uinfo, id);
        }

        Question question = null;
        QuestionEditForm form;
        if (id != null) {
            Optional<Question> questionOpt = questionDao.find(id);
            if (!questionOpt.isPresent())
                return redirectToIndex(uinfo);
            question = questionOpt.get();
            if (!isQuestionAuthor(user, question))
                return redirectToIndex(uinfo);
            form = QuestionEditForm.bindFrom(question);
        } else {
            form = QuestionEditForm.defaultForm();
        }

        View view = new View("questions/edit", params(
                "form", new FormHelper<QuestionEditForm>(form),
                "question", question));
        return Response.ok(view).build();
    }

    @POST
    @Path("edit")
    @Consumes("application/x-www-form-urlencoded")
    public Response postEdit(
            @Context User user,
            @Context UriInfo uinfo,
            @FormParam("id") Long id,
            MultivaluedMap<String, String> formParams) {
        if (!user.getAccount().isPresent()) {
            return redirectToLogin(uinfo, id);
        }
        Account account = user.getAccount().get();

        Question question = null;
        QuestionEditForm form = QuestionEditForm.bindFrom(formParams);

        if (id != null) {
            Optional<Question> questionOpt = questionDao.find(id);
            if (!questionOpt.isPresent())
                return redirectToIndex(uinfo);
            question = questionOpt.get();
            if (!isQuestionAuthor(user, question))
                return redirectToIndex(uinfo);
        }

        Set<ConstraintViolation<QuestionEditForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("questions/edit", params(
                    "form", new FormHelper<QuestionEditForm>(form, errors),
                    "question", question));
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(view).build();
        }
        if (id == null) {
            question = new Question();
            question.setAuthorId(account.getId());
            question.setPostedAt(new java.util.Date());
        }
        question.setSubject(form.getSubject());
        question.setBody(form.getBody());
        questionDao.save(question);

        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/questions").build()).build();
    }

    private Response redirectToIndex(UriInfo uinfo) {
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/questions")
                .build()).build();
    }

    private Response redirectToLogin(UriInfo uinfo, Long id) {
        String url = "/questions/edit";
        if (id != null)
            url += "?id=" + id;
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/accounts/signin")
                .queryParam("url", url)
                .build()).build();
    }

    private boolean isQuestionAuthor(User user, Question question) {
        if (!user.getAccount().isPresent())
            return false;
        Account account = user.getAccount().get();
        if (question.getAuthorId() == 0)
            return false;
        if (question.getAuthorId() != account.getId())
            return false;
        return true;
    }
}
