<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Editing Profile">
<div class="alert alert-success">
  <p>Your account has been created successfully.</p>
  <p>Your email address is <code>${account.email?html}</code>.</p>
</div>
<div>
  <a href="${config.url.base}dashboard" class="btn btn-default">Dashboard</a>
</div>
</@layout.defaultLayout>
