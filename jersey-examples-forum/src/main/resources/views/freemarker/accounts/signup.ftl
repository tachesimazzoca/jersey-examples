<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Account Registration">
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
<form action="signup" method="POST">
<dl>
  <dt>E-mail</dt>
  <dd>${form.toHTMLInput("text", "email")}</dd>
  <dt>Password</dt>
  <dd>${form.toHTMLInput("password", "password")}</dd>
  <dt>Re-type Password</dt>
  <dd>${form.toHTMLInput("password", "retypedPassword")}</dd>
</dl>
<div>
  <input type="submit" value="Submit">
</div>
</form>
</@layout.defaultLayout>
