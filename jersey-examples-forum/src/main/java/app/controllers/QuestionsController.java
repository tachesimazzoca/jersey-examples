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

@Path("/questions")
@Produces(MediaType.TEXT_HTML)
public class QuestionsController {
    private final ArticleDao articleDao;
    private final AccountDao accountDao;

    public QuestionsController(ArticleDao articleDao, AccountDao accountDao) {
        this.articleDao = articleDao;
        this.accountDao = accountDao;
    }

    @GET
    public Response index(
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit) {
        PaginationHelper<ArticlesResult> pagination = new PaginationHelper<ArticlesResult>(
                articleDao.selectQuestions(offset, limit),
                "questions?offset=%d&limit=%d");
        return Response.ok(new View("questions/index", params(
                "pagination", pagination))).build();
    }

    @GET
    @Path("{id}")
    public Response detail(
            @Context UriInfo uinfo,
            @PathParam("id") Long id) {
        Optional<Article> articleOpt = articleDao.find(id);
        if (!articleOpt.isPresent())
            return redirectToIndex(uinfo);
        Article article = articleOpt.get();
        if (!article.isQuestion())
            return redirectToIndex(uinfo);
        Optional<Account> accountOpt = accountDao.find(article.getAuthorId());
        if (!accountOpt.isPresent())
            return redirectToIndex(uinfo);
        Account author = accountOpt.get();

        View view = new View("questions/detail", params(
                "question", article,
                "author", author));
        return Response.ok(view).build();
    }

    private Response redirectToIndex(UriInfo uinfo) {
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/questions")
                .build()).build();
    }
}
