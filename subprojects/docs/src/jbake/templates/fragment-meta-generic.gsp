<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<%if (content.description) {%><meta name="description" content="${content.description}"><%}%>
<meta name="author" content="@nokeedev">
<%if (content.tags) {%><meta name="keywords" content="${content.tags.join(', ')}"><%}%>
<% include 'fragment-meta-canonical.gsp' %>
