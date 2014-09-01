<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "Editing Profile">
<#if flash?has_content>
<div class="alert alert-success" data-role="flash">Your profile has been saved successfully.</div>
</#if>
<#if form.hasErrors()>
<div class="alert alert-danger">
<ul>
<#list form.errors as err>
  <li>${(err.message)?html}</li>
</#list>
</ul>
</div>
</#if>
<form action="edit" method="POST">
<div style="max-width: 400px;">
  <div class="form-group">
    <label>E-mail</label>
    ${form.toHTMLInput("text", "email", "class=\"form-control\"")}
  </div>
  <div class="form-group">
    <label>Current Password</label>
    ${form.toHTMLInput("password", "currentPassword", "class=\"form-control\"")}
  </div>
  <div class="form-group">
    <label>Password</label>
    ${form.toHTMLInput("password", "password", "class=\"form-control\"")}
  </div>
  <div class="form-group">
    <label>Re-type Password</label>
    ${form.toHTMLInput("password", "retypedPassword", "class=\"form-control\"")}
  </div>
  <div class="form-group">
    <label>Nickname</label>
    ${form.toHTMLInput("text", "nickname", "class=\"form-control\"")}
  </div>
  <div class="form-group">
    <label>Icon</label>
    <div id="jsIconBlock">
      <#if icon>
      <div class="thumbnail">
        <img src="${config.url.base}api/upload/accounts/icon/${account.id}">
      </div>
      </#if>
    </div>
    <div id="jsTempIconBlock" style="display: none;">
      <div class="thumbnail clearfix">
        <div class="pull-right">
          <a href="#" onclick="return false;" id="jsRemoveTempIcon">
            <span class="glyphicon glyphicon-remove" style="color: #999"></span>
          </a>
        </div>
        <#if form.iconToken?has_content>
        <img src="${config.url.base}api/upload/tempfile/${form.iconToken}" id="jsTempIconImg">
        <#else>
        <img src="" id="jsTempIconImg">
        </#if>
      </div>
    </div>
    <div id="jsIconError" class="alert alert-danger" style="display: none;">
      <p>Uploading failed. Please check the following conditions.</p>
      <ul>
        <li>The supported file formats are (jpg|png|gif).</li>
        <li>The size of the file must be equal or less than 1MB.</li>
      </ul>
    </div>
    <div>
      <input type="hidden" name="iconToken" id="jsIconTokenInput">
      <input type="file" name="file" id="jsIconFileInput"> 
    </div>
  </div>
</div>
<div>
  <input type="submit" value="Update" class="btn btn-success">
</div>
</form>

<script type="text/javascript">
(function($) {
  $(function() {
    var Uploader = {
      postTempfile: function(el) {
        var defer = $.Deferred();
        var fd = new FormData();
        fd.append('file', el.files[0]);
        $.ajax({
          url: '${config.url.base}api/upload/tempfile'
        , data: fd
        , cache: false
        , contentType: false
        , processData: false
        , type: 'POST'
        , success: defer.resolve
        , error: defer.reject 
        });
        return defer.promise();
      }
    };

    var showTempIcon = function(filename) {
      $('#jsIconBlock').hide();
      $('#jsIconTokenInput').attr('value', filename);
      $('#jsTempIconImg').attr('src', '${config.url.base}api/upload/tempfile/' + filename);
      $('#jsTempIconBlock').show();
    };

    var hideTempIcon = function() {
      $('#jsTempIconBlock').hide();
      $('#jsIconTokenInput').attr('value', '');
      $('#jsIconBlock').show();
    };

    $('#jsIconFileInput').on("change", function() {
      Uploader.postTempfile(this).then(
        function(data) {
    	  $('#jsIconError').hide();
          showTempIcon(data);
        }
      , function(data) {
    	  hideTempIcon();
    	  $('#jsIconError').show();
        }
      );
    });

    $('#jsRemoveTempIcon').on("click", function() {
      hideTempIcon();
      var emptyIconFileInput = $('#jsIconFileInput').clone(true);
      $('#jsIconFileInput').replaceWith(emptyIconFileInput);
    });
  });  
})(jQuery.noConflict());
</script>
</@layout.defaultLayout>
