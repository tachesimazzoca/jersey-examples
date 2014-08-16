<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Account Recovery">
<div class="alert alert-success">
  <p>Your new password has been updated successfully.</p>
</div>
<div>
  <a href="${config.url.base}accounts/signin" class="btn btn-default">Sign In</a>
</div>
</@layout.defaultLayout>
