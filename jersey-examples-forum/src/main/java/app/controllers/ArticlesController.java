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
    public Response index() {
        return Response.ok(new View("articles/index")).build();
    }

    @GET
    @Path("edit")
    public Response edit(
            @Context User user,
            @Context UriInfo uinfo,
            @QueryParam("id") @DefaultValue("") Long id) {
        Response redirectToIndex = redirectToIndex(uinfo);
        Response redirectToLogin = redirectToLogin(uinfo, id);

        if (!user.getAccount().isPresent()) {
            return redirectToLogin;
        }

        ArticlesEditForm form;
        if (id != null) {
            Optional<Article> articleOpt = articleDao.find(id);
            if (!articleOpt.isPresent())
                return redirectToIndex;
            Article article = articleOpt.get();
            if (!isArticleOwner(user, article))
                return redirectToIndex;
            form = ArticlesEditForm.bindFrom(article);
        } else {
            form = ArticlesEditForm.defaultForm();
        }

        View view = new View("articles/edit", params(
                "form", new FormHelper<ArticlesEditForm>(form)));
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

        Response redirectToIndex = redirectToIndex(uinfo);
        Response redirectToLogin = redirectToLogin(uinfo, id);

        if (!user.getAccount().isPresent()) {
            return redirectToLogin;
        }
        Account account = user.getAccount().get();

        Article article;
        ArticlesEditForm form = ArticlesEditForm.bindFrom(formParams);
        if (!form.getId().isEmpty()) {
            Optional<Article> articleOpt = articleDao.find(id);
            if (!articleOpt.isPresent())
                return redirectToIndex;
            article = articleOpt.get();
        } else {
            article = new Article();
        }

        Set<ConstraintViolation<ArticlesEditForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("articles/edit", params(
                    "form", new FormHelper<ArticlesEditForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(view).build();
        }

        article.setSubject(form.getSubject());
        article.setBody(form.getBody());
        if (article.getId() == null) {
            article.setAuthorId(account.getId());
            article.setPostedAt(new java.util.Date());
        }
        articleDao.save(article);

        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/articles").build()).build();
    }

    private Response redirectToIndex(UriInfo uinfo) {
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/articles")
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

    private boolean isArticleOwner(User user, Article article) {
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
