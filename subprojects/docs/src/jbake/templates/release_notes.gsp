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
			</div>
		</div>
		<aside class="secondary-navigation"></aside>
	</main>

<% include "footer-docs.gsp" %>
