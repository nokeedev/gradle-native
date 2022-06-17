<%
	def permalink = content.uri.substring(0, content.uri.lastIndexOf('/'))
%>
<!DOCTYPE html>
<html lang="en" prefix="og: https://ogp.me/ns#">
<head>
	<% include "fragment-docs-header.gsp" %>
	<link href="/css/docs-samples.css" rel="stylesheet">
	<!-- Disable for now -->
	<!--
	<meta name="twitter:player" content="${config.site_host}/${permalink}/all-commands.embed.html">
	<meta name="twitter:image" content="${config.site_host}/${permalink}/all-commands.png">
	<meta name="twitter:player:width" content="1179">
	<meta name="twitter:player:height" content="792">
	-->
</head>
<body onload="prettyPrint()">

	<% include "fragment-menu.gsp" %>
	<main class="main-content">
		<% include "fragment-docs-navigation.gsp" %>

		<div class="chapter">
			<div id="header">
				<h1>${content.title}</h1>
			</div>

			<div class="download">
				<ul>
					<li>
						<p><a href="${content.archivebasename}-${content.archiveversion == null ? config.archiveversion : content.archiveversion}-groovy-dsl.zip"><span class="icon"><i class="fa fa-download"></i></span> Groovy DSL</a></p>
					</li>
					<li>
						<p><a href="${content.archivebasename}-${content.archiveversion == null ? config.archiveversion : content.archiveversion}-kotlin-dsl.zip"><span class="icon"><i class="fa fa-download"></i></span> Kotlin DSL</a></p>
					</li>
				</ul>
			</div>

			<div id="content">
				${content.body}
			</div>
		</div>
		<aside class="secondary-navigation"></aside>
	</main>
<% include "fragment-docs-footer.gsp" %>
</body>
</html>
