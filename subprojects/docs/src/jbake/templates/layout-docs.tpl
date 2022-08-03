layout 'layout-page.tpl', content: content, config: config,
	multiLanguageSampleEnabled: true,
	headContents: contents {
		link(rel: 'stylesheet', href: '/css/docs-asciidoctor-docs-layout.css') newLine()
	},
	primaryNavigationContents: contents {
		layout 'fragment-docs-navigation.tpl', content: content
	},
	secondaryNavigationContents: contents {
		aside(class: 'secondary-navigation') {}
	}
