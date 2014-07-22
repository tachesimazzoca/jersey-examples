<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Editing Profile">
<p>Your new e-mail address has been updated successfully.</p>
<p>Your email address is <code>${account.email?html}</code>.</p>
<ul>
  <li><a href="edit">Editing Profile</a></li>
</ul>
</@layout.defaultLayout>
