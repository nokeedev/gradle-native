<%
    def twitterCreator = '@nokeedev'
	if (content.twitter && content.twitter.creator) {
		twitterCreator = content.twitter.creator
	}

	def indexOfLastSlash = content.uri.lastIndexOf('/')
	def permalink = ''
	if (indexOfLastSlash != -1) {
		permalink = content.uri.substring(0, content.uri.lastIndexOf('/'))
	}

	def twitterCard = 'summary'
//	if (content.type == 'sample_chapter') {
//		twitterCard = 'player'
//	}
	if (content.twitter && content.twitter.card) {
		twitterCard = content.twitter.card
	}
%>
<meta name="twitter:site" content="@nokeedev">
<meta name="twitter:creator" content="${twitterCreator}">
<meta name="twitter:title" content="${content.title}">
<meta name="twitter:card" content="${twitterCard}">
<%if (content.description) {%><meta name="twitter:description" content="${content.description}"><%}%>
<%if (twitterCard == 'summary_large_image') {%>
	<%if (content.leadimage) {%><meta name="twitter:image" content="${config.site_host}/${permalink}/${content.leadimage}"><%}%>
	<%if (content.leadimagealt) {%><meta name="twitter:image:alt" content="${content.leadimagealt}"><%}%>
<%}%>
