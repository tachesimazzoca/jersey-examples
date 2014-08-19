<#import "/_layouts/default.ftl" as layout>
<#import "/_elements/pagination.ftl" as p>
<@layout.defaultLayout "Questions">
<p>
   <a href="questions/edit" class="btn btn-default">New Question</a>
</p>
<#if pagination.results?has_content>
<hr>
<table class="table">
<#if (pagination.count > 1)>
<p>${pagination.count} Questions</p>
</#if>
<thead>
<tr>
  <th>#</th>
  <th>Subject</th>
  <th><span class="glyphicon glyphicon-pencil" title="answers"></span></th>
  <th><span class="glyphicon glyphicon-arrow-up" title="up"></span></th>
  <th><span class="glyphicon glyphicon-arrow-down" title="down"></span></th>
</tr>
</thead>
<tbody>
<#list pagination.results as x>
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
  <td>${x.negativePoints}</td>
</tr>
</#list>
</tbody>
</table>
<@p.defaultPagination pagination></@p.defaultPagination>
</#if>
</@layout.defaultLayout>