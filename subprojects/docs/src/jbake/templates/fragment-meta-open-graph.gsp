<%
	def indexOfLastSlash = content.uri.lastIndexOf('/')
	def permalink = ''
	if (indexOfLastSlash != -1) {
		permalink = content.uri.substring(0, content.uri.lastIndexOf('/'))
	}
%>
<% // og:url configuration is in fragment-meta-canonical.gsp %>
<meta property="og:site_name" content="Nokee">
<meta property="og:title" content="${content.title}">
<%if (content.description) {%><meta name="og:description" content="${content.description}"><%}%>
<%if (content.leadimage) {%><meta name="og:image" content="${config.site_host}/${permalink}/${content.leadimage}"><%}%>
