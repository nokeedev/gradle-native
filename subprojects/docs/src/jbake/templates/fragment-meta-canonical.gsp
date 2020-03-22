<%
def canonicalPath = content.uri
def m = (canonicalPath =~ /docs\/(current|nightly|(\d+\.\d+\.\d+))(\/manual)?\/index.html/)
if (m.matches()) {
	canonicalPath = "docs/${m.group(1)}/manual/user-manual.html"
} else if (canonicalPath.endsWith('/index.html')) {
	canonicalPath = canonicalPath.substring(0, canonicalPath.lastIndexOf('/') + 1)
} else if (canonicalPath == 'index.html') {
	canonicalPath = ''
}
%>
<link rel="canonical" href="${config.site_host}/${canonicalPath}">
