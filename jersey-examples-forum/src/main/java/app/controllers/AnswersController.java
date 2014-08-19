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

@Path("/answers")
@Produces(MediaType.TEXT_HTML)
public class AnswersController {
    private final Validator validator;
    private final AccountDao accountDao;
    private final QuestionDao questionDao;
    private final AnswerDao answerDao;

    public AnswersController(
            AccountDao accountDao,
            QuestionDao questionDao,
            AnswerDao answerDao) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.accountDao = accountDao;
        this.answerDao = answerDao;
        this.questionDao = questionDao;
    }

    @GET
    @Path("edit")
    public Response edit(
            @Context Session session,
            @Context UriInfo uinfo,
            @QueryParam("questionId") @DefaultValue("") Long questionId,
            @QueryParam("id") @DefaultValue("") Long id) {
        Optional<Account> accountOpt = getAccount(session);
        if (!accountOpt.isPresent()) {
            String returnTo = "/answers/edit";
            if (id != null)
                returnTo += "?id=" + id;
            else
                returnTo += "?questionId=" + questionId;
            return redirectToLogin(uinfo, returnTo);
        }
        Account account = accountOpt.get();

        Answer answer;
        AnswerEditForm form;
        if (id != null) {
            Optional<Answer> answerOpt = answerDao.find(id);
            if (!answerOpt.isPresent())
                return redirectToIndex(uinfo, null);
            answer = answerOpt.get();
            if (!answer.isSameAuthor(account))
                return redirectToIndex(uinfo, answer.getQuestionId());
            form = AnswerEditForm.bindFrom(answer);
            questionId = answer.getQuestionId();
        } else {
            answer = null;
            form = AnswerEditForm.defaultForm();
        }
        if (questionId == null)
            return redirectToIndex(uinfo, null);

        Optional<Question> questionOpt = questionDao.find(questionId);
        if (!questionOpt.isPresent()) {
            return redirectToIndex(uinfo, null);
        }
        Question question = questionOpt.get();
        form.setQuestionId(question.getId().toString());

        String flash = session.remove("flash").orNull();
        View view = new View("answers/edit", params(
                "form", new FormHelper<AnswerEditForm>(form),
                "question", question,
                "answer", answer,
                "flash", flash));
        return Response.ok(view).cookie(session.toCookie()).build();
    }

    @POST
    @Path("edit")
    @Consumes("application/x-www-form-urlencoded")
    public Response postEdit(
            @Context Session session,
            @Context UriInfo uinfo,
            @FormParam("questionId") Long questionId,
            @FormParam("id") Long id,
            MultivaluedMap<String, String> formParams) {

        Optional<Account> accountOpt = getAccount(session);
        if (!accountOpt.isPresent()) {
            return redirectToIndex(uinfo, null);
        }
        Account account = accountOpt.get();

        Answer answer;
        AnswerEditForm form = AnswerEditForm.bindFrom(formParams);
        if (id != null) {
            Optional<Answer> answerOpt = answerDao.find(id);
            if (!answerOpt.isPresent())
                return redirectToIndex(uinfo, null);
            answer = answerOpt.get();
            if (!answer.isSameAuthor(account))
                return redirectToIndex(uinfo, answer.getQuestionId());
        } else {
            answer = null;
        }

        Optional<Question> questionOpt = questionDao.find(questionId);
        if (!questionOpt.isPresent()) {
            return redirectToIndex(uinfo, null);
        }
        Question question = questionOpt.get();
        form.setQuestionId(question.getId().toString());

        Set<ConstraintViolation<AnswerEditForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("answers/edit", params(
                    "form", new FormHelper<AnswerEditForm>(form, errors),
                    "question", question,
                    "answer", answer));
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(view).build();
        }

        if (answer == null) {
            answer = new Answer();
            answer.setQuestionId(questionId);
            answer.setAuthorId(account.getId());
            answer.setPostedAt(new java.util.Date());
            session.put("flash", "created");
        } else {
            session.put("flash", "updated");
        }
        answer.setBody(form.getBody());
        answer.setStatus(Answer.Status.fromValue(form.getStatus()));
        answerDao.save(answer);

        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/answers/edit")
                .queryParam("id", answer.getId())
                .build()).cookie(session.toCookie()).build();
    }

    @GET
    @Path("delete")
    public Response delete(
            @Context Session session,
            @Context UriInfo uinfo,
            @QueryParam("id") @DefaultValue("") Long id) {

        Optional<Account> accountOpt = getAccount(session);
        if (!accountOpt.isPresent())
            return redirectToIndex(uinfo, null);
        Account account = accountOpt.get();

        if (id == null)
            return redirectToDashboard(uinfo);

        Optional<Answer> answerOpt = answerDao.find(id);
        if (!answerOpt.isPresent())
            return redirectToDashboard(uinfo);
        Answer answer = answerOpt.get();
        if (!answer.isSameAuthor(account))
            return redirectToIndex(uinfo, null);

        answerDao.updateStatus(id, Answer.Status.DELETED);
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

    private Response redirectToIndex(UriInfo uinfo, Long questionId) {
        String path = "/questions";
        if (questionId != null) {
            path += "/" + questionId;
        }
        return redirect(uinfo, path);
    }

    private Response redirectToDashboard(UriInfo uinfo) {
        return redirect(uinfo, "/dashboard/answers");
    }

    private Response redirectToLogin(UriInfo uinfo, String returnTo) {
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/accounts/signin")
                .queryParam("returnTo", returnTo)
                .build()).build();
    }
}
