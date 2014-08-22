<#import "/_layouts/default.ftl" as layout>
<#import "/_elements/pagination.ftl" as p>
<@layout.defaultLayout "${question.subject}">
<div class="panel panel-default">
  <div class="panel-heading">
    <strong>Question #${question.id}: </strong>
    <span>${author.nickname?html} posted at ${question.postedAt?string("yyyy-MM-dd HH:mm:ss")}</span>
  </div>
  <div class="panel-body clearfix">
    <p>${question.body?html?replace("\n", "<br>")}</p>
    <div class="pull-right">
    <#if (account?has_content)>
      <#if (account.id == question.authorId)>
        <a href="${config.url.base}questions/edit?id=${question.id}" class="btn btn-default">Edit</a>
      <#else>
        <#if (questionInfo.starred)>
          <#assign _vote="unstar">
          <#assign _btnStyle="btn-warning">
        <#else>
          <#assign _vote="star">
          <#assign _btnStyle="btn-default">
        </#if>
      </#if>
      <div class="btn-group">
        <a href="${_vote}?id=${question.id}" class="btn btn-sm ${_btnStyle}">
          <span class="glyphicon glyphicon-star" title="Star"></span>
        </a>
        <div class="btn btn-sm btn-default">${questionInfo.numPoints}</div>
      </div>
    </#if>
    </div>
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
<#setting url_escaping_charset="UTF-8">
<#assign _returnTo='${"/questions/${question.id}?offset=${answers.offset}&limit=${answers.limit}"?url}'>
<#list answers.results as x>
<div class="panel panel-default">
  <div class="panel-heading">
    <strong>Answer #${x.id}: </strong>
    <span>${x.nickname?html} posted at ${x.postedAt?string("yyyy-MM-dd HH:mm:ss")}</span>
  </div>
  <div class="panel-body clearfix">
    <p>${x.body?html?replace("\n", "<br>")}</p>
    <div class="pull-right">
      <#if (account?has_content)>
        <#if (account.id == x.authorId)>
          <a href="${config.url.base}answers/edit?id=${x.id}" class="btn btn-default">Edit</a>
        <#else>
        <div class="btn-group">
          <a href="${config.url.base}answers/vote?id=${x.id}&point=1&returnTo=${_returnTo}" class="btn btn-default"><span class="glyphicon glyphicon-arrow-up" title="Up"></span></a>
          <span class="btn btn-default">${x.positivePoints}</span>
          <a href="${config.url.base}answers/vote?id=${x.id}&point=-1&returnTo=${_returnTo}" class="btn btn-default"><span class="glyphicon glyphicon-arrow-down" title="Down"></span></a>
          <span class="btn btn-default">${x.negativePoints * -1}</span>
        </div>
        </#if>
      </#if>
    </div>
  </div>
</div>
</#list>
</div>
<@p.defaultPagination answers "${config.url.base}questions/${question.id}"></@p.defaultPagination>
</#if>

</@layout.defaultLayout>
