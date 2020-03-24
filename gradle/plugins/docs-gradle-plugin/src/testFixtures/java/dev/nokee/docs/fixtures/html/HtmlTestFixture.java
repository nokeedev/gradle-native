package dev.nokee.docs.fixtures.html;

import com.google.common.collect.Sets;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import groovy.util.slurpersupport.NodeChild;
import lombok.NonNull;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.cyberneko.html.parsers.SAXParser;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;

@Value
public class HtmlTestFixture {
	Path root;
	@NonNull URI uri;
	@NonNull UriService uriService;

	private String getPath() {
		if (uri.getScheme().equals("file")) {
			return root.relativize(new File(uri).toPath()).toString();
		}
		return uri.getPath();
	}

	public String getCanonicalPath() {
		String path = getPath();
		Matcher m = Pattern.compile("docs/(nightly|current|(\\d+\\.\\d+\\.\\d+))(/manual)?/index\\.html").matcher(path);
		if (m.matches()) {
			path = String.format("docs/%s/manual/user-manual.html", m.group(1));
		}
		return String.format("https://nokee.dev/%s", canonicalize(path));
	}

	private String canonicalize(String path) {
		if (path.endsWith("/index.html")) {
			return path.substring(0, path.lastIndexOf("/") + 1);
		}
		return path;
	}

	public boolean isRedirectionPage() {
		String path = getPath();
		Matcher m = Pattern.compile("docs/(nightly|current|(\\d+\\.\\d+\\.\\d+))(/manual)?/index\\.html").matcher(path);
		if (m.matches()) {
			return true;
		}
		return false;
	}

	public List<ListItem> getBreadcrumbs() {
		String path = getPath();
		if (path.endsWith("/plugin-references.html")) {
			return asList(new ListItem("User Manual", "https://nokee.dev/docs/nightly/manual/user-manual.html"), new ListItem("Plugin References", "https://nokee.dev/docs/nightly/manual/plugin-references.html"));
		} else if (path.endsWith("-plugin.html")) {
			return asList(new ListItem("User Manual", "https://nokee.dev/docs/nightly/manual/user-manual.html"), new ListItem("Plugin References", "https://nokee.dev/docs/nightly/manual/plugin-references.html"), new ListItem(getPageName(), "https://nokee.dev/" + path));
		} else if (path.endsWith("/samples/index.html")) {
			return asList(new ListItem("Samples", "https://nokee.dev/docs/nightly/samples/"));
		} else if (path.endsWith("/release-notes.html")) {
			return asList(new ListItem("Release Notes", "https://nokee.dev/docs/nightly/release-notes.html"));
		} else if (path.contains("/samples/")) {
			return asList(new ListItem("Samples", "https://nokee.dev/docs/nightly/samples/"), new ListItem(getPageName(), "https://nokee.dev/" + canonicalize(path)));
		} else if (path.endsWith("/user-manual.html")) {
			return asList(new ListItem("User Manual", "https://nokee.dev/docs/nightly/manual/user-manual.html"));
		} else if (path.contains("/manual/")) {
			return asList(new ListItem("User Manual", "https://nokee.dev/docs/nightly/manual/user-manual.html"), new ListItem(getPageName(), "https://nokee.dev/" + canonicalize(path)));
		}
		return Collections.emptyList();
	}

	// Not used, but kept as it's somewhat good-ish
	private String getPageName() {
		String htmlPageName = getPath();
		if (htmlPageName.contains("/samples/")) {
			htmlPageName = htmlPageName.substring(0, htmlPageName.lastIndexOf('/'));
			htmlPageName = htmlPageName.substring(htmlPageName.lastIndexOf('/') + 1);
		} else {
			htmlPageName = htmlPageName.substring(htmlPageName.lastIndexOf('/') + 1);
			htmlPageName = htmlPageName.substring(0, htmlPageName.lastIndexOf('.'));
		}
		htmlPageName = Arrays.stream(htmlPageName.split("-")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
		htmlPageName = htmlPageName.replace("Junit", "JUnit");
		htmlPageName = htmlPageName.replace("Jni", "JNI");
		htmlPageName = htmlPageName.replace("With", "with");
		return htmlPageName;
	}

	public <T extends HtmlTagFixture> Set<T> findAll(HtmlTag<T> tag) {
		SAXParser parser = new SAXParser();
		try {
			GPathResult page = new XmlSlurper(parser).parseText(uriService.fetch(uri));
			Spliterator<GPathResult> it = Spliterators.spliteratorUnknownSize(page.depthFirst(), Spliterator.NONNULL);
			Set<T> result = Sets.newLinkedHashSet();
			result.addAll(StreamSupport.stream(it, false)
					.filter(tag::is)
					.map(e -> tag.create(uri, (NodeChild)e))
					.collect(Collectors.toSet()));
			return result;
		} catch (IOException | SAXException e) {
			throw new RuntimeException(e);
		}
	}

	@Value
	public static class ListItem {
		@NonNull String name;
		@NonNull String item;
	}
}
