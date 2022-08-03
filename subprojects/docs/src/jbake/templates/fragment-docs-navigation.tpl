assert content : "Please specify a content"

def path='../'
if (content.uri.endsWith('release-notes.html')) {
	path=''
} else if (content.uri.startsWith('samples/') && !content.uri.equals('samples/index.html')) {
	path = '../../'
}

def anchorOf = { String href ->
	if (content.uri.endsWith(href)) {
		return "href=\"${path}${href}\" class=\"active\""
	}
	return "href=\"${path}${href}\""
}

def crumb = { String anchorName, String uri ->
	return [
		title: anchorName,
		href: uri,
	]
}

def getBreadcrumbs = {
	def kContentCrumb = crumb(content.title, "/${content.uri}")
	def kUserManualCrumb = crumb('User Manual', "${path}manual/user-manual.html")
	def kReferencePluginsCrumb = crumb('Reference Plugins', "${path}manual/plugin-references.html")
	def kSamplesCrumb = crumb('Samples', "${path}samples/")
	def kReleaseNotesCrumb = crumb('Release Notes', "${path}release-notes.html")

	switch (content.type) {
		case 'reference_chapter': return [kUserManualCrumb, kReferencePluginsCrumb, kContentCrumb]
		case 'reference_index': return [kUserManualCrumb, kReferencePluginsCrumb]
		case 'sample_chapter': return [kSamplesCrumb, kContentCrumb]
		case 'sample_index': return [kSamplesCrumb]
		case 'release_notes': return [kReleaseNotesCrumb]
		case 'manual_chapter':
			if (content.uri.endsWith('user-manual.html')) {
				return [kUserManualCrumb]
			} else {
				return [kUserManualCrumb, kContentCrumb]
			}
		default:
			throw new UnsupportedOperationException("[fragment-docs-navigation.gsp] Unknown content type (${content.type})")
	}
}

def formatCrumbs = { List<Map> breadcrumbs ->
	return "<ul>${breadcrumbs.collect({ '<li><a href="' + it.href + '">' + it.title + '</a></li>' }).join('')}</ul>"
}

yieldUnescaped """
<nav class="docs-navigation">
	<div class="breadcrumbs">${formatCrumbs(getBreadcrumbs())}</div>
	<input class="docs-navigation-hamburger" type="checkbox" id="docs-navigation-hamburger" />
	<label class="menu-icon" for="docs-navigation-hamburger"><span class="fa navicon"></span></label>
	<div class="navigation-items">
		<ul>
			<li><a ${anchorOf('manual/user-manual.html')}>Docs Home</a></li>
			<li><a ${anchorOf('samples/')}>Samples</a></li>
			<li><a ${anchorOf('release-notes.html')}>Release Notes</a></li>
			<li><a ${anchorOf('javadoc/index.html')}>Nokee Javadoc API</a></li>
			<li><a ${anchorOf('dsl/index.html')}>Nokee DSL Reference</a></li>
		</ul>
		<h3 id="user-manual">User Manual </h3>
		<ul>
			<li><a ${anchorOf('manual/getting-started.html')}>Getting Started</a></li>
			<li><a ${anchorOf('manual/plugin-anatomy.html')}>Anatomy of a Nokee Plugin</a></li>
			<li><a ${anchorOf('manual/building-jni-projects.html')}>Building JNI Projects</a></li>
			<li><a ${anchorOf('manual/building-native-projects.html')}>Building Native Projects</a></li>
			<li><a ${anchorOf('manual/developing-with-xcode-ide.html')}>Developing with Xcode IDE</a></li>
			<li><a ${anchorOf('manual/gradle-plugin-development.html')}>Gradle Plugin Development</a></li>
			<li><a ${anchorOf('manual/terminology.html')}>Terminology</a></li>
		</ul>
		<h3 id="reference">Reference</h3>
		<ul>
			<li><a ${anchorOf('manual/plugin-references.html')}>Nokee Plugins</a></li>
			<ul>
				<li><a ${anchorOf('manual/plugin-references.html#sec:plugin-reference-native-development')}>Native Development</a></li>
				<li><a ${anchorOf('manual/plugin-references.html#sec:plugin-reference-ios')}>iOS Development</a></li>
				<li><a ${anchorOf('manual/plugin-references.html#sec:plugin-reference-jvm')}>JNI Development</a></li>
				<li><a ${anchorOf('manual/xcode-ide-plugin.html')}>Xcode IDE</a></li>
				<li><a ${anchorOf('manual/plugin-references.html#sec:plugin-reference-gradledev')}>Gradle Plugin Development</a></li>
			</ul>
		</ul>
	</div>
</nav>
"""
