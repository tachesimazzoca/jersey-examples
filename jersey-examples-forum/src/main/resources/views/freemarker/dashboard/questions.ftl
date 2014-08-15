<#import "/_layouts/default.ftl" as layout>
<#import "/_elements/pagination.ftl" as p>
<@layout.defaultLayout "Dashboard Questions">
<ul>
   <li><a href="${config.url.base}questions/edit">New Question</a></li>
</ul>
<#if pagination.results?has_content>
<@p.defaultPagination pagination></@p.defaultPagination>
<table>
<tr>
  <th>ID</th>
  <th>Status</th>
  <th>Subject</th>
  <th>Posted at</th>
</tr>
<#list pagination.results as x>
<tr>
  <td>${(x.id)?html}</td>
  <td>${(x.status.label)?html}</td>
  <td><a href="${config.url.base}questions/${x.id}">${(x.subject)?html}</a>
    <ul>
      <li><a href="${config.url.base}questions/edit?id=${x.id}&return_to=%2fdashboard%2fquestions">Edit</a></li>
      <li><a href="${config.url.base}questions/delete?id=${x.id}" onclick="return confirm('Are you sure to delete?')">Delete</a></li>
    </ul>
  </td>
  <td>${(x.postedAt)?html}</td>
</tr>
</#list>
</table>
</#if>
</@layout.defaultLayout>
