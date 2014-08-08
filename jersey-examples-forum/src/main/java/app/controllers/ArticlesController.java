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

@Path("/articles")
@Produces(MediaType.TEXT_HTML)
public class ArticlesController {
    private final Validator validator;
    private final ArticleDao articleDao;

    public ArticlesController(ArticleDao articleDao) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.articleDao = articleDao;
    }

    @GET
    @Path("edit")
    public Response edit(
            @Context User user,
            @Context UriInfo uinfo,
            @QueryParam("parentId") @DefaultValue("") Long parentId,
            @QueryParam("id") @DefaultValue("") Long id) {
        if (!user.getAccount().isPresent()) {
            return redirectToLogin(uinfo, id);
        }

        ArticleEditForm form;
        if (id != null) {
            Optional<Article> articleOpt = articleDao.find(id);
            if (!articleOpt.isPresent())
                return redirectToIndex(uinfo);
            Article article = articleOpt.get();
            if (!isQuestionAuthor(user, article))
                return redirectToIndex(uinfo);
            form = ArticleEditForm.bindFrom(article);
        } else {
            form = ArticleEditForm.defaultForm();
            if (parentId != null) {
                form.setParentId(parentId.toString());
            }
        }

        if (parentId != null) {
            Optional<Article> articleOpt = articleDao.find(parentId);
            if (!articleOpt.isPresent())
                return redirectToIndex(uinfo);
        }

        View view = new View("articles/edit", params(
                "form", new FormHelper<ArticleEditForm>(form)));
        return Response.ok(view).build();
    }

    @POST
    @Path("edit")
    @Consumes("application/x-www-form-urlencoded")
    public Response postEdit(
            @Context User user,
            @Context UriInfo uinfo,
            @FormParam("parentId") Long parentId,
            @FormParam("id") Long id,
            MultivaluedMap<String, String> formParams) {
        if (!user.getAccount().isPresent()) {
            return redirectToLogin(uinfo, id);
        }
        Account account = user.getAccount().get();

        Article article;
        ArticleEditForm form = ArticleEditForm.bindFrom(formParams);

        if (id != null) {
            Optional<Article> articleOpt = articleDao.find(id);
            if (!articleOpt.isPresent())
                return redirectToIndex(uinfo);
            article = articleOpt.get();
            if (!isQuestionAuthor(user, article))
                return redirectToIndex(uinfo);
        } else {
            article = new Article();
            if (parentId != null)
                article.setParentId(parentId);
            else
                article.setParentId(0L);
            article.setAuthorId(account.getId());
            article.setPostedAt(new java.util.Date());
        }

        if (!article.isQuestion()) {
            Optional<Article> parentOpt = articleDao.find(article.getParentId());
            if (!parentOpt.isPresent())
                return redirectToIndex(uinfo);
        }

        Set<ConstraintViolation<ArticleEditForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("articles/edit", params(
                    "form", new FormHelper<ArticleEditForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(view).build();
        }

        article.setSubject(form.getSubject());
        article.setBody(form.getBody());
        articleDao.save(article);

        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/questions").build()).build();
    }

    private Response redirectToIndex(UriInfo uinfo) {
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/questions")
                .build()).build();
    }

    private Response redirectToLogin(UriInfo uinfo, Long id) {
        String url = "/articles/edit";
        if (id != null)
            url += "?id=" + id;
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/accounts/signin")
                .queryParam("url", url)
                .build()).build();
    }

    private boolean isQuestionAuthor(User user, Article article) {
        if (!user.getAccount().isPresent())
            return false;
        Account account = user.getAccount().get();
        if (article.getAuthorId() == 0)
            return false;
        if (article.getAuthorId() != account.getId())
            return false;
        return true;
    }
}
