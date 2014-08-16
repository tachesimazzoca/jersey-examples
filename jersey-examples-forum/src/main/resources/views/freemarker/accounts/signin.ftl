<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Sign In">
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
<div class="center-block" style="width: 400px">
<form action="signin" method="POST">
<div class="well">
  ${form.toHTMLInput("hidden", "url")}
  <div class="form-group">
    <label>E-mail</label>
    ${form.toHTMLInput("text", "email", "class=\"form-control\"")}
  </div>
  <div class="form-group">
    <label>Password</label>
    ${form.toHTMLInput("password", "password", "class=\"form-control\"")}
  </div>
</div><!--/.well-->
<div>
  <input type="submit" value="Sign In" class="btn btn-primary">
  &nbsp;<a href="${config.url.base}recovery/entry">Forgot Password</a>
</div>
</form>
<hr>
<p>If you don't have an account, <a href="${config.url.base}accounts/signup">create a new account</a>.</p>
</div>
</@layout.defaultLayout>
