<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Editing Answer">

<ul>
  <li><a href="${config.url.base}questions">Questions</a></li>
  <li><a href="${config.url.base}questions/${question.id}">${(question.subject)?html}</a></li>
</ul>

<#if form.hasErrors()>
<ul>
<#list form.errors as err>
  <li>${(err.propertyPath)?html}: ${(err.message)?html}</li>
</#list>
</ul>
</#if>
<#if form.hasMessages()>
<ul>
<#list form.messages as msg>
  <li>${msg?html}</li>
</#list>
</ul>
</#if>
<form action="edit" method="POST">
${form.toHTMLInput("hidden", "id")}
${form.toHTMLInput("hidden", "questionId")}
<dl>
  <dt>Body</dt>
  <dd><textarea name="body">${(form.form.body)?html}</textarea></dd>
</dl>
<div>
  <input type="submit" value="Submit">
</div>
</form>
</@layout.defaultLayout>
