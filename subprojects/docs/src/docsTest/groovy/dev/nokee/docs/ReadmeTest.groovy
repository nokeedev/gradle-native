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


import dev.gradleplugins.runnerkit.GradleExecutor
import dev.gradleplugins.runnerkit.GradleRunner
import dev.gradleplugins.test.fixtures.gradle.GradleScriptDsl
import dev.nokee.docs.fixtures.NokeeReadMe
import dev.nokee.docs.fixtures.html.HtmlLinkTester
import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

import java.nio.file.Files

import static dev.nokee.docs.fixtures.html.HtmlLinkTester.validEmails
import static org.asciidoctor.OptionsBuilder.options
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasKey

class ReadmeTest {
	private static final String README_LOCATION_PROPERTY_NAME = 'dev.nokee.docs.readme.location'
	static NokeeReadMe readme = new NokeeReadMe(readmeFile.toPath())

	private static File getReadmeFile() {
		assertThat(System.properties, hasKey(README_LOCATION_PROPERTY_NAME));
		return new File(System.getProperty(README_LOCATION_PROPERTY_NAME))
	}

	@Test
	void "uses the latest released version"() {
//		expect:
		assertThat(readme.getNokeeVersion(), equalTo(currentNokeeVersion));
	}

	private static String getCurrentNokeeVersion() {
		return new JsonSlurper().parse(new URL('https://services.nokee.dev/versions/current.json'), [requestProperties: ['User-Agent': 'Fool-Us-github-pages']]).version
	}

	@EnabledOnOs(OS.MAC)
	void "checks for broken links"() {
//		given:
		def rootDirectory = Files.createTempDirectory('nokee')
		def renderedReadMeFile = rootDirectory.resolve('readme.html').toFile()
		renderedReadMeFile.text = asciidoctor.convertFile(readmeFile, options().toFile(false))

//		expect:
		def report = new HtmlLinkTester(validEmails("hello@nokee.dev"), new HtmlLinkTester.BlackList() {
			@Override
			boolean isBlackListed(URI uri) {
				if (uri.scheme == 'file') {
					def path = rootDirectory.toUri().relativize(uri).toString()
					if (new File(readmeFile.parentFile, path).exists()) {
						return true
					}
				}
				return false
			}
		}).reportBrokenLinks(rootDirectory.toFile())
		report.assertNoFailures()
	}

	@ParameterizedTest(name = "check code snippet [{0}]")
	@EnumSource(GradleScriptDsl)
	void "check code snippet"(dsl) {
//		given:
		def rootDirectory = Files.createTempDirectory('nokee')
		def settingsFile = rootDirectory.resolve(dsl.settingsFileName).toFile()
		def buildFile = rootDirectory.resolve(dsl.buildFileName).toFile()
		readme.findSnippetBlocks().each {
			switch (it.getType()) {
				case NokeeReadMe.SnippetBlock.Type.BUILD:
					buildFile << it.content
					break
				case NokeeReadMe.SnippetBlock.Type.SETTINGS:
					settingsFile << it.content
					break
				default:
					throw new RuntimeException("Unrecognized snippet block")
			}
		}

//		and:
		GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(rootDirectory.toFile()).withGradleVersion("6.2.1").withGradleUserHomeDirectory(rootDirectory.resolve('gradle-user-home').toFile())

//		expect:
		runner.withArgument('help').build()
		runner.withArgument('tasks').build()
	}
}
