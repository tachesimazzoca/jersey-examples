<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Sign In">
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
<form action="signin" method="POST">
${form.toHTMLInput("hidden", "url")}
<dl>
  <dt>E-mail</dt>
  ${form.toHTMLInput("text", "email")}
  <dt>Password</dt>
  ${form.toHTMLInput("password", "password")}
</dl>
<div>
  <input type="submit" value="Sign In">
</div>
</form>
</@layout.defaultLayout>
