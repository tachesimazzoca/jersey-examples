<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Account Registration">
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
<form action="signup" method="POST">
<div style="width: 400px;">
  <div class="form-group">
    <label>E-mail</label>
    ${form.toHTMLInput("text", "email", "class=\"form-control\"")}
  </div>
  <div class="form-group">
    <label>Password</label>
    ${form.toHTMLInput("password", "password", "class=\"form-control\"")}
  </div>
  <div class="form-group">
    <label>Re-type Password</label>
    ${form.toHTMLInput("password", "retypedPassword", "class=\"form-control\"")}
  </div>
</div>
<div>
  <input type="submit" value="Sign Up" class="btn btn-success">
</div>
</form>
</@layout.defaultLayout>
