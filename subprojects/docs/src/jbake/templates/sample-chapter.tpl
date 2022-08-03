/*
	<!-- Disable for now -->
	<!--
	<meta name="twitter:player" content="${config.site_host}/${permalink}/all-commands.embed.html">
	<meta name="twitter:image" content="${config.site_host}/${permalink}/all-commands.png">
	<meta name="twitter:player:width" content="1179">
	<meta name="twitter:player:height" content="792">
	-->
 */

layout 'layout-docs.tpl', content: content, config: config,
	headContents: contents {
		link(rel: 'stylesheet', href: '/css/docs-samples.css')
	},
	bodyContents: contents {
		yieldUnescaped """
			<div class="download">
				<ul>
					<li>
						<p><a href="${content.archivebasename}-${content.archiveversion == null ? config.archiveversion : content.archiveversion}-groovy-dsl.zip"><span class="icon"><i class="fa fa-download"></i></span> Groovy DSL</a></p>
					</li>
					<li>
						<p><a href="${content.archivebasename}-${content.archiveversion == null ? config.archiveversion : content.archiveversion}-kotlin-dsl.zip"><span class="icon"><i class="fa fa-download"></i></span> Kotlin DSL</a></p>
					</li>
				</ul>
			</div>
"""
		yieldUnescaped content.body
	}
