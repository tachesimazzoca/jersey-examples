<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Editing Question">

<ul>
  <li><a href="${config.url.base}questions">Listing Questions</a></li>
  <#if question?has_content>
  <li><a href="${config.url.base}questions/${question.id}">${(question.subject)?html}</a></li>
  </#if>
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
<dl>
  <dt>Subject</dt>
  <dd>${form.toHTMLInput("text", "subject")}</dd>
  <dt>Body</dt>
  <dd><textarea name="body">${(form.form.body)?html}</textarea></dd>
  <dt>Status</dt>
  <dd>
    <select name="status">
      ${form.toHTMLOptions("status")}
    </select>
  </dd>
</dl>
<div>
  <input type="submit" value="Submit">
</div>
</form>
</@layout.defaultLayout>
