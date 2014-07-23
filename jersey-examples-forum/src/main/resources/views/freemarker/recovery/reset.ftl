<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Account Recovery">
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
<form action="reset" method="POST">
${form.toHTMLInput("hidden", "code")}
<dl>
  <dt>Password</dt>
  ${form.toHTMLInput("password", "password")}
  <dt>Re-type Password</dt>
  ${form.toHTMLInput("password", "retypedPassword")}
</dl>
<div>
  <input type="submit" value="Submit">
</div>
</form>
</@layout.defaultLayout>
