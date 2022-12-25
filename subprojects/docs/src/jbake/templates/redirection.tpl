import java.nio.file.Paths

def redirecturl = Paths.get(content.uri).parent?.resolve(content.redirecturl) ?: content.redirecturl
layout 'layout-main.tpl', pageInfo: [url: "${config.site_host}/${redirecturl}", description: content.description, keywords: content.tags],
	openGraph: [url: "${config.site_host}/${redirecturl}", description: content.description, title: "Redirect to ${content.redirecturl}"],
	twitter: [card: 'summary', description: content.description, title: "Redirect to ${content.redirecturl}"],
	headContents: contents {
		meta('http-equiv': 'Refresh', content: "0; url=${content.redirecturl}")
	},
	title: 'Nokee Labs',
	encoding: content.encoding,
	logoUrl: config.site_host,
	bodyContents: contents {
		yieldUnescaped("""<p>Please follow <a href="${content.redirecturl}">this link</a>.</p>""")
	}
