package dev.nokee.docs

import dev.nokee.docs.fixtures.html.BakedHtmlFixture
import dev.nokee.docs.fixtures.html.HtmlTag
import spock.lang.Specification

class ProperBakedHtmlTest extends Specification {
	def "has alt text on all images"() {
		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.findAllHtml().collect { it.findAll(HtmlTag.IMG) }.each {
			it*.assertHasAltText()
		}
	}

	def "has proper canonical links"() {
		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.findAllHtml().each {
			def canonicalLinks = it.findAll(HtmlTag.LINK).findAll { it.isCanonical() }
			assert canonicalLinks.size() == 1
			assert canonicalLinks.first().href.present
			assert canonicalLinks.first().href.get() == it.getCanonicalPath()
		}
	}

	def "has proper open-graph URL"() {
		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.findAllHtml().each {
			def openGraphUrls = it.findAll(HtmlTag.META).findAll { it.openGraphUrl }
			assert openGraphUrls.size() == 1
			assert openGraphUrls.first().content == it.getCanonicalPath()
		}
	}

	def "has proper description"() {
		// It seems 160 characters is a good limit
		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.findAllHtml().each {
			def metaDescriptions = it.findAll(HtmlTag.META).findAll { it.description }
			assert metaDescriptions.size() == 1, "${it.uri} does not have meta description tag"
			assert metaDescriptions.first().content.size() > 0, "${it.uri} cannot have empty meta description tag"
			assert metaDescriptions.first().content.size() <= 160
		}
	}

	def "has proper keywords"() {
		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.findAllHtml().each {
			def metaKeywords = it.findAll(HtmlTag.META).findAll { it.keywords }
			assert metaKeywords.size() == 1, "${it.uri} does not have meta keyword tag"
			assert metaKeywords.first().content.size() > 0, "${it.uri} cannot have empty meta keyword tag"
			assert !metaKeywords.first().content.startsWith('[') && !metaKeywords.first().content.endsWith(']')
		}
	}
}
