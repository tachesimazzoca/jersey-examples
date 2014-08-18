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
import static app.core.Util.safeURI;

@Path("/questions")
@Produces(MediaType.TEXT_HTML)
public class QuestionsController {
    private final Validator validator;
    private final AccountDao accountDao;
    private final QuestionDao questionDao;
    private final AnswerDao answerDao;

    public QuestionsController(
            AccountDao accountDao,
            QuestionDao questionDao,
            AnswerDao answerDao) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.accountDao = accountDao;
        this.questionDao = questionDao;
        this.answerDao = answerDao;
    }

    @GET
    public Response index(
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit) {
        PaginationHelper<QuestionsResult> pagination = new PaginationHelper<QuestionsResult>(
                questionDao.selectPublicQuestions(offset, limit),
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
        if (question.getStatus() != Question.Status.PUBLISHED)
            return redirectToIndex(uinfo);

        // author
        Optional<Account> authorOpt = accountDao.find(question.getAuthorId());
        if (!authorOpt.isPresent())
            return redirectToIndex(uinfo);
        Account author = authorOpt.get();

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
    @Path("cancel")
    public Response cancel(
            @Context Session session,
            @Context UriInfo uinfo,
            @QueryParam("id") @DefaultValue("") Long id) {

        Optional<String> returnTo = session.remove("returnTo");
        if (returnTo.isPresent()) {
            return redirect(uinfo, returnTo.get());
        }

        Question question = null;
        if (id != null) {
            Optional<Question> questionOpt = questionDao.find(id);
            if (questionOpt.isPresent())
                question = questionOpt.get();
        }

        if (question != null && question.getStatus() == Question.Status.PUBLISHED) {
            return redirect(uinfo, "/questions/" + question.getId());
        } else {
            return redirectToDashboard(uinfo);
        }
    }

    @GET
    @Path("edit")
    public Response edit(
            @Context Session session,
            @Context UriInfo uinfo,
            @QueryParam("id") @DefaultValue("") Long id,
            @QueryParam("returnTo") @DefaultValue("") String returnTo) {

        Optional<Account> accountOpt = getAccount(session);
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

        View view = new View("questions/edit", params(
                "form", new FormHelper<QuestionEditForm>(form),
                "question", question));

        if (returnTo != null && !returnTo.isEmpty()) {
            session.put("returnTo", returnTo);
        } else {
            session.remove("returnTo");
        }
        return Response.ok(view).cookie(session.toCookie()).build();
    }

    @POST
    @Path("edit")
    @Consumes("application/x-www-form-urlencoded")
    public Response postEdit(
            @Context Session session,
            @Context UriInfo uinfo,
            @FormParam("id") Long id,
            MultivaluedMap<String, String> formParams) {

        Optional<Account> accountOpt = getAccount(session);
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
                    "form", new FormHelper<QuestionEditForm>(form, errors),
                    "question", question));
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(view).build();
        }

        if (question == null) {
            question = new Question();
            question.setAuthorId(account.getId());
            question.setPostedAt(new java.util.Date());
        }
        question.setSubject(form.getSubject());
        question.setBody(form.getBody());
        question.setStatus(Question.Status.fromValue(form.getStatus()));
        questionDao.save(question);

        Optional<String> returnTo = session.remove("returnTo");
        if (returnTo.isPresent()) {
            return redirect(uinfo, returnTo.get());
        } else {
            if (question.getStatus() == Question.Status.PUBLISHED) {
                return redirect(uinfo, "/questions/" + question.getId());
            } else {
                return redirectToDashboard(uinfo);
            }
        }
    }

    @GET
    @Path("delete")
    public Response delete(
            @Context Session session,
            @Context UriInfo uinfo,
            @QueryParam("id") @DefaultValue("") Long id) {

        Optional<Account> accountOpt = getAccount(session);
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

    private Optional<Account> getAccount(Session session) {
        Optional<String> accountId = session.get("accountId");
        if (!accountId.isPresent())
            return Optional.absent();
        return accountDao.find(Long.parseLong(accountId.get()));
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
