/*
 * Copyright 2020-2021 the original author or authors.
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
					return uri.toString().contains('/javadoc/org/gradle/internal.metaobject');
				}
				return uri.toString().contains("/services") ||
					uri.toString().contains("/blog") ||
					uri.toString().contains('/current/') ||
					uri.toString().endsWith("/baked/index.html") ||
					uri.toString().contains('/api/javax/annotation/Nullable.html') ||
					uri.toString().contains('/api/javax/annotation/Nonnull.html') ||
					uri.toString().contains('/github.com/') ||
					uri.toString().contains('/javadoc/')
			}
		}).reportBrokenLinks(new File(System.getProperty('bakedContentDirectory')))
		report.assertNoFailures()
	}
}
