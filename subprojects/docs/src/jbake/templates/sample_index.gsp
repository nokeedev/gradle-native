<% include "header.gsp" %>

	<% include "menu.gsp" %>
	<div class="container">

		<div class="page-header">
			<h1>${content.title}</h1>
		</div>

		<p>${content.body}</p>

		<%sample_chapters.each {sample ->%>
		<a href="${sample.permalink}/"><h2>${sample.title}</h2></a>
		<%}%>

	</div>

<% include "footer.gsp" %>
