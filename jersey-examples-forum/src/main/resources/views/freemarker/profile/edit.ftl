<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Editing Profile">
<#if flash?has_content>
<div class="alert alert-success" data-role="flash">Your profile has been saved successfully.</div>
</#if>
<#if form.hasErrors()>
<div class="alert alert-danger">
<ul>
<#list form.errors as err>
  <li>${(err.message)?html}</li>
</#list>
</ul>
</div>
</#if>
<form action="edit" method="POST">
<div style="width: 400px;">
  <div class="form-group">
    <label>E-mail</label>
    ${form.toHTMLInput("text", "email", "class=\"form-control\"")}
  </div>
  <div class="form-group">
    <label>Current Password</label>
    ${form.toHTMLInput("password", "currentPassword", "class=\"form-control\"")}
  </div>
  <div class="form-group">
    <label>Password</label>
    ${form.toHTMLInput("password", "password", "class=\"form-control\"")}
  </div>
  <div class="form-group">
    <label>Re-type Password</label>
    ${form.toHTMLInput("password", "retypedPassword", "class=\"form-control\"")}
  </div>
  <div class="form-group">
    <label>Nickname</label>
    ${form.toHTMLInput("text", "nickname", "class=\"form-control\"")}
  </div>
</div>
<div>
  <input type="submit" value="Update" class="btn btn-success">
</div>
</form>
</@layout.defaultLayout>
