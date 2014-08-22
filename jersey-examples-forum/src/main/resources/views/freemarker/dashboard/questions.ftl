<#import "/_layouts/default.ftl" as layout>
<#import "/_elements/pagination.ftl" as p>
<@layout.defaultLayout "Questions">
<p>
   <a href="${config.url.base}questions/edit" class="btn btn-default">New Question</a>
</p>
<#if questions.results?has_content>
<table class="table">
<thead>
<tr>
  <th>#</th>
  <th>Status</th>
  <th colspan="2">Subject</th>
</tr>
</thead>
<tbody>
<#list questions.results as x>
<tr>
  <td>${(x.id)?html}</td>
  <td>${(x.status.label)?html}</td>
  <td>
    <div><a href="${config.url.base}questions/${x.id}">${(x.subject)?html}</a></div>
    <div class="text-muted"><small>Posted at ${(x.postedAt)?string("yyyy-MM-dd HH:mm:ss")}</small></div>
  </td>
  <td class="text-right">
    <a href="${config.url.base}questions/edit?id=${x.id}" class="btn btn-default">Edit</a></li>
    <a href="${config.url.base}questions/delete?id=${x.id}" onclick="return confirm('Are you sure to delete?')" class="btn btn-danger">Delete</a>
  </td>
</tr>
</#list>
</tbody>
</table>
<@p.defaultPagination questions "${config.url.base}dashboard/questions"></@p.defaultPagination>
</#if>
</@layout.defaultLayout>
