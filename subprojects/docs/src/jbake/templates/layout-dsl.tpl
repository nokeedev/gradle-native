layout 'layout-page.tpl', content: content, config: config,
	components: [
		[stylesheetUrl: '/component-multi-language-sample.css', scriptUrl: '/component-multi-language-sample.js'],
		[scriptUrl: '/js/active-link.js'],
	],
	headContents: contents {
		link(rel: 'stylesheet', href: '/css/docs-asciidoctor-docs-layout.css') newLine()
	},
	bodyContents: contents {
		layout 'fragment-menu-content.tpl',
			leftNavigationContents: contents {
				layout 'fragment-dsl-navigation.tpl', content: content,
					dsl_chapters: dsl_chapters
			},
			bodyContents: contents {
				layout 'fragment-chapter.tpl',
					headerContents: contents { h1(content.title) },
					bodyContents: contents { yieldUnescaped(content.body) }
			}

		layout 'fragment-copyright.tpl', ignored: false
	}
