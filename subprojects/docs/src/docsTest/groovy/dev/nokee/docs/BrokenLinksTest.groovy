package dev.nokee.docs

import dev.nokee.docs.fixtures.html.HtmlLinkTester
import dev.nokee.docs.tags.Baked
import org.junit.experimental.categories.Category
import spock.lang.Requires
import spock.lang.Specification
import spock.util.environment.OperatingSystem

@Category(Baked)
@Requires({ os.family == OperatingSystem.Family.MAC_OS })
class BrokenLinksTest extends Specification {
	def "checks HTML for broken links"() {
		expect:
		def report = new HtmlLinkTester(HtmlLinkTester.validEmails("hello@nokee.dev"), new HtmlLinkTester.BlackList() {
			@Override
			boolean isBlackListed(URI uri) {
				if (uri.toString().contains("/blog.gradle.org/")) {
					return false
				}
				if (uri.toString().contains("/docs.gradle.org/")) {
					return false
				}
				return uri.toString().contains("/blog") || uri.toString().contains('/current/') || uri.toString().endsWith("/baked/index.html") || uri.toString().contains('/api/javax/annotation/Nonnull.html') || uri.toString().contains('/github.com/')
			}
		}).reportBrokenLinks(new File(System.getProperty('bakedContentDirectory')))
		report.assertNoFailures()
	}
}
