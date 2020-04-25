package dev.nokee.docs

import dev.nokee.docs.fixtures.html.HtmlLinkTester
import dev.nokee.docs.tags.Baked
import org.junit.experimental.categories.Category
import spock.lang.Specification

@Category(Baked)
class BrokenLinksTest extends Specification {
	def "checks HTML for broken links"() {
		expect:
		def report = new HtmlLinkTester(HtmlLinkTester.validEmails("hello@nokee.dev"), new HtmlLinkTester.BlackList() {
			@Override
			boolean isBlackListed(URI uri) {
				return uri.toString().contains("/blog") || uri.toString().contains('/current/') || uri.toString().endsWith("/baked/index.html")
			}
		}).reportBrokenLinks(new File(System.getProperty('bakedContentDirectory')))
		report.assertNoFailures()
	}
}
