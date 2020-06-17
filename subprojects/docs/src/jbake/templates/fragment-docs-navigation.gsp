<%
	def path='../'
	if (content.uri.endsWith('release-notes.html')) {
		path=''
	} else if (content.uri.contains('/samples/') && !content.uri.endsWith('/samples/index.html')) {
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

	def hasDsl = !content.uri.contains('0.1.0') && !content.uri.contains('0.2.0')
	def hasXcodeIde = !content.uri.contains('0.1.0') && !content.uri.contains('0.2.0')
	def hasObjCIosApplicationPlugin = !content.uri.contains('0.1.0') && !content.uri.contains('0.2.0')
	def hasBuildingJniLibrary = !content.uri.contains('0.1.0') && !content.uri.contains('0.2.0')
	def hasAnatomyNokeePlugins = !content.uri.contains('0.1.0') && !content.uri.contains('0.2.0')
	def hasBuildingNativeProjects = !content.uri.contains('0.1.0') && !content.uri.contains('0.2.0') && !content.uri.contains('0.3.0')
	def hasGradlePluginDevelopment = !content.uri.contains('0.1.0') && !content.uri.contains('0.2.0') && !content.uri.contains('0.3.0')
%>
<nav class="docs-navigation">
	<div class="breadcrumbs">${formatCrumbs(getBreadcrumbs())}</div>
	<input class="docs-navigation-hamburger" type="checkbox" id="docs-navigation-hamburger" />
	<label class="menu-icon" for="docs-navigation-hamburger"><span class="fa navicon"></span></label>
	<div class="navigation-items">
		<ul>
			<li><a ${anchorOf('manual/user-manual.html')}>Docs Home</a></li>
			<li><a ${anchorOf('samples/')}>Samples</a></li>
			<li><a ${anchorOf('release-notes.html')}>Release Notes</a></li>
			<% if (!content.uri.contains('0.1.0')) {%><li><a ${anchorOf('javadoc/index.html')}>Nokee Javadoc API</a></li><%}%>
			<% if (hasDsl) {%><li><a ${anchorOf('dsl/index.html')}>Nokee DSL Reference</a></li><%}%>
		</ul>
		<h3 id="user-manual">User Manual </h3>
		<ul>
			<li><a ${anchorOf('manual/getting-started.html')}>Getting Started</a></li>
			<% if (hasAnatomyNokeePlugins) {%><li><a ${anchorOf('manual/plugin-anatomy.html')}>Anatomy of a Nokee Plugin</a></li><%}%>
			<% if (hasBuildingJniLibrary) {%><li><a ${anchorOf('manual/building-jni-projects.html')}>Building JNI Projects</a></li><%}%>
			<% if (hasBuildingNativeProjects) {%><li><a ${anchorOf('manual/building-native-projects.html')}>Building Native Projects</a></li><%}%>
			<% if (hasXcodeIde) {%><li><a ${anchorOf('manual/developing-with-xcode-ide.html')}>Developing with Xcode IDE</a></li><%}%>
			<% if (hasGradlePluginDevelopment) {%><li><a ${anchorOf('manual/gradle-plugin-development.html')}>Gradle Plugin Development</a></li><%}%>
			<li><a ${anchorOf('manual/terminology.html')}>Terminology</a></li>
		</ul>
		<h3 id="reference">Reference</h3>
		<ul>
			<li><a ${anchorOf('manual/plugin-references.html')}>Nokee Plugins</a></li>
			<ul>
				<li><a ${anchorOf('manual/jni-library-plugin.html')}>JNI Library</a></li>
				<li><a ${anchorOf('manual/cpp-language-plugin.html')}>C++ Language</a></li>
				<li><a ${anchorOf('manual/c-language-plugin.html')}>C Language</a></li>
				<% if (!content.uri.contains('0.1.0')) {%><li><a ${anchorOf('manual/objective-c-language-plugin.html')}>Objective-C Language</a></li><%}%>
				<% if (!content.uri.contains('0.1.0')) {%><li><a ${anchorOf('manual/objective-cpp-language-plugin.html')}>Objective-C++ Language</a></li><%}%>
				<% if (hasObjCIosApplicationPlugin) {%><li><a ${anchorOf('manual/objective-c-ios-application-plugin.html')}>Objective-C iOS Application</a></li><%}%>
				<% if (hasXcodeIde) {%><li><a ${anchorOf('manual/xcode-ide-plugin.html')}>Xcode IDE</a></li><%}%>
			</ul>
		</ul>
	</div>
</nav>
