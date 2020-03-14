<%
def categories = sample_chapters.collect { it.category } as Set
%>

<% include "header-docs.gsp" %>

	<% include "menu.gsp" %>
	<main class="main-content">
		<% include "docs_navigation.gsp" %>

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
<% include "footer-docs.gsp" %>
