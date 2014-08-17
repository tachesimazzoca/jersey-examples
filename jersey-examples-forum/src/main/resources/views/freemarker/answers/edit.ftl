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
  <div class="form-group">
    <label>Status</label>
    <select name="status" class="form-control" style="width: auto">
      ${form.toHTMLOptions("status")}
    </select>
  </div>
  <div>
    <input type="submit" value="Submit" class="btn btn-success">
    <#if answer?has_content>
    <a href="cancel?id=${answer.id!""}" class="btn btn-default">Cancel</a>
    <#else>
    <a href="cancel" class="btn btn-default">Cancel</a>
    </#if>
  </div>
</div>
</form>
</@layout.defaultLayout>
