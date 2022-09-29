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
import dev.nokee.docs.fixtures.LinkCheck
import dev.nokee.docs.fixtures.NokeeReadMe
import groovy.json.JsonSlurper
import org.hamcrest.Matchers
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Supplier

import static dev.nokee.docs.fixtures.HttpRequestMatchers.statusCode
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

class ReadmeTest {
	private static final String README_LOCATION_PROPERTY_NAME = 'dev.nokee.docs.readme.location'
	static NokeeReadMe readme = new NokeeReadMe(readmeFile.toPath())

	public static File getReadmeFile() {
		assertThat(System.properties, hasKey(README_LOCATION_PROPERTY_NAME));
		return new File(System.getProperty(README_LOCATION_PROPERTY_NAME))
	}

	@Test
	void "uses the latest released version"() {
		assertThat(readme.getNokeeVersion(), equalTo(currentNokeeVersion));
	}

	private static String getCurrentNokeeVersion() {
		return new JsonSlurper().parse(new URL('https://services.nokee.dev/versions/current.json'), [requestProperties: ['User-Agent': 'Fool-Us-github-pages']]).version
	}

	@Nested
	class WhenReadMeRenderedToHtml {
		@LinkCheck(NokeeReadMeSupplier)
		void checkUrls(URI context) {
			if (context.getScheme().equals("mailto")) {
				assertThat(context.toString(), equalTo("mailto:hello@nokee.dev"))
			} else {
				assertThat(context, statusCode(anyOf(Matchers.is(200), Matchers.is(301))))
			}
		}
	}

	public static final class NokeeReadMeSupplier implements Supplier<Path> {
		@Override
		public Path get() {
			NokeeReadMe readme = new NokeeReadMe(getReadmeFile().toPath());
			Path testDirectory = Files.createTempDirectory("nokee");
			Files.write(testDirectory.resolve("readme.html"), readme.renderToHtml().getBytes(StandardCharsets.UTF_8));
			return testDirectory;
		}
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
