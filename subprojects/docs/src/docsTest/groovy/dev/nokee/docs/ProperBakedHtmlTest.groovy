package dev.nokee.docs

import dev.nokee.docs.fixtures.html.BakedHtmlFixture
import dev.nokee.docs.fixtures.html.HtmlTag
import spock.lang.Specification

class ProperBakedHtmlTest extends Specification {
	def "has alt text on all images"() {
		expect:
		true
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.findAllHtml().collect { it.findAll(HtmlTag.IMG) }.each {
			it*.assertHasAltText()
		}
	}
}
