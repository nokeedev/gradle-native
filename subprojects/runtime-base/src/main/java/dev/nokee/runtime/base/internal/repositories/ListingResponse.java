package dev.nokee.runtime.base.internal.repositories;

import java.util.List;
import java.util.stream.Collectors;

public class ListingResponse implements Response {
	private final List<String> versions;

	public ListingResponse(List<String> versions) {
		this.versions = versions;
	}

	@Override
	public String getContentType() {
		return "text/html";
	}

	@Override
	public String getContent() {
		return "<html>\n" +
			"<head></head>\n" +
			"<body>\n" +
			versions.stream().map(it -> "<pre><a href=\"" + it + "/\">" + it + "/</a></pre>").collect(Collectors.joining("\n")) + "\n" +
			"</body>\n" +
			"</html>";
	}
}
