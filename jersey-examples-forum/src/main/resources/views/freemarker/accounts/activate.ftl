<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Account Registration">
<div class="alert alert-success">
  <p>Your account has been created successfully.</p>
  <p>Your email address is <code>${account.email?html}</code>.</p>
</div>
<div>
  <a href="${config.url.base}profile/edit" class="btn btn-default">Edit your profile</a>
</div>
</@layout.defaultLayout>
