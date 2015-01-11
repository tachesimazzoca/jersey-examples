<#macro defaultLayout pageTitle="">
<#if pageTitle?has_content>
  <#assign title="${pageTitle} | ${config.html.title}">
<#else>
  <#assign title="${config.html.title}">
</#if>
<!DOCTYPE html>
<html>
<meta charset="utf-8">
<head>
  <title>${title?html}</title>
</head>
<body>
<h1><#if pageTitle?has_content>${pageTitle?html}<#else>${(config.html.title)?html}</#if></h1>
<#nested/>
</body>
</html></#macro>
