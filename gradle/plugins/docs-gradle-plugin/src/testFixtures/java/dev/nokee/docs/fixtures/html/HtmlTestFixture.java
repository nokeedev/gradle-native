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
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;

@Value
public class HtmlTestFixture {
	Path root;
	@NonNull URI uri;
	@NonNull UriService uriService;

	private String getPath() {
		if (uri.getScheme().equals("file")) {
			return separatorsToUnix(root.relativize(new File(uri).toPath()).toString());
		}
		return separatorsToUnix(uri.getPath());
	}

	public String getCanonicalPath() {
		String path = getPath();
		return String.format("https://docs.nokee.dev/%s", canonicalize(path));
	}

	private static final Pattern PATTERN = Pattern.compile("http-equiv=\"Refresh\" content=\"0; url=([^\"]+)");
	private String canonicalize(String path) {
		Matcher matcher = PATTERN.matcher(uriService.fetch(uri));
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			if (path.endsWith("/index.html")) {
				return path.substring(0, path.lastIndexOf("/") + 1);
			}
			if (path.equals("index.html")) {
				return "";
			}
			return path;
		}
	}

	public boolean isRedirectionPage() {
		String path = getPath();
		if (getPath().equals("index.html") || getPath().equals("manual/index.html")) {
			return true;
		}
		return false;
	}

	public boolean isJavadoc() {
		String path = getPath();
		return path.contains("javadoc/");
	}

	public List<ListItem> getBreadcrumbs() {
		String path = getPath();
		if (path.endsWith("/plugin-references.html")) {
			return asList(new ListItem("User Manual", "https://docs.nokee.dev/manual/user-manual.html"), new ListItem("Plugin References", "https://docs.nokee.dev/manual/plugin-references.html"));
		} else if (path.endsWith("-plugin.html")) {
			return asList(new ListItem("User Manual", "https://docs.nokee.dev/manual/user-manual.html"), new ListItem("Plugin References", "https://docs.nokee.dev/manual/plugin-references.html"), new ListItem(getPageName(), "https://docs.nokee.dev/" + path));
		} else if (path.equals("samples/index.html")) {
			return asList(new ListItem("Samples", "https://docs.nokee.dev/samples/"));
		} else if (path.equals("release-notes.html")) {
			return asList(new ListItem("Release Notes", "https://docs.nokee.dev/release-notes.html"));
		} else if (path.startsWith("samples/")) {
			return asList(new ListItem("Samples", "https://docs.nokee.dev/samples/"), new ListItem(getPageName(), "https://docs.nokee.dev/" + canonicalize(path)));
		} else if (path.endsWith("/user-manual.html")) {
			return asList(new ListItem("User Manual", "https://docs.nokee.dev/manual/user-manual.html"));
		} else if (path.startsWith("manual/")) {
			return asList(new ListItem("User Manual", "https://docs.nokee.dev/manual/user-manual.html"), new ListItem(getPageName(), "https://docs.nokee.dev/" + canonicalize(path)));
		} else if (path.equals("dsl/index.html")) {
			return asList(new ListItem("Domain Specific Language", "https://docs.nokee.dev/dsl/"));
		} else if (path.startsWith("dsl/")) {
			return asList(new ListItem("Domain Specific Language", "https://docs.nokee.dev/dsl/"), new ListItem(getPageName(), "https://docs.nokee.dev/" + canonicalize(path)));
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
