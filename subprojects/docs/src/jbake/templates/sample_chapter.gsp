<%
	def permalink = content.uri.substring(0, content.uri.lastIndexOf('/'))
%>
<!DOCTYPE html>
<html lang="en" prefix="og: https://ogp.me/ns#">
<head>
	<% include "fragment-docs-header.gsp" %>
	<link href="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>css/docs-samples.css" rel="stylesheet">
	<meta name="twitter:card" content="player">
	<meta name="twitter:player" content="${config.site_host}/${permalink}/all-commands.embed.html">
	<meta name="twitter:image" content="${config.site_host}/${permalink}/all-commands.png">
	<meta name="twitter:player:width" content="1179">
	<meta name="twitter:player:height" content="792">
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
<% include "fragment-docs-footer.gsp" %>
</body>
</html>
