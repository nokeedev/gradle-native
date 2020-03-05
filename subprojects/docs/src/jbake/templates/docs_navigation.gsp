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
%>
<nav class="docs-navigation">
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
		</ul>
	</ul>
</nav>
