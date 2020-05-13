<%
	def anchorOf = { String href ->
		if (content.uri.endsWith(href)) {
			return "href=\"${href}\" class=\"active\""
		}
		return "href=\"${href}\""
	}

	def crumb = { String anchorName, String uri ->
		return [
			title: anchorName,
			href: uri,
		]
	}

	def getBreadcrumbs = {
		def kContentCrumb = crumb(content.title, "/${content.uri}")
		def kDslCrumb = crumb('Domain Specific Language', '../dsl/')

		switch (content.type) {
			case 'dsl_chapter': return [kDslCrumb, kContentCrumb]
			case 'dsl_index': return [kDslCrumb]
			default:
				throw new UnsupportedOperationException("[fragment-dsl-navigation.gsp] Unknown content type (${content.type})")
		}
	}

	def formatCrumbs = { List<Map> breadcrumbs ->
		return "<ul>${breadcrumbs.collect({ '<li><a href="' + it.href + '">' + it.title + '</a></li>' }).join('')}</ul>"
	}
%>
<nav class="docs-navigation">
	<div class="breadcrumbs">${formatCrumbs(getBreadcrumbs())}</div>
	<input class="docs-navigation-hamburger" type="checkbox" id="docs-navigation-hamburger" />
	<label class="menu-icon" for="docs-navigation-hamburger"><span class="fa navicon"></span></label>
	<div class="navigation-items">
		<ul>
			<li><a ${anchorOf('../manual/user-manual.html')}>User Manual Home</a></li>
			<li><a ${anchorOf('../dsl/index.html')}>DSL Reference Home</a></li>
			<li><a ${anchorOf('../release-notes.html')}>Release Notes</a></li>
		</ul>
		<h3 id="core-types">Core Types</h3>
		<ul>
			<%
			    println dsl_chapters.collect { dsl ->
					def typePath = "/${dsl.uri}"
					def typeName = dsl.uri.substring(dsl.uri.lastIndexOf('/') + 1) - '.html'
					typeName = typeName.substring(typeName.lastIndexOf('.') + 1)
					return [path: typePath, name: typeName]
				}.sort {a, b -> a.name <=> b.name}.collect { o ->
					return "<li><a ${anchorOf(o.path)}>${o.name}</a></li>"
				}.join('\n')
			%>
		</ul>
	</div>
</nav>
