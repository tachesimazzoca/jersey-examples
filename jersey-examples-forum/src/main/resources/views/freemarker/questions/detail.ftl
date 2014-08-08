<#import "/_layouts/default.ftl" as layout>
<#import "/_elements/pagination.ftl" as p>
<@layout.defaultLayout "${question.subject}">

<ul>
  <li><a href="../questions">Listing Questions</a></li>
</ul>

<div>
<p>${question.body?html}</p>
<ul>
  <li>${author.nickname?html}</li>
  <li>${question.postedAt?string("yyyy-MM-dd hh:mm:ss")}</li>
  <li><a href="${config.url.base}questions/edit?id=${question.id}">Edit</a></li>
</ul>
</div>

<#if answers.results?has_content>
<div>
<@p.defaultPagination answers></@p.defaultPagination>
<#list answers.results as x>
<p>${(x.body)?html}</p>
<ul>
  <li>${(x.nickname)?html}</li>
  <li>${(x.postedAt)?html}</li>
  <li><a href="${config.url.base}answers/edit?id=${x.id}">Edit</a></li>
</ul>
</#list>
</div>
</#if>

<ul>
  <li><a href="${config.url.base}answers/edit?questionId=${question.id}">Your Answer</a></li>
</ul>
</@layout.defaultLayout>
