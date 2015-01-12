package app.controllers;

import app.core.inject.UserContext;
import app.core.view.FormHelper;
import app.core.view.View;
import app.models.Account;
import app.models.AccountAnswerDao;
import app.models.Answer;
import app.models.AnswerDao;
import app.models.AnswerEditForm;
import app.models.Question;
import app.models.QuestionDao;
import app.models.UserHelper;
import com.google.common.base.Optional;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Set;

import static app.core.util.ParameterUtils.params;
import static app.core.util.URIUtils.safeURI;

@Path("/answers")
@Produces(MediaType.TEXT_HTML)
public class AnswersController {
    private final Validator validator;
    private final QuestionDao questionDao;
    private final AnswerDao answerDao;
    private final AccountAnswerDao accountAnswerDao;

    public AnswersController(
            Validator validator,
            QuestionDao questionDao,
            AnswerDao answerDao,
            AccountAnswerDao accountAnswerDao) {
        this.validator = validator;
        this.answerDao = answerDao;
        this.questionDao = questionDao;
        this.accountAnswerDao = accountAnswerDao;
    }

    @GET
    @Path("edit")
    public Response edit(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo,
            @QueryParam("questionId") @DefaultValue("") Long questionId,
            @QueryParam("id") @DefaultValue("") Long id) {
        Optional<Account> accountOpt = userHelper.getAccount();
        if (!accountOpt.isPresent()) {
            String returnTo = "/answers/edit";
            if (id != null)
                returnTo += "?id=" + id;
            else
                returnTo += "?questionId=" + questionId;
            return redirectToLogin(uriInfo, returnTo);
        }
        Account account = accountOpt.get();

        Answer answer;
        AnswerEditForm form;
        if (id != null) {
            Optional<Answer> answerOpt = answerDao.find(id);
            if (!answerOpt.isPresent())
                return redirectToIndex(uriInfo, null);
            answer = answerOpt.get();
            if (!answer.isSameAuthor(account))
                return redirectToIndex(uriInfo, answer.getQuestionId());
            form = AnswerEditForm.bindFrom(answer);
            questionId = answer.getQuestionId();
        } else {
            answer = null;
            form = AnswerEditForm.defaultForm();
        }
        if (questionId == null)
            return redirectToIndex(uriInfo, null);

        Optional<Question> questionOpt = questionDao.find(questionId);
        if (!questionOpt.isPresent()) {
            return redirectToIndex(uriInfo, null);
        }
        Question question = questionOpt.get();
        form.setQuestionId(question.getId().toString());

        String flash = userHelper.getFlash().orNull();
        View view = new View("answers/edit", params(
                "account", account,
                "form", new FormHelper<AnswerEditForm>(form),
                "question", question,
                "answer", answer,
                "flash", flash));
        return Response.ok(view).cookie(userHelper.toCookie()).build();
    }

    @POST
    @Path("edit")
    @Consumes("application/x-www-form-urlencoded")
    public Response postEdit(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo,
            @FormParam("questionId") Long questionId,
            @FormParam("id") Long id,
            MultivaluedMap<String, String> formParams) {

        Optional<Account> accountOpt = userHelper.getAccount();
        if (!accountOpt.isPresent()) {
            return redirectToIndex(uriInfo, null);
        }
        Account account = accountOpt.get();

        Answer answer;
        AnswerEditForm form = AnswerEditForm.bindFrom(formParams);
        if (id != null) {
            Optional<Answer> answerOpt = answerDao.find(id);
            if (!answerOpt.isPresent())
                return redirectToIndex(uriInfo, null);
            answer = answerOpt.get();
            if (!answer.isSameAuthor(account))
                return redirectToIndex(uriInfo, answer.getQuestionId());
        } else {
            answer = null;
        }

        Optional<Question> questionOpt = questionDao.find(questionId);
        if (!questionOpt.isPresent()) {
            return redirectToIndex(uriInfo, null);
        }
        Question question = questionOpt.get();
        form.setQuestionId(question.getId().toString());

        Set<ConstraintViolation<AnswerEditForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("answers/edit", params(
                    "account", account,
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
            userHelper.setFlash("created");
        } else {
            userHelper.setFlash("updated");
        }
        answer.setBody(form.getBody());
        answer.setStatus(Answer.Status.fromValue(form.getStatus()));
        answerDao.save(answer);

        return Response.seeOther(uriInfo.getBaseUriBuilder()
                .path("/answers/edit")
                .queryParam("id", answer.getId())
                .build()).cookie(userHelper.toCookie()).build();
    }

    @GET
    @Path("delete")
    public Response delete(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo,
            @QueryParam("id") @DefaultValue("") Long id) {

        Optional<Account> accountOpt = userHelper.getAccount();
        if (!accountOpt.isPresent())
            return redirectToIndex(uriInfo, null);
        Account account = accountOpt.get();

        if (id == null)
            return redirectToDashboard(uriInfo);

        Optional<Answer> answerOpt = answerDao.find(id);
        if (!answerOpt.isPresent())
            return redirectToDashboard(uriInfo);
        Answer answer = answerOpt.get();
        if (!answer.isSameAuthor(account))
            return redirectToIndex(uriInfo, null);

        answerDao.updateStatus(id, Answer.Status.DELETED);
        return redirectToDashboard(uriInfo);
    }

    @GET
    @Path("vote")
    public Response vote(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo,
            @QueryParam("id") @DefaultValue("") Long id,
            @QueryParam("point") @DefaultValue("0") int point,
            @QueryParam("returnTo") @DefaultValue("") String returnTo) {

        Optional<Account> accountOpt = userHelper.getAccount();
        if (!accountOpt.isPresent())
            return redirectToIndex(uriInfo, null);
        Account account = accountOpt.get();

        if (id == null)
            return redirectToDashboard(uriInfo);

        // limit -1 =< point <= 1
        if (point > 1)
            point = 1;
        if (point < -1)
            point = -1;

        Optional<Answer> answerOpt = answerDao.find(id);
        if (!answerOpt.isPresent())
            return redirectToDashboard(uriInfo);
        Answer answer = answerOpt.get();
        // Not allowed to vote by the same author
        if (!answer.isSameAuthor(account))
            accountAnswerDao.log(account.getId(), answer.getId(), point);

        if (returnTo != null && !returnTo.isEmpty())
            return redirect(uriInfo, returnTo);
        else
            return redirectToIndex(uriInfo, answer.getQuestionId());
    }

    private Response redirect(UriInfo uriInfo, String path) {
        return Response.seeOther(safeURI(uriInfo, path)).build();
    }

    private Response redirectToIndex(UriInfo uriInfo, Long questionId) {
        String path = "/questions";
        if (questionId != null) {
            path += "/" + questionId;
        }
        return redirect(uriInfo, path);
    }

    private Response redirectToDashboard(UriInfo uriInfo) {
        return redirect(uriInfo, "/dashboard/answers");
    }

    private Response redirectToLogin(UriInfo uriInfo, String returnTo) {
        return Response.seeOther(uriInfo.getBaseUriBuilder()
                .path("/accounts/signin")
                .queryParam("returnTo", returnTo)
                .build()).build();
    }
}
