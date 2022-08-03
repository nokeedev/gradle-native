layout 'layout-page.tpl', content: content, config: config,
	components: [
		[stylesheetUrl: '/component-multi-language-sample.css', scriptUrl: '/component-multi-language-sample.js'],
		[scriptUrl: '/js/active-link.js'],
	],
	headContents: contents {
		link(rel: 'stylesheet', href: '/css/docs-asciidoctor-docs-layout.css') newLine()
	},
	primaryNavigationContents: contents {
		layout 'fragment-docs-navigation.tpl', content: content
	},
	secondaryNavigationContents: contents {
		aside(class: 'secondary-navigation') {}
	}
