<#import "/_layouts/default.ftl" as layout>
<#import "/_elements/pagination.ftl" as p>
<@layout.defaultLayout "Listing Questions">
<ul>
   <li><a href="questions/edit">New Question</a></li>
</ul>
<#if pagination.results?has_content>
<@p.defaultPagination pagination></@p.defaultPagination>
<table>
<tr>
  <th>Subject</th>
  <th>Author</th>
  <th>Posted at</th>
</tr>
<#list pagination.results as x>
<tr>
  <td><a href="questions/${x.id}">${(x.subject)?html}</a></td>
  <#if x.nickname?has_content>
  <td>${(x.nickname)?html}</td>
  <#else>
  <td>user:${(x.authorId)?html}</td>
  </#if>
  <td>${(x.postedAt)?html}</td>
</tr>
</#list>
</table>
</#if>
</@layout.defaultLayout>