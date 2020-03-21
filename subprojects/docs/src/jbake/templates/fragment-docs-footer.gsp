
	<div id="push"></div>
	<div id="footer">
		<div class="container">
			<p class="muted credit">&copy; 2020</p>
		</div>
	</div>

	<% include 'fragment-gdpr-banner.gsp' %>
	<!-- Le javascript
	================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<script src="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>js/prettify.js"></script>
	<script src="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>js/multi-language-sample.js"></script>
	<script src="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>js/active-link.js"></script>
