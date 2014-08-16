<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Dashboard">

<div class="panel panel-default">
  <div class="panel-heading">
    <h2 class="panel-title">Profile</h2>
  </div>
  <div class="panel-body">
    <dl class="dl-horizontal">
      <dt>Email</dt>
      <dd>${(account.email)?html}</dd>
      <dt>Nickname</dt>
      <dd>${(account.nickname)?html}</dd>
    </dl>
    <div class="text-right">
      <a href="${config.url.base}profile/edit" class="btn btn-default">Edit</a>
    </div>
  </div>
</div>

<div>
<ul class="list-group">
   <li class="list-group-item"><a href="dashboard/questions">Questions</a></li>
   <li class="list-group-item"><a href="dashboard/answers">Answers</a></li>
</ul>
</div>

</@layout.defaultLayout>
