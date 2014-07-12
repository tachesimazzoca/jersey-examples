<#macro defaultLayout pageTitle="">
<#if pageTitle?has_content>
  <#assign title="${pageTitle?html} | ${(config.html.title)!'Untitled'}">
<#else>
  <#assign title="${(config.html.title)!'Untitled'}">
</#if>
<html>
<head>
  <title>${title?html}</title>
</head>
<body>
<div id="wrapper">
  <div id="header">
    <h1>${title?html}</h1>
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
