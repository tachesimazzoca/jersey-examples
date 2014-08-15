package app.controllers;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.google.common.base.Optional;

import app.core.*;
import app.models.*;

import static app.core.Util.params;

@Path("/dashboard")
@Produces(MediaType.TEXT_HTML)
public class DashboardController {
    private final AccountDao accountDao;
    private final QuestionDao questionDao;
    private final AnswerDao answerDao;

    public DashboardController(
            AccountDao accountDao,
            QuestionDao questionDao,
            AnswerDao answerDao) {
        this.accountDao = accountDao;
        this.questionDao = questionDao;
        this.answerDao = answerDao;
    }

    @GET
    public Response index(
            @Context Session session,
            @Context UriInfo uinfo) {
        Optional<Account> accountOpt = getAccount(session);
        if (!accountOpt.isPresent())
            return redirectToLogin(uinfo, "/dashboard");
        Account account = accountOpt.get();

        return Response.ok(new View("dashboard/index", params(
                "account", account))).build();
    }

    @GET
    @Path("questions")
    public Response questions(
            @Context Session session,
            @Context UriInfo uinfo,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit) {
        Optional<Account> accountOpt = getAccount(session);
        if (!accountOpt.isPresent())
            return redirectToLogin(uinfo, "/dashboard/questions");
        Account account = accountOpt.get();

        PaginationHelper<QuestionsResult> pagination = new PaginationHelper<QuestionsResult>(
                questionDao.selectByAuthorId(account.getId(), offset, limit),
                "questions?offset=%d&limit=%d");
        return Response.ok(new View("dashboard/questions", params(
                "pagination", pagination))).build();
    }

    @GET
    @Path("answers")
    public Response answers(
            @Context Session session,
            @Context UriInfo uinfo,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit) {
        Optional<Account> accountOpt = getAccount(session);
        if (!accountOpt.isPresent())
            return redirectToLogin(uinfo, "/dashboard/answers");
        Account account = accountOpt.get();

        PaginationHelper<AnswersResult> pagination = new PaginationHelper<AnswersResult>(
                answerDao.selectByAuthorId(account.getId(), offset, limit),
                "answers?offset=%d&limit=%d");
        return Response.ok(new View("dashboard/answers", params(
                "pagination", pagination))).build();
    }

    private Optional<Account> getAccount(Session session) {
        Optional<String> accountId = session.get("accountId");
        if (!accountId.isPresent())
            return Optional.absent();
        return accountDao.find(Long.parseLong(accountId.get()));
    }

    private Response redirectToLogin(UriInfo uinfo, String returnTo) {
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/accounts/signin")
                .queryParam("url", returnTo)
                .build()).build();
    }
}
