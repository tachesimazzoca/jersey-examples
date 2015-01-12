package app.controllers;

import app.core.inject.UserContext;
import app.core.util.Pagination;
import app.core.view.FormHelper;
import app.core.view.View;
import app.models.Account;
import app.models.AccountDao;
import app.models.AccountQuestionDao;
import app.models.AnswerDao;
import app.models.AnswersResult;
import app.models.Question;
import app.models.QuestionDao;
import app.models.QuestionEditForm;
import app.models.QuestionsResult;
import app.models.UserHelper;
import com.google.common.base.Optional;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Map;
import java.util.Set;

import static app.core.util.ParameterUtils.emptyTo;
import static app.core.util.ParameterUtils.nullTo;
import static app.core.util.ParameterUtils.params;
import static app.core.util.URIUtils.safeURI;
import static org.apache.commons.lang.StringUtils.isEmpty;

@Path("/questions")
@Produces(MediaType.TEXT_HTML)
public class QuestionsController {
    private final Validator validator;
    private final QuestionDao questionDao;
    private final AnswerDao answerDao;
    private final AccountDao accountDao;
    private final AccountQuestionDao accountQuestionDao;
    private final Map<String, Object> sortMap;

    public QuestionsController(
            Validator validator,
            QuestionDao questionDao,
            AnswerDao answerDao,
            AccountDao accountDao,
            AccountQuestionDao accountQuestionDao) {
        this.validator = validator;
        this.questionDao = questionDao;
        this.answerDao = answerDao;
        this.accountDao = accountDao;
        this.accountQuestionDao = accountQuestionDao;

        sortMap = params(
                QuestionsResult.OrderBy.POSTED_AT_DESC.getName(), "Newest",
                QuestionsResult.OrderBy.NUM_ANSWERS_DESC.getName(), "Active",
                QuestionsResult.OrderBy.SUM_POINTS_DESC.getName(), "Vote");
    }

    @GET
    public Response index(
            @UserContext UserHelper userHelper,
            @QueryParam("sort") @DefaultValue("") String sort,
            @QueryParam("offset") @DefaultValue("") Integer offset,
            @QueryParam("limit") @DefaultValue("") Integer limit) {

        final String KEY_CONDITION = "questions.condition";

        @SuppressWarnings("unchecked")
        Map<String, Object> condition = (Map<String, Object>) userHelper.getAttribute(
                KEY_CONDITION, Map.class).or(params());

        offset = nullTo(offset, (Integer) condition.get("offset"), 0);
        limit = nullTo(limit, (Integer) condition.get("limit"), 20);
        sort = emptyTo(sort, (String) condition.get("sort"));

        QuestionsResult.OrderBy orderBy;
        if (!isEmpty(sort) && sortMap.containsKey(sort))
            orderBy = QuestionsResult.OrderBy.fromName(sort);
        else
            orderBy = QuestionsResult.OrderBy.defaultValue();
        sort = orderBy.getName();

        Pagination<QuestionsResult> questions = questionDao.selectPublicQuestions(
                offset, limit, orderBy);

        userHelper.setAttribute(KEY_CONDITION, params(
                "offset", questions.getOffset(),
                "limit", questions.getLimit(),
                "sort", sort));

        return Response.ok(new View("questions/index", params(
                "account", userHelper.getAccount().orNull(),
                "questions", questions,
                "sort", sort,
                "sortMap", sortMap))).cookie(userHelper.toCookie()).build();
    }

    @GET
    @Path("{id}")
    public Response detail(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo,
            @PathParam("id") Long id,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("5") int limit) {

        // account
        Optional<Account> accountOpt = userHelper.getAccount();
        Account account = accountOpt.orNull();

        // question
        Optional<Question> questionOpt = questionDao.find(id);
        if (!questionOpt.isPresent())
            return redirectToIndex(uriInfo);
        Question question = questionOpt.get();
        if (question.getStatus() != Question.Status.PUBLISHED)
            return redirectToIndex(uriInfo);

        // author
        Optional<Account> authorOpt = accountDao.find(question.getAuthorId());
        if (!authorOpt.isPresent())
            return redirectToIndex(uriInfo);
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
        Pagination<AnswersResult> answers = answerDao.selectByQuestionId(id, offset, limit);
        View view = new View("questions/detail", params(
                "account", account,
                "questionInfo", questionInfo,
                "question", question,
                "author", author,
                "answers", answers));
        return Response.ok(view).build();
    }

    private Response vote(UserHelper userHelper, UriInfo uriInfo, Long id, int point) {
        Optional<Question> questionOpt = questionDao.find(id);
        if (!questionOpt.isPresent())
            return redirectToIndex(uriInfo);
        Question question = questionOpt.get();
        if (question.getStatus() != Question.Status.PUBLISHED)
            return redirectToIndex(uriInfo);

        Optional<Account> accountOpt = userHelper.getAccount();
        if (!accountOpt.isPresent())
            return redirect(uriInfo, "/questions/" + id);
        Account account = accountOpt.get();

        accountQuestionDao.log(account.getId(), question.getId(), point);

        return redirect(uriInfo, "/questions/" + id);
    }

    @GET
    @Path("star")
    public Response star(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo,
            @QueryParam("id") Long id) {
        return vote(userHelper, uriInfo, id, 1);
    }

    @GET
    @Path("unstar")
    public Response unstar(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo,
            @QueryParam("id") Long id) {

        return vote(userHelper, uriInfo, id, 0);
    }

    @GET
    @Path("edit")
    public Response edit(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo,
            @QueryParam("id") @DefaultValue("") Long id) {

        Optional<Account> accountOpt = userHelper.getAccount();
        if (!accountOpt.isPresent())
            return redirectToLogin(uriInfo, id);
        Account account = accountOpt.get();

        Question question = null;
        QuestionEditForm form;
        if (id != null) {
            Optional<Question> questionOpt = questionDao.find(id);
            if (!questionOpt.isPresent())
                return redirectToIndex(uriInfo);
            question = questionOpt.get();
            if (!question.isSameAuthor(account))
                return redirectToIndex(uriInfo);
            form = QuestionEditForm.bindFrom(question);
        } else {
            form = QuestionEditForm.defaultForm();
        }

        String flash = userHelper.getFlash().orNull();
        View view = new View("questions/edit", params(
                "account", account,
                "form", new FormHelper<QuestionEditForm>(form),
                "question", question,
                "flash", flash));
        return Response.ok(view).cookie(userHelper.toCookie()).build();
    }

    @POST
    @Path("edit")
    @Consumes("application/x-www-form-urlencoded")
    public Response postEdit(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo,
            @FormParam("id") Long id,
            MultivaluedMap<String, String> formParams) {

        Optional<Account> accountOpt = userHelper.getAccount();
        if (!accountOpt.isPresent())
            return redirectToLogin(uriInfo, id);
        Account account = accountOpt.get();

        Question question = null;
        QuestionEditForm form = QuestionEditForm.bindFrom(formParams);

        if (id != null) {
            Optional<Question> questionOpt = questionDao.find(id);
            if (!questionOpt.isPresent())
                return redirectToIndex(uriInfo);
            question = questionOpt.get();
            if (!question.isSameAuthor(account))
                return redirectToIndex(uriInfo);
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
            userHelper.setFlash("created");
        } else {
            userHelper.setFlash("updated");
        }
        question.setSubject(form.getSubject());
        question.setBody(form.getBody());
        question.setStatus(Question.Status.fromValue(form.getStatus()));
        questionDao.save(question);

        return Response.seeOther(uriInfo.getBaseUriBuilder()
                .path("/questions/edit")
                .queryParam("id", question.getId())
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
            return redirectToLogin(uriInfo, id);
        Account account = accountOpt.get();

        if (id == null)
            return redirectToDashboard(uriInfo);

        Optional<Question> questionOpt = questionDao.find(id);
        if (!questionOpt.isPresent())
            return redirectToDashboard(uriInfo);
        Question question = questionOpt.get();
        if (!question.isSameAuthor(account))
            return redirectToIndex(uriInfo);

        questionDao.updateStatus(id, Question.Status.DELETED);
        return redirectToDashboard(uriInfo);
    }

    private Response redirect(UriInfo uriInfo, String path) {
        return Response.seeOther(safeURI(uriInfo, path)).build();
    }

    private Response redirectToIndex(UriInfo uriInfo) {
        return redirect(uriInfo, "/questions");
    }

    private Response redirectToDashboard(UriInfo uriInfo) {
        return redirect(uriInfo, "/dashboard/questions");
    }

    private Response redirectToLogin(UriInfo uriInfo, String returnTo) {
        return Response.seeOther(uriInfo.getBaseUriBuilder()
                .path("/accounts/signin")
                .queryParam("returnTo", returnTo)
                .build()).build();
    }

    private Response redirectToLogin(UriInfo uriInfo, Long id) {
        String url = "/questions/edit";
        if (id != null)
            url += "?id=" + id;
        return redirectToLogin(uriInfo, url);
    }
}
