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
package dev.nokee.docs;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.gradleplugins.test.fixtures.gradle.GradleScriptDsl;
import dev.nokee.docs.fixtures.LinkCheckerUtils;
import dev.nokee.docs.fixtures.NokeeReadMe;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.stream.Stream;

import static dev.nokee.docs.fixtures.HttpRequestMatchers.document;
import static dev.nokee.docs.fixtures.HttpRequestMatchers.statusCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;

class ReadmeTest {
	private static final String README_LOCATION_PROPERTY_NAME = "dev.nokee.docs.readme.location";
	static NokeeReadMe readme = new NokeeReadMe(getReadmeFile().toPath());

	public static File getReadmeFile() {
		assertThat(System.getProperties(), hasKey(README_LOCATION_PROPERTY_NAME));
		return new File(System.getProperty(README_LOCATION_PROPERTY_NAME));
	}

	@Test
	void usesTheLatestReleasedVersion() throws IOException {
		assertThat(readme.getNokeeVersion(), equalTo(fetchCurrentNokeeVersion()));
	}

	private static String fetchCurrentNokeeVersion() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL("https://services.nokee.dev/versions/current.json").openConnection();
		connection.setRequestProperty("User-Agent", "Fool-Us-github-pages");
		connection.connect();
		try (Reader reader = new InputStreamReader(connection.getInputStream())) {
			return new Gson().<Map<String, Object>>fromJson(reader, new TypeToken<Map<String, Object>>() {}.getType())
				.get("version").toString();
		} finally {
			connection.disconnect();
		}
	}

	@Nested
	class WhenReadMeRenderedToHtml {
//		@EnabledOnOs(OS.LINUX)
		@ParameterizedTest(name = "check URL [{0}]")
		@ArgumentsSource(NokeeReadMeProvider.class)
		void checkUrls(URI context) {
			if (context.getScheme().equals("mailto")) {
				assertThat(context.toString(), equalTo("mailto:hello@nokee.dev"));
			} else {
				assertThat(context, statusCode(anyOf(Matchers.is(200), Matchers.is(301))));
				if (context.getPath().contains("#")) {
					String id = context.getPath().substring(context.getPath().lastIndexOf('#'));
					assertThat(document(context).getElementById(id), notNullValue());
				}
			}
		}
	}

	public static final class NokeeReadMeProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			try {
				NokeeReadMe readme = new NokeeReadMe(getReadmeFile().toPath());
				Path testDirectory = Files.createTempDirectory("nokee");
				Files.write(testDirectory.resolve("readme.html"), readme.renderToHtml().getBytes(StandardCharsets.UTF_8));
				return LinkCheckerUtils.findAllLinks(testDirectory).map(Arguments::of);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	@ParameterizedTest(name = "check code snippet [{0}]")
	@EnumSource(GradleScriptDsl.class)
	void checkCodeSnippet(GradleScriptDsl dsl) throws IOException {
		Path rootDirectory = Files.createTempDirectory("nokee");
		File settingsFile = Files.createFile(rootDirectory.resolve(dsl.getSettingsFileName())).toFile();
		File buildFile = Files.createFile(rootDirectory.resolve(dsl.getBuildFileName())).toFile();
		for (NokeeReadMe.SnippetBlock it : readme.findSnippetBlocks()) {
			switch (it.getType()) {
				case BUILD:
					Files.write(buildFile.toPath(), it.getContent().getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
					break;
				case SETTINGS:
					Files.write(settingsFile.toPath(), it.getContent().getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
					break;
				default:
					throw new RuntimeException("Unrecognized snippet block");
			}
		}

		GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(rootDirectory.toFile()).withGradleVersion("6.2.1").withGradleUserHomeDirectory(rootDirectory.resolve("gradle-user-home").toFile());

		runner.withArgument("help").build();
		runner.withArgument("tasks").build();
	}
}
