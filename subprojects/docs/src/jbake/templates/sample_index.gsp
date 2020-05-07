<%
def categories = sample_chapters.collectEntries {
	switch (it.category) {
		case 'Java Native Interface (JNI)':
			return ['sec:samples-jni': it.category]
		case 'iOS':
			return ['sec:samples-ios': it.category]
		case 'Integrated Development Environment (IDE)':
			return ['sec:samples-ide': it.category]
		default:
			throw new IllegalArgumentException("Unknown category ${it.category}, please specify the id in 'sample_index.gsp'")
	}
}
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

				<%
				    // The following HTML structure mirror how adoc render each document section
					// It mostly allow anchors on h2
				    categories.each { id, category ->
				%>
					<div class="sec1">
						<h2 id="${id}">
							<a class="anchor" href="#${id}"></a>
							<a class="link" href="#${id}">${category}</a>
						</h2>
						<div class="sectionbody">
							<ul>
							<%sample_chapters.findAll { it.category == category }.each {sample ->%>
								<li><p><a href="${sample.permalink}/">${sample.title}</a>: ${sample.summary}</p></li>
							<%}%>
							</ul>
						</div>
					</div>
				<%}%>
			</div>
		</div>
		<aside class="secondary-navigation"></aside>
	</main>
<% include "fragment-docs-footer.gsp" %>
</body>
</html>
