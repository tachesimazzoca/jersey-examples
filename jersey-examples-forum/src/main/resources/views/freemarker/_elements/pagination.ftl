<#macro defaultPagination pagination baseuri="">
<#if baseuri?contains("?")>
<#assign _baseuri="${baseuri}&">
<#else>
<#assign _baseuri="${baseuri}?">
</#if>
<#if (pagination.count > pagination.limit)>
<ul class="pager">
  <#if ((pagination.offset - pagination.limit) >= 0)>
  <li><a href="${_baseuri}offset=${pagination.offset - pagination.limit}&limit=${pagination.limit}">Prev</a></li>
  <#else>
  <li class="disabled"><a href="#">Prev</a></li>
  </#if>
  <#if ((pagination.offset + pagination.limit) < pagination.count)>
  <li><a href="${_baseuri}offset=${pagination.offset + pagination.limit}&limit=${pagination.limit}">Next</a></li>
  <#else>
  <li class="disabled"><a href="#">Next</a></li>
  </#if>
</ul>
</#if>
</#macro>
