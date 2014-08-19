<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Editing Answer">
<#if flash?has_content>
<div class="alert alert-success" data-role="flash">Your answer has been posted successfully.</div>
</#if>
<#if form.hasErrors()>
<div class="alert alert-danger">
<ul>
<#list form.errors as err>
  <li>${(err.message)?html}</li>
</#list>
</ul>
</div>
</#if>
<form action="edit" method="POST">
${form.toHTMLInput("hidden", "id")}
${form.toHTMLInput("hidden", "questionId")}
<div style="max-width: 640px">
  <#if answer?has_content>
  <div class="form-group">
    <div class="form-control-static"><strong>ID</strong>: ${answer.id}</div> 
  </div>
  </#if>
  <div class="form-group">
    <label>Question</label>
    <div class="form-control-static"><a href="${config.url.base}questions/${question.id}">${(question.subject)?html}</a></div>
  </div>
  <div class="form-group">
    <label>Body</label>
    <textarea name="body" class="form-control">${(form.form.body)?html}</textarea>
  </div>
  <div class="form-group">
    <label>Status</label>
    <select name="status" class="form-control" style="width: auto">
      ${form.toHTMLOptions("status")}
    </select>
  </div>
  <div>
    <input type="submit" value="Submit" class="btn btn-success">
    <a href="${config.url.base}dashboard/answers" class="btn btn-default">Dashboard</a>
  </div>
</div>
</form>
</@layout.defaultLayout>
