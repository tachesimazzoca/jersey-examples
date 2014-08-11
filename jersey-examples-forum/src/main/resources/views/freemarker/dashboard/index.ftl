<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Dashboard">

<h2>Profile</h2>
<dl>
  <dt>Email</dt>
  <dd>${(account.email)?html}</dd>
  <dt>Nickname</dt>
  <dd>${(account.nickname)?html}</dd>
</dl>

<h2>Posts</h2>
<ul>
   <li><a href="dashboard/questions">Questions</a></li>
   <li><a href="dashboard/answers">Answers</a></li>
</ul>

</@layout.defaultLayout>