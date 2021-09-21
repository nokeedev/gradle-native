/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.docs

import dev.nokee.docs.fixtures.html.BakedHtmlFixture
import dev.nokee.docs.fixtures.html.HtmlTag
import dev.nokee.docs.tags.Baked
import groovy.json.JsonSlurper
import org.junit.experimental.categories.Category
import spock.lang.Specification

@Category(Baked)
class ProperBakedHtmlTest extends Specification {
	def "has alt text on all images"() {
		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.allHtmlWithoutJavadoc.collect { it.findAll(HtmlTag.IMG) }.each {
			it*.assertHasAltText()
		}
	}

	def "has proper canonical links"() {
		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.allHtmlWithoutJavadoc.each {
			def canonicalLinks = it.findAll(HtmlTag.LINK).findAll { it.isCanonical() }
			assert canonicalLinks.size() == 1
			assert canonicalLinks.first().href.present
			assert canonicalLinks.first().href.get() == it.getCanonicalPath()
		}
	}

	def "has proper open-graph URL"() {
		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.allHtmlWithoutJavadoc.each {
			def openGraphUrls = it.findAll(HtmlTag.META).findAll { it.openGraphUrl }
			assert openGraphUrls.size() == 1
			assert openGraphUrls.first().content == it.getCanonicalPath()
		}
	}

	def "has proper description"() {
		// It seems 160 characters is a good limit
		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.allHtmlWithoutJavadoc.each {
			def metaDescriptions = it.findAll(HtmlTag.META).findAll { it.description }
			assert metaDescriptions.size() == 1, "${it.uri} does not have the right meta description tag count"
			assert metaDescriptions.first().content.size() > 0, "${it.uri} cannot have empty meta description tag"
			assert metaDescriptions.first().content.size() <= 160
		}
	}

	def "has proper keywords"() {
		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.allHtmlWithoutJavadoc.each {
			def metaKeywords = it.findAll(HtmlTag.META).findAll { it.keywords }
			assert metaKeywords.size() == 1, "${it.uri} does not have the right meta keyword tag count"
			assert metaKeywords.first().content.size() > 0, "${it.uri} cannot have empty meta keyword tag"
			assert !metaKeywords.first().content.startsWith('[') && !metaKeywords.first().content.endsWith(']')
		}
	}

	def "has proper twitter meta description"() {
		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.allHtmlWithoutJavadoc.each {
			def twitterDescriptions = it.findAll(HtmlTag.META).findAll { it.twitterDescription }
			assert twitterDescriptions.size() == 1, "${it.uri} does not have the right meta twitter description tag count"
			assert twitterDescriptions.first().content.size() > 0, "${it.uri} cannot have empty meta keyword tag"
			assert twitterDescriptions.first().content.size() <= 160
		}
	}

	def "has breadcrumb structured data"() {
		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.allHtmlWithoutRedirectionAndJavadoc.each {
			// Retrieve structured data tag
			def scripts = it.findAll(HtmlTag.SCRIPT).findAll { it.type.present && it.type.get() == 'application/ld+json' }
			assert scripts.size() == 1, "${it.uri} does not have the right structured data tag count"

			// Retrieve structured data
			assert scripts.first().text.present
			def data = new JsonSlurper().parse(scripts.first().text.get().bytes)

			// Retrieve breadcrumb data
			def breadcrumb = data.findAll { it.'@type' == 'BreadcrumbList' }
			assert breadcrumb.size() == 1
			assert breadcrumb.first().itemListElement.size() == it.breadcrumbs.size()

			GroovyCollections.transpose(breadcrumb.first().itemListElement, it.breadcrumbs).eachWithIndex { obj, index ->
				def left = obj[0]
				assert left.'@type' == 'ListItem'
				assert left.position == (index + 1)

				def right = obj[1]
				// We are not asserting the name for now because it doesn't exactly align with the URL
				assert left.item == right.item
			}
		}
	}
}
