<% include "header.gsp" %>

	<% include "menu.gsp" %>
	<main class="main-container">
		<% include "docs_navigation.gsp" %>

		<div class="chapter">
			<div id="header">
				<h1>${content.title}</h1>
			</div>

			<div class="download">
				<ul>
					<li>
						<p><a href="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>zips/${content.archivebasename}-${content.version}-groovy-dsl.zip"><span class="icon"><i class="fa fa-download"></i></span> Groovy DSL</a></p>
					</li>
					<li>
						<p><a href="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>zips/${content.archivebasename}-${content.version}-kotlin-dsl.zip"><span class="icon"><i class="fa fa-download"></i></span> Kotlin DSL</a></p>
					</li>
				</ul>
			</div>

			<div id="content">
				${content.body}
			</div>
		</div>
		<aside class="secondary-navigation"></aside>
	</main>
<% include "footer.gsp" %>
