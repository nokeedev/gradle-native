<%
	def path='../'
	if (content.uri.endsWith('release_notes.html')) {
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
		def kContentCrumb = crumb(content.title, content.uri)
		def kUserManualCrumb = crumb('User Manual', "${path}manual/user_manual.html")
		def kReferencePluginsCrumb = crumb('Reference Plugins', "${path}manual/plugin_references.html")
		def kSamplesCrumb = crumb('Samples', "${path}samples/index.html")
		def kReleaseNotesCrumb = crumb('Release Notes', "${path}release_notes.html")

		switch (content.type) {
			case 'reference_chapter': return [kUserManualCrumb, kReferencePluginsCrumb, kContentCrumb]
			case 'reference_index': return [kUserManualCrumb, kReferencePluginsCrumb]
			case 'sample_chapter': return [kSamplesCrumb, kContentCrumb]
			case 'sample_index': return [kSamplesCrumb]
			case 'release_notes': return [kReleaseNotesCrumb]
			case 'manual_chapter':
				if (content.uri.endsWith('user_manual.html')) {
					return [kUserManualCrumb]
				} else {
					return [kUserManualCrumb, kContentCrumb]
				}
			default:
				throw new UnsupportedOperationException("Unknown content type (${content.type})")
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
			<li><a ${anchorOf('manual/user_manual.html')}>Docs Home</a></li>
			<li><a ${anchorOf('samples/index.html')}>Samples</a></li>
			<li><a ${anchorOf('release_notes.html')}>Release Notes</a></li>
		</ul>
		<h3 id="user-manual">User Manual </h3>
		<ul>
			<li><a ${anchorOf('manual/getting_started.html')}>Getting Started</a></li>
			<li><a ${anchorOf('manual/terminology.html')}>Terminology</a></li>
		</ul>
		<h3 id="reference">Reference</h3>
		<ul>
			<li><a ${anchorOf('manual/plugin_references.html')}>Nokee Plugins</a></li>
			<ul>
				<li><a ${anchorOf('manual/jni_library_plugin.html')}>JNI Library Plugin</a></li>
				<li><a ${anchorOf('manual/cpp_language_plugin.html')}>C++ Language Plugin</a></li>
				<li><a ${anchorOf('manual/c_language_plugin.html')}>C Language Plugin</a></li>
			</ul>
		</ul>
	</div>
</nav>
