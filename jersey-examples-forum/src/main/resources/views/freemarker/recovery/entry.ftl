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
<form action="entry" method="POST">
<div style="width: 400px;">
  <div class="form-group">
    <label>E-mail</label>
    ${form.toHTMLInput("text", "email", "class=\"form-control\"")}
  </div>
</div>
<div>
  <input type="submit" value="Request Recovery" class="btn btn-primary">
</div>
</form>
</@layout.defaultLayout>
