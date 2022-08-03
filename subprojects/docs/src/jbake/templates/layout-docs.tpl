def bodyContents = bodyContents
if (!bodyContents) {
	bodyContents = contents { yieldUnescaped(content.body) }
}

layout 'layout-page.tpl', content: content, config: config,
	components: [
		[stylesheetUrl: '/component-multi-language-sample.css', scriptUrl: '/component-multi-language-sample.js'],
		[scriptUrl: '/js/active-link.js'],
	],
	headContents: contents {
		link(rel: 'stylesheet', href: '/css/docs-asciidoctor-docs-layout.css') newLine()
		if (headContents) headContents()
	},
	bodyContents: contents {
		layout 'fragment-menu-content.tpl',
			leftNavigationContents: contents {
				layout 'fragment-docs-navigation.tpl', content: content
			},
			bodyContents: contents {
				layout 'fragment-chapter.tpl',
					headerContents: contents { h1(content.title) },
					bodyContents: bodyContents
			}

		layout 'fragment-copyright.tpl', ignored: false
	}
