layout 'layout-page.tpl', content: content, config: config,
	multiLanguageSampleEnabled: true,
	headContents: contents {
		link(rel: 'stylesheet', href: '/css/docs-asciidoctor-docs-layout.css') newLine()
	},
	footContents: contents {
		script(src: '/js/active-link.js') {}
	},
	primaryNavigationContents: contents {
		layout 'fragment-dsl-navigation.tpl', content: content,
			dsl_chapters: dsl_chapters
	},
	secondaryNavigationContents: contents {
		aside(class: 'secondary-navigation') {}
	}