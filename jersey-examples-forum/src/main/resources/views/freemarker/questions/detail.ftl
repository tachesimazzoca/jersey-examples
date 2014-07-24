<#import "/_layouts/default.ftl" as layout>
<@layout.defaultLayout "${question.subject}">
<p>${question.body?html}</p>
<dl>
  <dt>Author</dt>
  <dd>id:${author.id}</dd>
  <dt>Posted at</dt>
  <dd>${question.postedAt?string("yyyy-MM-dd hh:mm:ss")}</dd>
</dl>
<ul>
  <li><a href="${config.url.base}answers/edit?questionId=${question.id}">Your Answer</a></li>
  <li><a href="../questions">Listing Questions</a></li>
</ul>
</@layout.defaultLayout>
