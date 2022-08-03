layout 'layout-main.tpl', pageInfo: [url: "${config.site_host}/${content.redirecturl}", description: content.description, keywords: content.tags],
	headContents: contents {
		meta('http-equiv': 'Refresh', content: "0; url=${content.redirecturl}")
	},
	title: 'Nokee Labs',
	encoding: content.encoding,
	logoUrl: config.site_host,
	bodyContents: contents {
		yieldUnescaped("""<p>Please follow <a href="${content.redirecturl}">this link</a>.</p>""")
	}
