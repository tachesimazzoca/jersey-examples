<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Account Registration">
<p>Your account has been created successfully.</p>
<p>Your email address is <code>${account.email?html}</code>.</p>
<ul>
  <li><a href="signin">Sign In</a></li>
</ul>
</@layout.defaultLayout>
