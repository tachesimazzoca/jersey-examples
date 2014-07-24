<#macro defaultLayout pageTitle="">
<#if pageTitle?has_content>
  <#assign title="${pageTitle?html} | ${(config.html.title)!'Untitled'}">
<#else>
  <#assign title="${(config.html.title)!'Untitled'}">
</#if>
<html>
<meta charset="utf-8">
<head>
  <title>${title?html}</title>
</head>
<body>
<div id="wrapper">
  <div id="header">
	<#if pageTitle?has_content>
    <h1>${pageTitle?html}</h1>
	<#else>
    <h1>${((config.html.title)!"")?html}</h1>
	</#if>
  </div>
  <div id="content">
    <#nested/>
  </div>
  <div id="footer">
    <hr>
    <p>&copy; Copyright 2014 <a href="http://github.com/tachesimazzoca">tachesimazzoca</a>.</p>
  </div>
</div>
</body>
</html></#macro>
