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
<div style="width: 400px;">
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
    <div><img src="" id="jsIconImg" style="display: none;"></div>
    <div>
      <input type="hidden" name="iconToken" id="jsIconTokenInput"> 
      <input type="file" name="file" id="jsIconFile"> 
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
      postImage: function(el) {
        var defer = $.Deferred();
        var fd = new FormData();
        fd.append('file', el.files[0]);
        $.ajax({
          url: '${config.url.base}api/uploader/images'
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

    $('#jsIconFile').on("change", function() {
      Uploader.postImage(this).then(
        // done
        function(data) {
          $('#jsIconTokenInput').attr('value', data);
          $('#jsIconImg').attr('src', '${config.url.base}api/uploader/images/' + data).show();
        }
        // fail
      , function(data) {
          $('#jsIconTokenInput').attr('value', '');
          $('#jsIconImg').hide();
        }
      );
    });
  });  
})(jQuery.noConflict());
</script>
</@layout.defaultLayout>
