<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<%if (content.description) {%><meta name="description" content="${content.description}"><%}%>
<meta name="author" content="@nokeedev">
<%if (content.tags) {%><meta name="keywords" content="${content.tags.join(', ')}"><%}%>
<% include 'fragment-meta-canonical.gsp' %>

<%
	def crumb = { String anchorName, String uri ->
		if (uri.endsWith('/index.html')) {
			uri = uri.substring(0, uri.lastIndexOf('/') + 1)
		}
		return [
			title: anchorName,
			href: uri,
		]
	}

	def m = (content.uri =~ /docs\/(current|nightly|\d+\.\d+\.\d+)\/.+/)
	def version = 'current'
	if (m.matches()) {
		version = m.group(1)
	}

	def getBreadcrumbs = {
		def kBlogCrumb = crumb('Blog', "${config.site_host}/blog/")
		def kContentCrumb = crumb(content.title, "${config.site_host}/${content.uri}")
		def kUserManualCrumb = crumb('User Manual', "${config.site_host}/docs/${version}/manual/user-manual.html")
		def kReferencePluginsCrumb = crumb('Reference Plugins', "${config.site_host}/docs/${version}/manual/plugin-references.html")
		def kSamplesCrumb = crumb('Samples', "${config.site_host}/docs/${version}/samples/")
		def kReleaseNotesCrumb = crumb('Release Notes', "${config.site_host}/docs/${version}/release-notes.html")

		switch (content.type) {
			case 'reference_chapter': return [kUserManualCrumb, kReferencePluginsCrumb, kContentCrumb]
			case 'reference_index': return [kUserManualCrumb, kReferencePluginsCrumb]
			case 'sample_chapter': return [kSamplesCrumb, kContentCrumb]
			case 'sample_index': return [kSamplesCrumb]
			case 'release_notes': return [kReleaseNotesCrumb]
			case 'manual_chapter':
				if (content.uri.endsWith('user-manual.html')) {
					return [kUserManualCrumb]
				} else {
					return [kUserManualCrumb, kContentCrumb]
				}
			case 'blog_index': return [kBlogCrumb]
			case 'blog_post': return [kBlogCrumb, kContentCrumb]
			case 'landing_page':
			case 'page':
				return []
			default:
				throw new UnsupportedOperationException("Unknown content type (${content.type})")
		}
	}

	def formatCrumb = { int index, Map c ->
		return """{
  "@type": "ListItem",
  "position": ${index + 1},
  "name": "${c.title}",
  "item": "${c.href}"
}"""
	}
%>

<%
if (content.type != 'redirection') {
	def breadcrumbs = getBreadcrumbs()
	if (!breadcrumbs.empty) {
%>
<script type="application/ld+json">
    [{
      "@context": "https://schema.org",
      "@type": "BreadcrumbList",
      "itemListElement": [${breadcrumbs.withIndex().collect { element, index -> formatCrumb(index, element) }.join(',')}]
    }]
</script>
<%
	}
}
%>
