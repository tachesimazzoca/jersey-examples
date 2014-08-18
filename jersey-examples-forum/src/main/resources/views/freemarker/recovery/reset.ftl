<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Account Recovery">
<#if form.hasErrors()>
<div class="alert alert-danger">
<ul>
<#list form.errors as err>
  <li>${(err.message)?html}</li>
</#list>
</ul>
</div>
</#if>
<form action="reset" method="POST">
${form.toHTMLInput("hidden", "code")}
<div style="width: 400px;">
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
  <input type="submit" value="Update Password" class="btn btn-success">
</div>
</form>
</@layout.defaultLayout>
