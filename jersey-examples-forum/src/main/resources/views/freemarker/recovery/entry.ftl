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
<form action="entry" method="POST">
<dl>
  <dt>E-mail</dt>
  <dd>${form.toHTMLInput("text", "email")}</dd>
</dl>
<div>
  <input type="submit" value="Submit">
</div>
</form>
</@layout.defaultLayout>