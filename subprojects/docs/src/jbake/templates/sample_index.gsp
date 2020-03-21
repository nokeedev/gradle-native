<%
def categories = sample_chapters.collect { it.category } as Set
%>

<!DOCTYPE html>
<html lang="en" prefix="og: https://ogp.me/ns#">
<head>
	<% include "fragment-docs-header.gsp" %>
</head>
<body onload="prettyPrint()">

	<% include "fragment-menu.gsp" %>
	<main class="main-content">
		<% include "fragment-docs-navigation.gsp" %>

		<div class="chapter">
			<div id="header">
				<h1>${content.title}</h1>
			</div>

			<div id="content">
				${content.body}
				<%categories.each { category ->%>
					<h2>${category}</h2>
					<ul>
					<%sample_chapters.findAll { it.category == category }.each {sample ->%>
						<li><p><a href="${sample.permalink}/">${sample.title}</a>: ${sample.summary}</p></li>
					<%}%>
					</ul>
				<%}%>
			</div>
		</div>
		<aside class="secondary-navigation"></aside>
	</main>
<% include "fragment-docs-footer.gsp" %>
</body>
</html>
