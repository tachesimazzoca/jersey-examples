<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Editing Answer">

<#if form.hasErrors()>
<div class="alert alert-danger">
<ul>
<#list form.errors as err>
  <li>${(err.propertyPath)?html}: ${(err.message)?html}</li>
</#list>
</ul>
</div>
</#if>
<#if form.hasMessages()>
<div class="alert alert-danger">
<ul>
<#list form.messages as msg>
  <li>${msg?html}</li>
</#list>
</ul>
</div>
</#if>
<form action="edit" method="POST">
${form.toHTMLInput("hidden", "id")}
${form.toHTMLInput("hidden", "questionId")}
<div style="max-width: 640px">
  <div class="form-group">
    <label>Question</label>
    <div class="form-control-static"><a href="${config.url.base}questions/${question.id}">${(question.subject)?html}</a></div>
  </div>
  <div class="form-group">
    <label>Body</label>
    <textarea name="body" class="form-control">${(form.form.body)?html}</textarea>
  </div>
  <div>
    <input type="submit" value="Submit" class="btn btn-success">
    <a href="${config.url.base}questions/${question.id}" class="btn btn-default">Cancel</a>
  </div>
</div>
</form>
</@layout.defaultLayout>
