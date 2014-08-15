<#import "/_layouts/default.ftl" as layout>
<#import "/_elements/pagination.ftl" as p>
<@layout.defaultLayout "Dashboard Answers">

<#if pagination.results?has_content>
<@p.defaultPagination pagination></@p.defaultPagination>
<table>
<tr>
  <th>ID</th>
  <th>Status</th>
  <th>Answer</th>
  <th>Posted at</th>
</tr>
<#list pagination.results as x>
<tr>
  <td>${(x.id)?html}</td>
  <td>${(x.status.label)?html}</td>
  <td>
    <p>${(x.body)?html}</p>
    <ul>
      <li><a href="${config.url.base}answers/edit?id=${x.id}">Edit</a></li>
      <li><a href="${config.url.base}answers/delete?id=${x.id}" onclick="return confirm('Are you sure to delete?')">Delete</a></li>
    </ul>
  </td>
  <td>${(x.postedAt)?html}</td>
</tr>
</#list>
</table>
</#if>
</@layout.defaultLayout>