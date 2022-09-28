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
import groovy.json.JsonSlurper
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

@Tag("Baked")
class ProperBakedHtmlTest {
	@Test
	void "has alt text on all images"() {
//		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.allHtmlWithoutJavadoc.collect { it.findAll(HtmlTag.IMG) }.each {
			it*.assertHasAltText()
		}
	}

	@Test
	void "has proper canonical links"() {
//		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.allHtmlWithoutJavadoc.each {
			def canonicalLinks = it.findAll(HtmlTag.LINK).findAll { it.isCanonical() }
			assertThat(canonicalLinks, iterableWithSize(1));
			assertThat(canonicalLinks.first().href, optionalWithValue(it.getCanonicalPath()));
		}
	}

	@Test
	void "has proper open-graph URL"() {
//		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.allHtmlWithoutJavadoc.each {
			def openGraphUrls = it.findAll(HtmlTag.META).findAll { it.openGraphUrl }
			assertThat(openGraphUrls, iterableWithSize(1));
			assertThat(openGraphUrls.first().content, equalTo(it.getCanonicalPath()));
		}
	}

	@Test
	void "has proper description"() {
		// It seems 160 characters is a good limit
//		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.allHtmlWithoutJavadoc.each {
			def metaDescriptions = it.findAll(HtmlTag.META).findAll { it.description }
			assertThat("${it.uri} does not have the right meta description tag count", metaDescriptions, iterableWithSize(1));
			assertThat("${it.uri} cannot have empty meta description tag", metaDescriptions.first().content.size(), allOf(greaterThan(0), lessThanOrEqualTo(160)));
		}
	}

	@Test
	void "has proper keywords"() {
//		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.allHtmlWithoutJavadoc.each {
			def metaKeywords = it.findAll(HtmlTag.META).findAll { it.keywords }
			assertThat("${it.uri} does not have the right meta keyword tag count", metaKeywords, iterableWithSize(1));
			assertThat("${it.uri} cannot have empty meta keyword tag", metaKeywords.first().content.size(), greaterThan(0));
			assertThat(metaKeywords.first().content, not(allOf(startsWith("["), endsWith("]"))));
		}
	}

	@Test
	void "has proper twitter meta description"() {
//		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.allHtmlWithoutJavadoc.each {
			def twitterDescriptions = it.findAll(HtmlTag.META).findAll { it.twitterDescription }
			assertThat("${it.uri} does not have the right meta twitter description tag count", twitterDescriptions, iterableWithSize(1));
			assertThat("${it.uri} cannot have empty meta keyword tag", twitterDescriptions.first().content.size(), allOf(greaterThan(0), lessThanOrEqualTo(160)));
		}
	}

	@Disabled
	@Test
	void "has breadcrumb structured data"() {
//		expect:
		def fixture = new BakedHtmlFixture(new File(System.getProperty('bakedContentDirectory')).toPath())
		fixture.allHtmlWithoutRedirectionAndJavadoc.each {
			// Retrieve structured data tag
			def scripts = it.findAll(HtmlTag.SCRIPT).findAll { it.type.present && it.type.get() == 'application/ld+json' }
			assertThat("${it.uri} does not have the right structured data tag count", scripts, iterableWithSize(1));

			// Retrieve structured data
			assertThat(scripts.first().text, optionalWithValue())
			def data = new JsonSlurper().parse(scripts.first().text.get().bytes)

			// Retrieve breadcrumb data
			def breadcrumb = data.findAll { it.'@type' == 'BreadcrumbList' }
			assertThat(breadcrumb, iterableWithSize(1));
			assertThat(breadcrumb.first().itemListElement.size(), it.breadcrumbs.size());

			GroovyCollections.transpose(breadcrumb.first().itemListElement, it.breadcrumbs).eachWithIndex { obj, index ->
				def left = obj[0]
				assertThat(left.'@type', equalTo('ListItem'));
				assertThat(left.position, equalTo((index + 1)));

				def right = obj[1]
				// We are not asserting the name for now because it doesn't exactly align with the URL
				assertThat(left.item, equalTo(right.item));
			}
		}
	}
}
