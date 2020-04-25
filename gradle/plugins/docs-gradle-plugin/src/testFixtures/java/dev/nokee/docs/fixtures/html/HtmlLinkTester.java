package dev.nokee.docs.fixtures.html;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;
import dev.nokee.docs.fixtures.DiagnosticsVisitor;
import dev.nokee.docs.fixtures.TreeFormatter;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.hamcrest.Matchers;
import org.junit.Assert;

import java.io.File;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
		List<HtmlTestFixture> pages = new BakedHtmlFixture(root.toPath()).findAllHtml();
		Assert.assertThat(pages.size(), Matchers.greaterThan(0));
		pages.forEach(page -> {
			FailureReporter reporter = report.report(new File(page.getUri()).toPath());
			findHrefs(page).forEach(link -> {
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

	private final UriService service = UriService.INSTANCE;

	public Set<Res> findHrefs(HtmlTestFixture thiz) {
		Set<Hyperlink> links = new HashSet<>();
		links.addAll(thiz.findAll(HtmlTag.A).stream().filter(it -> it.getHref().isPresent()).map(it -> new Hyperlink(it.getPath(), it.getHref().get())).collect(Collectors.toSet()));
		links.addAll(thiz.findAll(HtmlTag.IMG).stream().filter(it -> it.getSrc().isPresent()).map(it -> new Hyperlink(it.getPath(), it.getSrc().get())).collect(Collectors.toSet()));
		links.addAll(thiz.findAll(HtmlTag.SCRIPT).stream().filter(it -> it.getSrc().isPresent()).map(it -> new Hyperlink(it.getPath(), it.getSrc().get())).collect(Collectors.toSet()));
		links.addAll(thiz.findAll(HtmlTag.LINK).stream().filter(it -> it.getHref().isPresent() && !it.isCanonical()).map(it -> new Hyperlink(it.getPath(), it.getHref().get())).collect(Collectors.toSet()));
		return links.stream().map(link -> {
			String href = URLDecoder.decode(link.getHref());

			// Shortcut for known href to ignore
			if (href.startsWith("javascript:")) {
				return new JavaScript();
			} else if (href.startsWith("mailto:")) {
				return new Mailto(link, URI.create(href));
			}

			Optional<String> anchor = Optional.empty();
			if (href.contains("#")) {
				anchor = Optional.of(href.substring(href.indexOf("#") + 1));
				href = href.substring(0, href.indexOf("#"));
			}

			if (href.endsWith("/")) {
				href += "index.html";
			}

			ResWithUri res = new OnDiskFile(link, thiz.getRoot(), new File(thiz.getUri()).toPath().getParent().resolve(href).toUri());
			if (href.startsWith("http://") || href.startsWith("https://")) {
				res = new HttpResource(link, URI.create(href));
			} else if (href.startsWith("/")) {
				res = new OnDiskFile(link, thiz.getRoot(), thiz.getRoot().resolve(href.substring(1)).toUri());
			} else if (href.startsWith("../")) {
				res = new OnDiskFile(link, thiz.getRoot(), new File(thiz.getUri()).toPath().getParent().resolve(href).normalize().toUri());
			} else if (href.isEmpty()) {
				res = new OnDiskFile(link, thiz.getRoot(), thiz.getUri());
			} else if (href.contains(":")) {
				// May be a unknown protocol/scheme
				return new Unknown(link, href);
			}

			if (anchor.isPresent()) {
				return new AnchorWithinResource(res, anchor.get());
			}
			return res;
		}).collect(Collectors.toSet());
	}

	//region Resources
	public interface Res {
		void validate(FailureReporter visitor);
	}

	@Value
	public class AnchorWithinResource implements Res {
		@NonNull ResWithUri resource;
		@NonNull String anchor;

		@Override
		public void validate(FailureReporter visitor) {
			resource.validate(visitor);

			if (resource.getFixture().findAll(HtmlTag.ANCHOR).stream().noneMatch(it -> it.getIdOrName().equals(anchor))) {
				visitor.badAnchor(this);
			}
		}
	}

	@Value
	public class JavaScript implements Res {
		@Override
		public void validate(FailureReporter visitor) {
		}
	}

	public interface ResWithUri extends Res {
		HtmlTestFixture getFixture();
	}

	@Value
	public class HttpResource implements ResWithUri {
		@NonNull Hyperlink link;
		@NonNull URI uri;

		@Override
		public HtmlTestFixture getFixture() {
			return new HtmlTestFixture(null, uri, UriService.INSTANCE);
		}

		@Override
		public void validate(FailureReporter reporter) {
			if (blackList.isBlackListed(uri)) {
				return;
			}
			if (!service.exists(uri)) {
				reporter.resourceDoesNotExists(this);
			}
			if (uri.getScheme().equals("http")) {
				if (service.hasValidSslCertificate(uri)) {
					reporter.usingUnsecuredProtocol(this);
				}
			}
		}
	}

	@Value
	public class OnDiskFile implements ResWithUri {
		@NonNull Hyperlink link;
		@NonNull Path root;
		@NonNull URI uri;

		@Override
		public HtmlTestFixture getFixture() {
			return new HtmlTestFixture(root, uri, UriService.INSTANCE);
		}

		@Override
		public void validate(FailureReporter reporter) {
			if (blackList.isBlackListed(uri)) {
				return;
			}
			if (!new File(withoutQueryString(uri)).exists()) {
				reporter.resourceDoesNotExists(this);
			}
		}

		private URI withoutQueryString(URI href) {
			String uri = URLDecoder.decode(href.toString());
			if (uri.contains("?")) {
				return URI.create(uri.substring(0, uri.indexOf("?")));
			}
			return href;
		}
	}

	@Value
	public class Unknown implements Res {
		@NonNull Hyperlink link;
		@NonNull String href;

		@Override
		public void validate(FailureReporter reporter) {
			reporter.unsupportedResource(this);
		}
	}

	@Value
	public class Mailto implements Res {
		@NonNull Hyperlink link;
		@NonNull URI mailto;

		@Override
		public void validate(FailureReporter reporter) {
			if (!mailtoValidator.isValidEmail(mailto.getSchemeSpecificPart())) {
				reporter.invalidEmail(this);
			}
		}
	}
	//endregion

	@Value
	public static class Hyperlink {
		@NonNull HtmlTagPath crumbs;
		@NonNull String href;
	}
}
