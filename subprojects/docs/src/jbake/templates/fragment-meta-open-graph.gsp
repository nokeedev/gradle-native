<% // og:url configuration is in fragment-meta-canonical.gsp %>
<meta property="og:site_name" content="Nokee">
<meta property="og:title" content="${content.title}">
<%if (content.opengraph) {%>
	<%if (content.opengraph.description) {%><meta property="og:description" content="${content.opengraph.description}"><%}%>
	<%if (content.opengraph.image) {%><meta property="og:image" content="${content.opengraph.image}"><%}%>
<%}%>
