<#import "/_layouts/default.ftl" as layout>
<#import "/_elements/pagination.ftl" as p>
<@layout.defaultLayout "Questions">
<p>
   <a href="questions/edit" class="btn btn-default">New Question</a>
</p>
<#if questions.results?has_content>
<hr>
<table class="table">
<#if (questions.count > 1)>
<p>${questions.count} Questions</p>
</#if>
<thead>
<tr>
  <th>#</th>
  <th>Subject</th>
  <th><span class="glyphicon glyphicon-pencil" title="answers"></span></th>
  <th><span class="glyphicon glyphicon-star" title="star"></span></th>
</tr>
</thead>
<tbody>
<#list questions.results as x>
<#if x.nickname?has_content>
<#assign author="${x.nickname}">
<#else>
<#assign author="user:${x.authorId}">
</#if>
<tr>
  <td>${x.id}</td>
  <td>
    <div><a href="questions/${x.id}">${(x.subject)?html}</a></div>
    <div>${author?html} posted at ${x.postedAt?string("yyyy-MM-dd HH:mm:ss")}</div>
  </td>
  <td>${x.numAnswers}</td>
  <td>${x.positivePoints}</td>
</tr>
</#list>
</tbody>
</table>
<@p.defaultPagination questions></@p.defaultPagination>
</#if>
</@layout.defaultLayout>