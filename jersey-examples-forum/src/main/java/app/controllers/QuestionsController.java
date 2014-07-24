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

@Path("/questions")
@Produces(MediaType.TEXT_HTML)
public class QuestionsController {
    private final Validator validator;
    private final QuestionDao questionDao;
    private final AccountDao accountDao;

    public QuestionsController(QuestionDao questionDao, AccountDao accountDao) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.questionDao = questionDao;
        this.accountDao = accountDao;
    }

    @GET
    public Response index() {
        return Response.ok(new View("questions/index")).build();
    }

    @GET
    @Path("{id}")
    public Response detail(
            @Context UriInfo uinfo,
            @PathParam("id") Long id) {
        Optional<Question> questionOpt = questionDao.find(id);
        if (!questionOpt.isPresent())
            return redirectToIndex(uinfo);
        Question question = questionOpt.get();
        Optional<Account> accountOpt = accountDao.find(question.getAuthorId());
        if (!accountOpt.isPresent())
            return redirectToIndex(uinfo);
        Account author = accountOpt.get();

        View view = new View("questions/detail", params(
                "question", question,
                "author", author));
        return Response.ok(view).build();
    }

    @GET
    @Path("edit")
    public Response edit(
            @Context User user,
            @Context UriInfo uinfo,
            @QueryParam("id") @DefaultValue("") Long id) {
        if (!user.getAccount().isPresent()) {
            return redirectToLogin(uinfo, id);
        }

        QuestionsEditForm form;
        if (id != null) {
            Optional<Question> questionOpt = questionDao.find(id);
            if (!questionOpt.isPresent())
                return redirectToIndex(uinfo);
            Question question = questionOpt.get();
            if (!isQuestionAuthor(user, question))
                return redirectToIndex(uinfo);
            form = QuestionsEditForm.bindFrom(question);
        } else {
            form = QuestionsEditForm.defaultForm();
        }

        View view = new View("questions/edit", params(
                "form", new FormHelper<QuestionsEditForm>(form)));
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
        if (!user.getAccount().isPresent()) {
            return redirectToLogin(uinfo, id);
        }
        Account account = user.getAccount().get();

        Question question;
        QuestionsEditForm form = QuestionsEditForm.bindFrom(formParams);
        if (!form.getId().isEmpty()) {
            Optional<Question> questionOpt = questionDao.find(id);
            if (!questionOpt.isPresent())
                return redirectToIndex(uinfo);
            question = questionOpt.get();
        } else {
            question = new Question();
        }

        Set<ConstraintViolation<QuestionsEditForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("questions/edit", params(
                    "form", new FormHelper<QuestionsEditForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(view).build();
        }

        question.setSubject(form.getSubject());
        question.setBody(form.getBody());
        if (question.getId() == null) {
            question.setAuthorId(account.getId());
            question.setPostedAt(new java.util.Date());
        }
        questionDao.save(question);

        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/questions").build()).build();
    }

    private Response redirectToIndex(UriInfo uinfo) {
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/questions")
                .build()).build();
    }

    private Response redirectToLogin(UriInfo uinfo, Long id) {
        String url = "/questions/edit";
        if (id != null)
            url += "?id=" + id;
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/accounts/signin")
                .queryParam("url", url)
                .build()).build();
    }

    private boolean isQuestionAuthor(User user, Question question) {
        if (!user.getAccount().isPresent())
            return false;
        Account account = user.getAccount().get();
        if (question.getAuthorId() == 0)
            return false;
        if (question.getAuthorId() != account.getId())
            return false;
        return true;
    }
}