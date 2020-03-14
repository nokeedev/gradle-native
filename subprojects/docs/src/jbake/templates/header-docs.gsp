<!DOCTYPE html>
<html lang="en">
  <head>
	<meta charset="utf-8"/>
	<title><%if (content.title) {%>${content.title}<% } else { %>JBake<% }%></title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<%if (content.description) {%><meta name="description" content="${content.description}"><%}%>
	<meta name="author" content="">
	<meta name="keywords" content="">

	<!-- Le styles -->
	<link href="https://fonts.googleapis.com/css?family=Lato&display=swap" rel="stylesheet">
	<link href="https://stackpath.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet">
	<link href="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>css/normalize-8.0.1.css" rel="stylesheet">
	<link href="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>css/docs-asciidoctor.css" rel="stylesheet">
	<link href="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>css/docs-asciidoctor-override.css" rel="stylesheet">
	<link href="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>css/docs-asciidoctor-color-override.css" rel="stylesheet">
	<link href="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>css/docs-base.css" rel="stylesheet">
	<link href="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>css/prettify.css" rel="stylesheet">
	<link href="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>css/menu.css" rel="stylesheet">
	<link href="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>css/multi-language-sample.css" rel="stylesheet">
	<link href="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>css/docs-samples.css" rel="stylesheet">

	<!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
	<!--[if lt IE 9]>
	  <script src="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>js/html5shiv.min.js"></script>
	<![endif]-->
  </head>
  <body onload="prettyPrint()">
