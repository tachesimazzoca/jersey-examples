package app.controllers;

import app.core.inject.UserContext;
import app.core.util.Pagination;
import app.core.view.View;
import app.models.Account;
import app.models.AnswerDao;
import app.models.AnswersResult;
import app.models.ForumUser;
import app.models.QuestionDao;
import app.models.QuestionsResult;
import com.google.common.base.Optional;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import static app.core.util.ParameterUtils.params;

@Path("/dashboard")
@Produces(MediaType.TEXT_HTML)
public class DashboardController {
    private final QuestionDao questionDao;
    private final AnswerDao answerDao;

    public DashboardController(
            QuestionDao questionDao,
            AnswerDao answerDao) {
        this.questionDao = questionDao;
        this.answerDao = answerDao;
    }

    @GET
    public Response index(
            @UserContext ForumUser forumUser,
            @Context UriInfo uinfo) {

        Optional<Account> accountOpt = forumUser.getAccount();
        if (!accountOpt.isPresent())
            return redirectToLogin(uinfo, "/dashboard");
        Account account = accountOpt.get();

        return Response.ok(new View("dashboard/index", params(
                "account", account))).build();
    }

    @GET
    @Path("questions")
    public Response questions(
            @UserContext ForumUser forumUser,
            @Context UriInfo uinfo,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit) {

        Optional<Account> accountOpt = forumUser.getAccount();
        if (!accountOpt.isPresent())
            return redirectToLogin(uinfo, "/dashboard/questions");
        Account account = accountOpt.get();

        Pagination<QuestionsResult> questions =
                questionDao.selectByAuthorId(account.getId(), offset, limit);
        return Response.ok(new View("dashboard/questions", params(
                "account", account,
                "questions", questions))).build();
    }

    @GET
    @Path("answers")
    public Response answers(
            @UserContext ForumUser forumUser,
            @Context UriInfo uinfo,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit) {

        Optional<Account> accountOpt = forumUser.getAccount();
        if (!accountOpt.isPresent())
            return redirectToLogin(uinfo, "/dashboard/answers");
        Account account = accountOpt.get();

        Pagination<AnswersResult> answers =
                answerDao.selectByAuthorId(account.getId(), offset, limit);
        return Response.ok(new View("dashboard/answers", params(
                "account", account,
                "answers", answers))).build();
    }

    private Response redirectToLogin(UriInfo uinfo, String returnTo) {
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/accounts/signin")
                .queryParam("returnTo", returnTo)
                .build()).build();
    }
}
