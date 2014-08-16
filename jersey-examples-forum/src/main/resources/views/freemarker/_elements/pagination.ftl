<#macro defaultPagination pagination>
<#if (pagination.count > pagination.limit)>
<ul class="pager">
  <#if pagination.hasPreviousOffset()>
  <li><a href="${pagination.previousOffsetUrl}">Prev</a></li>
  <#else>
  <li class="disabled"><a href="javascript:void();">Prev</a></li>
  </#if>
  <#if pagination.hasNextOffset()>
  <li><a href="${pagination.nextOffsetUrl}">Next</a></li>
  <#else>
  <li class="disabled"><a href="javascript:void();">Next</a></li>
  </#if>
</ul>
</#if>
</#macro>
