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
import app.models.ForumUser;
import app.models.Question;
import app.models.QuestionDao;
import app.models.QuestionEditForm;
import app.models.QuestionsResult;
import com.google.common.base.Optional;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
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
            QuestionDao questionDao,
            AnswerDao answerDao,
            AccountDao accountDao,
            AccountQuestionDao accountQuestionDao) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
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
            @UserContext ForumUser forumUser,
            @Context UriInfo uinfo,
            @QueryParam("sort") @DefaultValue("") String sort,
            @QueryParam("offset") @DefaultValue("") Integer offset,
            @QueryParam("limit") @DefaultValue("") Integer limit) {

        final String KEY_CONDITION = "questions.condition";

        @SuppressWarnings("unchecked")
        Map<String, Object> condition = (Map<String, Object>) forumUser.getAttribute(
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

        forumUser.setAttribute(KEY_CONDITION, params(
                "offset", (Integer) questions.getOffset(),
                "limit", (Integer) questions.getLimit(),
                "sort", sort));

        return Response.ok(new View("questions/index", params(
                "account", forumUser.getAccount().orNull(),
                "questions", questions,
                "sort", sort,
                "sortMap", sortMap))).cookie(forumUser.toCookie()).build();
    }

    @GET
    @Path("{id}")
    public Response detail(
            @UserContext ForumUser forumUser,
            @Context UriInfo uinfo,
            @PathParam("id") Long id,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("5") int limit) {

        // account
        Optional<Account> accountOpt = forumUser.getAccount();
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
        Pagination<AnswersResult> answers = answerDao.selectByQuestionId(id, offset, limit);
        View view = new View("questions/detail", params(
                "account", account,
                "questionInfo", questionInfo,
                "question", question,
                "author", author,
                "answers", answers));
        return Response.ok(view).build();
    }

    private Response vote(ForumUser forumUser, UriInfo uinfo, Long id, int point) {
        Optional<Question> questionOpt = questionDao.find(id);
        if (!questionOpt.isPresent())
            return redirectToIndex(uinfo);
        Question question = questionOpt.get();
        if (question.getStatus() != Question.Status.PUBLISHED)
            return redirectToIndex(uinfo);

        Optional<Account> accountOpt = forumUser.getAccount();
        if (!accountOpt.isPresent())
            return redirect(uinfo, "/questions/" + id);
        Account account = accountOpt.get();

        accountQuestionDao.log(account.getId(), question.getId(), point);

        return redirect(uinfo, "/questions/" + id);
    }

    @GET
    @Path("star")
    public Response star(
            @UserContext ForumUser forumUser,
            @Context UriInfo uinfo,
            @QueryParam("id") Long id) {
        return vote(forumUser, uinfo, id, 1);
    }

    @GET
    @Path("unstar")
    public Response unstar(
            @UserContext ForumUser forumUser,
            @Context UriInfo uinfo,
            @QueryParam("id") Long id) {

        return vote(forumUser, uinfo, id, 0);
    }

    @GET
    @Path("edit")
    public Response edit(
            @UserContext ForumUser forumUser,
            @Context UriInfo uinfo,
            @QueryParam("id") @DefaultValue("") Long id) {

        Optional<Account> accountOpt = forumUser.getAccount();
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

        String flash = forumUser.getFlash().orNull();
        View view = new View("questions/edit", params(
                "account", account,
                "form", new FormHelper<QuestionEditForm>(form),
                "question", question,
                "flash", flash));
        return Response.ok(view).cookie(forumUser.toCookie()).build();
    }

    @POST
    @Path("edit")
    @Consumes("application/x-www-form-urlencoded")
    public Response postEdit(
            @UserContext ForumUser forumUser,
            @Context UriInfo uinfo,
            @FormParam("id") Long id,
            MultivaluedMap<String, String> formParams) {

        Optional<Account> accountOpt = forumUser.getAccount();
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
            forumUser.setFlash("created");
        } else {
            forumUser.setFlash("updated");
        }
        question.setSubject(form.getSubject());
        question.setBody(form.getBody());
        question.setStatus(Question.Status.fromValue(form.getStatus()));
        questionDao.save(question);

        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/questions/edit")
                .queryParam("id", question.getId())
                .build()).cookie(forumUser.toCookie()).build();
    }

    @GET
    @Path("delete")
    public Response delete(
            @UserContext ForumUser forumUser,
            @Context UriInfo uinfo,
            @QueryParam("id") @DefaultValue("") Long id) {

        Optional<Account> accountOpt = forumUser.getAccount();
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
