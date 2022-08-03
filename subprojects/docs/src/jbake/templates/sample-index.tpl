def categories = sample_chapters.collectEntries {
	switch (it.category) {
		case 'Java Native Interface (JNI)':
			return ['sec:samples-jni': it.category]
		case 'iOS':
			return ['sec:samples-ios': it.category]
		case 'Integrated Development Environment (IDE)':
			return ['sec:samples-ide': it.category]
		case 'Native Development':
			return ['sec:samples-native': it.category]
		case 'Gradle Plugin Development':
			return ['sec:samples-gradle-dev': it.category]
		default:
			throw new IllegalArgumentException("Unknown category ${it.category}, please specify the id in 'sample_index.gsp'")
	}
}

layout 'layout-docs.tpl', content: content, config: config,
	bodyContents: contents {
		yieldUnescaped(content.body)

		// The following HTML structure mirror how adoc render each document section
		// It mostly allow anchors on h2
		categories.forEach { id, category ->
			yieldUnescaped """
<div class="sec1">
<h2 id="${id}">
<a class="anchor" href="#${id}"></a>
			<a class="link" href="#${id}">${category}</a>
</h2>
		<div class="sectionbody">
			<ul>
"""
			sample_chapters.findAll { it.category == category }.each { sample ->
				yieldUnescaped """
				<li><p><a href="${sample.permalink}/">${sample.title}</a>: ${sample.summary}</p></li>
"""
			}
			yieldUnescaped """
</ul>
		</div>
</div>
"""
		}
	}
