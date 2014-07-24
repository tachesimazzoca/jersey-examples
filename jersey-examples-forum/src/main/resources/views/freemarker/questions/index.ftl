<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Listing Questions">
<ul>
   <li><a href="questions/edit">New Question</a></li>
</ul>
<#if pagination.results?has_content>
<table>
<tr>
  <th>Subject</th>
  <th>Posted at</th>
</tr>
<#list pagination.results as x>
<tr>
  <td><a href="questions/${x.id}">${(x.subject)?html}</a></td>
  <td>${(x.postedAt)?html}</td>
</tr>
</#list>
</table>
</#if>
</@layout.defaultLayout>