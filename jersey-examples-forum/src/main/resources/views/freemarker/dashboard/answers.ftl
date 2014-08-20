<#import "/_layouts/default.ftl" as layout>
<#import "/_elements/pagination.ftl" as p>
<#include "/_elements/functions.ftl">
<@layout.defaultLayout "Answers">
<#if answers.results?has_content>
<table class="table">
<thead>
<tr>
  <th>#</th>
  <th>Status</th>
  <th colspan="2">Answer</th>
</tr>
</thead>
<tbody>
<#list answers.results as x>
<tr>
  <td>${(x.id)?html}</td>
  <td>${(x.status.label)?html}</td>
  <td>
    <div>${(truncate(x.body, 20))?html}</div>
    <div class="text-muted"><small>Posted at ${(x.postedAt)?string("yyyy-MM-dd HH:mm:ss")}</small></div>
  </td>
  <td class="text-right text-nowrap">
    <a href="${config.url.base}answers/edit?id=${x.id}" class="btn btn-default">Edit</a></li>
    <a href="${config.url.base}answers/delete?id=${x.id}" onclick="return confirm('Are you sure to delete?')" class="btn btn-danger">Delete</a>
  </td>
</tr>
</#list>
</tbody>
</table>
<@p.defaultPagination answers></@p.defaultPagination>
</#if>
</@layout.defaultLayout>
