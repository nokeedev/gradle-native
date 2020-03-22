<%
def canonicalPathFor = { String path ->
	def canonicalPath = path
	def m = (canonicalPath =~ /docs\/(current|nightly|(\d+\.\d+\.\d+))(\/manual)?\/index.html/)
	if (m.matches()) {
		canonicalPath = "docs/${m.group(1)}/manual/user-manual.html"
	} else if (canonicalPath.endsWith('/index.html')) {
		canonicalPath = canonicalPath.substring(0, canonicalPath.lastIndexOf('/') + 1)
	} else if (canonicalPath == 'index.html') {
		canonicalPath = ''
	}
	return "${config.site_host}/${canonicalPath}"
}

def canonicalLink = canonicalPathFor(content.uri)
%>
<link rel="canonical" href="${canonicalLink}">
<meta property="og:url" content="${canonicalLink}">
