<#import "/_layouts/default.ftl" as layout>
<#import "/_elements/pagination.ftl" as p>
<@layout.defaultLayout "${question.subject}">
<div class="panel panel-default">
  <div class="panel-heading">
    <div class="btn-group">
      <#if (account?has_content)>
        <#if (questionInfo.starred)>
          <#assign vote="unstar">
        <#else>
          <#assign vote="star">
        </#if>
        <a href="${vote}?id=${question.id}" class="btn btn-sm btn-default">
          <span class="glyphicon glyphicon-star" title="Star"></span>
        </a>
      </#if>
      <div class="btn btn-sm btn-default">${questionInfo.numPoints}</div>
    </div>
    <strong>Question #${question.id}: </strong>
    <span>${author.nickname?html} posted at ${question.postedAt?string("yyyy-MM-dd HH:mm:ss")}</span>
  </div>
  <div class="panel-body clearfix">
    <p>${question.body?html?replace("\n", "<br>")}</p>
    <#if (account?has_content && account.id == question.authorId)>
    <div class="pull-right">
      <a href="${config.url.base}questions/edit?id=${question.id}&returnTo=%2fquestions%2f${question.id}" class="btn btn-default">Edit</a>
    </div>
    </#if>
  </div>
</div>
<p>
  <a href="${config.url.base}answers/edit?questionId=${question.id}" class="btn btn-primary">Post Your Answer</a>
</p>
<#if answers.results?has_content>
<hr>
<#if (answers.count > 1)>
<p>${answers.count} Answers</p>
</#if>
<#list answers.results as x>
<div class="panel panel-default">
  <div class="panel-heading">
    <strong>Answer #${x.id}: </strong>
    <span>${x.nickname?html} posted at ${x.postedAt?string("yyyy-MM-dd HH:mm:ss")}</span>
  </div>
  <div class="panel-body clearfix">
    <p>${x.body?html?replace("\n", "<br>")}</p>
    <div class="pull-right">
      <#if (account?has_content && account.id == question.authorId)>
      <div class="btn-group">
        <a href="${config.url.base}answers/up?id=${x.id}" class="btn btn-default"><span class="glyphicon glyphicon-arrow-up" title="Up"></span></a>
        <a href="${config.url.base}answers/down?id=${x.id}" class="btn btn-default"><span class="glyphicon glyphicon-arrow-down" title="Down"></span></a>
      </div>
      </#if>
      <#if (account?has_content && account.id == x.authorId)>
      <a href="${config.url.base}answers/edit?id=${x.id}" class="btn btn-default">Edit</a>
      </#if>
    </div>
  </div>
</div>
</#list>
</div>
<@p.defaultPagination answers></@p.defaultPagination>
</#if>

</@layout.defaultLayout>
