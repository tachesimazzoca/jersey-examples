<#macro defaultLayout title="">
<html>
<head>
<#if title?has_content>
  <title>${title?html} | ${(html.title)?html}</title>
<#else>
  <title>${(html.title)?html}</title>
</#if>
</head>
<body>
<div id="wrapper">
  <div id="header">
    <h1>${(html.title)?html}</h1>
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
