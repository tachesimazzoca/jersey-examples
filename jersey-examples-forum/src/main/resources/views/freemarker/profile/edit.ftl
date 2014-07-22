<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Editing Profile">
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
<dl>
  <dt>E-mail</dt>
  ${form.toHTMLInput("text", "email")}
  <dt>Current Password</dt>
  ${form.toHTMLInput("password", "currentPassword")}
  <dt>New Password</dt>
  ${form.toHTMLInput("password", "password")}
  <dt>Re-type Password</dt>
  ${form.toHTMLInput("password", "retypedPassword")}
</dl>
<div>
  <input type="submit" value="Submit">
</div>
</form>
</@layout.defaultLayout>
