<title><%if (content.title) {%>${content.title}<% } else { %>Nokee Labs<% }%></title>
<% include 'fragment-meta-generic.gsp' %>
<% include 'fragment-meta-open-graph.gsp' %>
<% include 'fragment-meta-twitter-card.gsp' %>

<!-- Le styles -->
<link href="https://fonts.googleapis.com/css2?family=IBM+Plex+Sans&family=Rajdhani" rel="stylesheet">
<link href="https://fonts.googleapis.com/css?family=Lato&display=swap" rel="stylesheet">
<link href="https://stackpath.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet">
<link href="/css/normalize-8.0.1.css" rel="stylesheet">
<link href="/css/prettify.css" rel="stylesheet">
<link href="/component-menu.css" rel="stylesheet">
<% if (content.colorscheme && content.colorscheme == 'dark') {%>
<link href="/component-menu-dark.css" rel="stylesheet">
<%} else {%>
<link href="/component-menu-light.css" rel="stylesheet">
<%}%>

<!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]>
<script src="/js/html5shiv.min.js"></script>
<![endif]-->

<!-- Fav and touch icons -->
<link rel="apple-touch-icon" sizes="180x180" href="/apple-touch-icon.png">
<link rel="icon" type="image/png" sizes="32x32" href="/favicon-32x32.png">
<link rel="icon" type="image/png" sizes="16x16" href="/favicon-16x16.png">
<link rel="manifest" href="/site.webmanifest">
<link rel="mask-icon" href="/safari-pinned-tab.svg" color="#001824">
<meta name="msapplication-TileColor" content="#00a300">
<meta name="theme-color" content="#ffffff">


<!-- Global site tag (gtag.js) - Google Analytics -->
<script async src="https://www.googletagmanager.com/gtag/js?id=UA-157599764-1"></script>
<script>
	window.dataLayer = window.dataLayer || [];
	function gtag(){dataLayer.push(arguments);}
	gtag('js', new Date());

	gtag('config', 'UA-157599764-1');
</script>
