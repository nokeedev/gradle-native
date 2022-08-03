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

def dslByCategory = new LinkedHashMap()
// Force an ordering for some types
['Core types', 'JNI types', 'Native types', 'C types', 'C++ types', 'Objective-C types', 'Objective-C++ types', 'Swift types', 'Xcode IDE types'].each { dslByCategory.put(it, [])}
dsl_chapters.each {
	def category = it.category == null ? 'Core types' : it.category
	dslByCategory.putIfAbsent(category, [])
	dslByCategory.get(category).add(it)
}

yieldUnescaped """
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
"""
		dslByCategory.each { category, dslChapters ->
			if (!dslChapters.empty) {
				yieldUnescaped """
		<h3 id="${category.toLowerCase().replace(' ', '-')}">${category}</h3>
		<ul>
"""
				dslChapters.collect { dslChapter ->
					def typePath = dslChapter.uri.substring(dslChapter.uri.lastIndexOf('/') + 1)
					def typeName = typePath - '.html'
					typeName = typeName.substring(typeName.lastIndexOf('.') + 1)
					return [path: typePath, name: typeName]
				}.sort { a, b -> a.name <=> b.name }.each {
					def typePath = it.path
					def typeName = it.name
					yieldUnescaped """
			<li><a ${anchorOf(typePath)}>${typeName}</a></li>
"""
				}
				yieldUnescaped """
		</ul>
"""
			}
		}
	yieldUnescaped """
	</div>
</nav>
"""
