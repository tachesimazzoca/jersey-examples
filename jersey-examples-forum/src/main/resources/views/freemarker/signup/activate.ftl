<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Account Registration">
<p>Your account has been created successfully.</p>
<p>Your email address is <code>${user.email?html}</code>.</p>
<ul>
  <li><a href="${config.url.base}profile/edit">Profile</a></li>
</ul>
</@layout.defaultLayout>
