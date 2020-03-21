<%
    def twitterCreator = '@nokeedev'
	if (content.twitter && content.twitter.creator) {
		twitterCreator = content.twitter.creator
	}
%>
<meta name="twitter:site" content="@nokeedev">
<meta name="twitter:creator" content="${twitterCreator}">
<meta name="twitter:title" content="${content.title}">
<%if (content.twitter) {%>
  <%if (content.twitter.card) {%><meta name="twitter:card" content="${content.twitter.card}"><%}%>
<%}%>
