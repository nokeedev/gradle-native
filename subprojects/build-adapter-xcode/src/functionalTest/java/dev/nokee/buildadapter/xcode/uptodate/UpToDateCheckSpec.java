/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.buildadapter.xcode.uptodate;

import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.UpToDateCheck;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static dev.gradleplugins.buildscript.blocks.PluginsBlock.plugins;
import static dev.nokee.internal.testing.GradleRunnerMatchers.skipped;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;

@EnabledOnOs(OS.MAC)
@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
public abstract class UpToDateCheckSpec {
	GradleRunner executer;
	@TestDirectory Path testDirectory;

	@BeforeEach
	final void setup(GradleRunner runner) throws IOException {
		new UpToDateCheck().writeToProject(testDirectory);
		setup(testDirectory.resolve("UpToDateCheck.xcodeproj"));

		plugins(it -> it.id("dev.nokee.xcode-build-adapter")).writeTo(testDirectory.resolve("settings.gradle"));
		executer = runner.withTasks("AppDebug").withArgument("-Dsdk=macosx");
		executer.build();
		assertThat(executer.build().getTasks(), everyItem(skipped()));
	}

	void setup(Path location) {}

	static void appendMeaningfulChangeToCFile(Path location) throws IOException {
		// Need to make a meaningful change so the final binary changes (this change will add a .data entry)
		Files.write(location, Arrays.asList("int value = 42;"), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
	}

	static void appendChangeToCHeader(Path location) throws IOException {
		Files.write(location, Arrays.asList("// Some additional line"), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
	}

	static void appendChangeToSwiftFile(Path location) throws IOException {
		Files.write(location, Arrays.asList("// Some additional line"), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
	}

	static void writeColorSet(Path location) throws IOException {
		Files.createDirectories(location);
		Files.write(location.resolve("Contents.json"), Arrays.asList("{\n" +
			"  \"colors\" : [\n" +
			"    {\n" +
			"      \"idiom\" : \"universal\"\n" +
			"    }\n" +
			"  ],\n" +
			"  \"info\" : {\n" +
			"    \"author\" : \"xcode\",\n" +
			"    \"version\" : 1\n" +
			"  }\n" +
			"}"), StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
	}

	Path appDebugProductsDirectory() {
		return buildDirectory().resolve("derivedData/App/Build/Products/Debug");
	}

	Path buildDirectory() {
		return testDirectory.resolve("build/subprojects/UpToDateCheck-1jnf0zhg14ui3");
	}
}
