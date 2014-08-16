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
  <link href="${config.url.assets}lib/bootstrap/css/bootstrap.min.css" rel="stylesheet">
  <link href="${config.url.assets}css/bootstrap-override.css" rel="stylesheet">
  <script src="${config.url.assets}lib/jquery/jquery.min.js"></script>
  <script src="${config.url.assets}lib/bootstrap/js/bootstrap.min.js"></script>
</head>
<body>
<div id="wrapper">
  <div id="header">
    <div class="navbar navbar-default" role="navigation">
      <div class="container-fluid">
      <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#jsNavbarCollapse-1">
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="${config.url.home}">${(config.html.title)?html}</a>
      </div>
      <div class="collapse navbar-collapse" id="jsNavbarCollapse-1">
        <ul class="nav navbar-nav">
          <li><a href="${config.url.base}questions">Questions</a></li>
          <li><a href="${config.url.base}dashboard">Dashboard</a></li>
        </ul>
      </div>
      </div>
    </div>
  </div>
  <div id="content">
    <div class="container-fluid">
      <div class="row-fluid">
      <h1 class="page-header">
	    <#if pageTitle?has_content>
        ${pageTitle?html}
	    <#else>
        ${(config.html.title)?html}
	    </#if>
      </h1>
      </div>
      <div class="row-fluid">
      <#nested/>
      </div>
    </div>
  </div>
  <div id="footer">
    <hr>
    <div class="container-fluid">
      <div class="row-fluid">
        <p>&copy; Copyright 2014 <a href="http://github.com/tachesimazzoca">tachesimazzoca</a>.</p>
      </div>
    </div>
  </div>
</div>
</body>
</html></#macro>
