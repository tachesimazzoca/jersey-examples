<#macro defaultPagination pagination>
Total ${pagination.count}:&nbsp;
<#if pagination.hasPreviousOffset()>
<a href="${pagination.previousOffsetUrl}">&lt; Prev</a>
</#if>
<#if pagination.hasPreviousOffset() && pagination.hasNextOffset()>
&nbsp;|&nbsp;
</#if>
<#if pagination.hasNextOffset()>
<a href="${pagination.nextOffsetUrl}">Next &gt;</a>
</#if>
</#macro>
