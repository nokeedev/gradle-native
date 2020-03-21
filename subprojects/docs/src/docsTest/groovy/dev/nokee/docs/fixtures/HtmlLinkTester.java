package dev.nokee.docs.fixtures;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import groovy.util.slurpersupport.NodeChild;
import groovyx.net.http.HttpBuilder;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.apache.commons.io.IOUtils;
import org.cyberneko.html.parsers.SAXParser;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

// NOTE: This need to be synched with the one on nokeedev.gitlab.io
public class HtmlLinkTester {
	private final MailtoValidator mailtoValidator;
	private final BlackList blackList;

	//region mailto: scheme validator
	public interface MailtoValidator {
		boolean isValidEmail(String email);
	}
	public static MailtoValidator validEmails(String... emails) {
		Set<String> validEmails = Sets.newHashSet(emails);
		return email -> validEmails.contains(email);
	}

	public interface BlackList {
		boolean isBlackListed(URI uri);
	}
	//endregion

	public HtmlLinkTester(MailtoValidator mailtoValidator, BlackList blacklist) {
		this.mailtoValidator = mailtoValidator;
		this.blackList = blacklist;
	}

	public FailureReport reportBrokenLinks(File root) {
		FailureReport report = new FailureReport();
		List<HtmlPage> pages = findHrefContainingFiles(root.toPath());
		Assert.assertThat(pages.size(), Matchers.greaterThan(0));
		pages.forEach(page -> {
			FailureReporter reporter = report.report(page.path);
			page.findHrefs().forEach(link -> {
				link.validate(reporter);
			});
		});
		return report;
	}

	public static class FailureReport {
		private final Multimap<Path, Failure> failures = MultimapBuilder.hashKeys().arrayListValues().build();

		FailureReporter report(Path path) {
			return new FailureReporter(failures, path);
		}

		public void assertNoFailures() {
			if (!failures.isEmpty()) {
				DiagnosticsVisitor visitor = new TreeFormatter();
				failures.asMap().entrySet().stream().forEach(it -> {
					visitor.node("Failures inside: " + it.getKey());
					DiagnosticsVisitor nestedVisitor = visitor.startChildren();
					it.getValue().forEach(failure -> {
						failure.explain(nestedVisitor);
					});
					nestedVisitor.endChildren();
				});
				throw new AssertionError(visitor.toString());
			}
		}
	}

	@AllArgsConstructor
	private static class FailureReporter {
		private final Multimap<Path, Failure> failures;
		private final Path from;

		void resourceDoesNotExists(Res resource) {
			failures.put(from, visitor -> visitor.node("Non-existing resource: " + resource));
		}

		void badAnchor(Res resource) {
			failures.put(from, visitor -> visitor.node("Bad anchor on target: " + resource));
		}

		void invalidEmail(Res resource) {
			failures.put(from, visitor -> visitor.node("Invalid email: " + resource));
		}

		void usingUnsecuredProtocol(Res resource) {
			failures.put(from, visitor -> visitor.node("Using unsecured protocol when one is available: " + resource));
		}

		void unsupportedResource(Res resource) {
			failures.put(from, visitor -> visitor.node("Resource unsupported, the test may need to be improved: " + resource));
		}
	}

	private interface Failure {
		void explain(DiagnosticsVisitor visitor);
	}

	//region Service
	private final UriService service = new CachingUriService();

	/**
	 * A service for general URI operations for this link tester.
	 */
	public interface UriService {
		/**
		 * Checks if the resource exists.
		 * @param uri resource to check
		 * @return {@code true} if the resource exists or {@code false} otherwise.
		 */
		boolean exists(URI uri);

		/**
		 * Fetches the resource as a text format.
		 * @param uri resource to fetch
		 * @return the resource as String or throw an exception.
		 */
		String fetch(URI uri);

		/**
		 * Checks the SSL certificate of the host the resource.
		 * @param uri resource to validate the SSL certificate
		 * @return {@code true} if the resource support SSL or {@code false} otherwise.
		 */
		boolean hasValidSslCertificate(URI uri);
	}

	/**
	 * Default implementation.
	 */
	public static class DefaultUriService implements UriService {
		public boolean exists(URI uri) {
			HttpBuilder client = HttpBuilder.configure(config -> {
				config.getRequest().setUri(uri);

				Map<String, CharSequence> headers = new HashMap<>();
				headers.put("User-Agent", "nokee-labs/0.0.0.1");
				config.getRequest().setHeaders(headers);
			});
			try {
				client.head();
				return true;
			} catch (RuntimeException e) {
				return false;
			}
		}

		public boolean hasValidSslCertificate(URI uri) {
			HttpBuilder client = HttpBuilder.configure(config -> {
				try {
					config.getRequest().setUri(new URI("https", uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null));
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}

				Map<String, CharSequence> headers = new HashMap<>();
				headers.put("User-Agent", "nokee-labs/0.0.0.1");
				config.getRequest().setHeaders(headers);
			});
			try {
				client.head();
				return true;
			} catch (RuntimeException e) {
				return false;
			}
		}

		public String fetch(URI uri) {
			try {
				URLConnection connection = uri.toURL().openConnection();
				connection.addRequestProperty("User-Agent", "Non empty");

				return IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
			} catch (IOException e) {
				throw new UncheckedIOException(String.format("Unable to load resource content '%s'", uri), e);
			}
		}
	}

	/**
	 * Caching implementation.
	 */
	public static class CachingUriService implements UriService {
		private final UriService delegate = new DefaultUriService();
		private final LoadingCache<URI, Boolean> headCache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.build(
				new CacheLoader<URI, Boolean>() {
					public Boolean load(URI key) {
						return delegate.exists(key);
					}
				});
		private final LoadingCache<URI, String> getCache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.build(
				new CacheLoader<URI, String>() {
					public String load(URI key) {
						return delegate.fetch(key);
					}
				});

		@Override
		public boolean exists(URI uri) {
			return headCache.getUnchecked(uri);
		}

		@Override
		public String fetch(URI uri) {
			return getCache.getUnchecked(uri);
		}

		@Override
		public boolean hasValidSslCertificate(URI uri) {
			return delegate.hasValidSslCertificate(uri);
		}
	}
	//endregion

	public List<HtmlPage> findHrefContainingFiles(Path root) {
		List<HtmlPage> result = new ArrayList<>();
		try {
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
					if (isHrefFile(path)) {
						result.add(new HtmlPage(root, path));
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			throw new UncheckedIOException("Unable to find href containing files", e);
		}
		return result;
	}

	public static boolean isHrefFile(Path path) {
		return path.toFile().getName().endsWith(".html");
	}

	@Value
	public class HtmlPage {
		@NonNull Path root;
		@NonNull Path path;

		public Set<Res> findHrefs() {
			return html(path.toUri()).getHyperlinkReferences().stream().map(link -> {
				String href = link.href;
				if (href.startsWith("http://") || href.startsWith("https://")) {
					return new HttpResource(link, URI.create(href));
				} else if (href.startsWith("/")) {
					return new OnDiskFile(link, root, root.resolve(href.substring(1)));
				} else if (href.startsWith("../")) {
					return new OnDiskFile(link, root, path.getParent().resolve(href).normalize());
				} else if (href.startsWith("#")) {
					return new AnchorWithinFile(link, root, path, href.substring(1));
				} else if (href.startsWith("mailto:")) {
					return new Mailto(link, URI.create(href));
				} else if (href.contains("#")) {
					if (href.contains(":")) {
						if (href.indexOf(":") < href.indexOf("#")) {
							// May be a unknown protocol/scheme
							return new Unknown(link, href);
						}
					}
					return new AnchorWithinFile(link, root, path.getParent().resolve(href.substring(0, href.indexOf("#"))), href.substring(href.indexOf("#") + 1));
				} else {
					return new OnDiskFile(link, root, path.getParent().resolve(href));
				}
			}).collect(Collectors.toSet());
		}
	}

	//region Resources
	public interface Res {
		void validate(FailureReporter visitor);
	}

	@Value
	public class HttpResource implements Res {
		@NonNull HtmlResource.Hyperlink link;
		@NonNull URI uri;

		@Override
		public void validate(FailureReporter reporter) {
			if (blackList.isBlackListed(uri)) {
				return;
			}
			if (!service.exists(getUriWithoutFragment())) {
				reporter.resourceDoesNotExists(this);
			}
			if (uri.getFragment() != null) {
				if (!html(getUriWithoutFragment()).hasAnchor(uri.getFragment())) {
					reporter.badAnchor(this);
				}
			}
			if (uri.getScheme().equals("http")) {
				if (service.hasValidSslCertificate(uri)) {
					reporter.usingUnsecuredProtocol(this);
				}
			}
		}

		private URI getUriWithoutFragment() {
			try {
				return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), null);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Value
	public class OnDiskFile implements Res {
		@NonNull HtmlResource.Hyperlink link;
		@NonNull Path root;
		@NonNull Path path;

		@Override
		public void validate(FailureReporter reporter) {
			if (blackList.isBlackListed(path.toUri())) {
				return;
			}
			if (!Files.exists(path)) {
				reporter.resourceDoesNotExists(this);
			}
		}
	}

	@Value
	public class AnchorWithinFile implements Res {
		@NonNull HtmlResource.Hyperlink link;
		@NonNull Path root;
		@NonNull Path path;
		@NonNull String anchor;

		@Override
		public void validate(FailureReporter reporter) {
			if (blackList.isBlackListed(path.toUri())) {
				return;
			}
			if (!Files.exists(path)) {
				reporter.resourceDoesNotExists(this);
			}

			if (!html(path.toUri()).hasAnchor(anchor)) {
				reporter.badAnchor(this);
			}
		}
	}

	@Value
	public class Unknown implements Res {
		@NonNull HtmlResource.Hyperlink link;
		@NonNull String href;

		@Override
		public void validate(FailureReporter reporter) {
			reporter.unsupportedResource(this);
		}
	}

	@Value
	public class Mailto implements Res {
		@NonNull HtmlResource.Hyperlink link;
		@NonNull URI mailto;

		@Override
		public void validate(FailureReporter reporter) {
			if (!mailtoValidator.isValidEmail(mailto.getSchemeSpecificPart())) {
				reporter.invalidEmail(this);
			}
		}
	}
	//endregion

	private HtmlResource html(URI uri) {
		return new HtmlResource(service, uri);
	}

	@Value
	public static class HtmlResource {
		@NonNull HtmlLinkTester.UriService http;
		@NonNull URI uri;

		public boolean hasAnchor(String anchorName) {
			return getAnchors().contains(anchorName);
		}

		public Set<String> getAnchors() {
			SAXParser parser = new SAXParser();
			try {
				GPathResult page = new XmlSlurper(parser).parseText(http.fetch(uri));
				Spliterator<GPathResult> it = Spliterators.spliteratorUnknownSize(page.depthFirst(), Spliterator.NONNULL);
				return StreamSupport.stream(it, false).filter(this::hasIdAnchor).map(this::extractIdAnchor).collect(Collectors.toSet());
			} catch (IOException | SAXException e) {
				// TODO: add to errors
				throw new RuntimeException(e);
			}
		}

		private boolean hasIdAnchor(GPathResult e) {
			return ((NodeChild)e).attributes().get("id") != null;
		}

		private String extractIdAnchor(GPathResult e) {
			return ((NodeChild)e).attributes().get("id").toString();
		}

		public Set<Hyperlink> getHyperlinkReferences() {
			SAXParser parser = new SAXParser();
			try {
				GPathResult page = new XmlSlurper(parser).parseText(http.fetch(uri));
				Spliterator<GPathResult> it = Spliterators.spliteratorUnknownSize(page.depthFirst(), Spliterator.NONNULL);
				Set<Hyperlink> result = Sets.newHashSet();
				result.addAll(StreamSupport.stream(it, false).filter(e -> e.name().equals("A") && ((NodeChild)e).attributes().get("href") != null).map(extractHref("href")).collect(Collectors.toSet()));
				result.addAll(StreamSupport.stream(it, false).filter(e -> e.name().equals("LINK") && ((NodeChild)e).attributes().get("href") != null).map(extractHref("href")).collect(Collectors.toSet()));
				result.addAll(StreamSupport.stream(it, false).filter(e -> e.name().equals("SCRIPT") && ((NodeChild)e).attributes().get("src") != null).map(extractHref("src")).collect(Collectors.toSet()));
				result.addAll(StreamSupport.stream(it, false).filter(e -> e.name().equals("IMG") && ((NodeChild)e).attributes().get("src") != null).map(extractHref("src")).collect(Collectors.toSet()));
				return result;
			} catch (IOException | SAXException e) {
				throw new RuntimeException(e);
			}
		}

		private Function<GPathResult, Hyperlink> extractHref(String attributeName) {
			return (GPathResult e) -> {
				List<BreadCrumb> breadCrumbs = Lists.newArrayList();
				GPathResult ee = e;
				while (!ee.name().equals("HTML")) {
					breadCrumbs.add(0, BreadCrumb.of((NodeChild) ee));
					ee = ee.parent();
				}
				return new Hyperlink(breadCrumbs, ((NodeChild) e).attributes().get("href").toString());
			};
		}

		@Value
		private static class BreadCrumb {
			@NonNull String name;
			String id;
			String classes;

			static BreadCrumb of(NodeChild n) {
				String name = n.name();
				String id = Objects.toString(n.attributes().get("id"), null);
				String classes = Objects.toString(n.attributes().get("class"), null);
				return new BreadCrumb(name, id, classes);
			}
		}

		@Value
		private static class Hyperlink {
			@NonNull List<BreadCrumb> crumbs;
			@NonNull String href;
		}
	}
}
