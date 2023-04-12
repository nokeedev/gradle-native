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
package dev.nokee.buildadapter.xcode;

import dev.gradleplugins.buildscript.blocks.ProjectBlock;
import dev.gradleplugins.buildscript.statements.ExpressionStatement;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.buildadapter.xcode.internal.plugins.DefaultXcodeInstallation;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.GenerateVirtualFileSystemOverlaysTask;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import dev.nokee.xcode.XCProjectReference;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.gradleplugins.buildscript.blocks.BuildScriptBlock.classpath;
import static dev.gradleplugins.buildscript.blocks.DependencyNotation.files;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovy;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static dev.nokee.internal.testing.GradleRunnerMatchers.skipped;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class GenerateVirtualFileSystemOverlaysTaskUpToDateDetectionFunctionalTests {
	@TestDirectory Path testDirectory;
	GradleRunner executer;

	@BeforeEach
	void givenProjectWithGenerateTask(GradleRunner runner) throws IOException {
		executer = runner.withTasks("verify");

		ProjectBlock.builder()
			.buildscript(it -> it.dependencies(classpath(files(runner.getPluginClasspath()))))
			.add(ExpressionStatement.of(groovy(Stream.of(
				"import " + GenerateVirtualFileSystemOverlaysTask.class.getCanonicalName(),
				"import " + XCProjectReference.class.getCanonicalName(),
				"import " + DefaultXcodeInstallation.class.getCanonicalName(),
				"tasks.register('verify', " + GenerateVirtualFileSystemOverlaysTask.class.getSimpleName() + ") {",
				"  parameters {",
				"    overlays {",
				"      create('/my/root/dir') {",
				"        entries.create('Foo.h') { location = '/my/path/to/Foo.h' }",
				"      }",
				"    }",
				"    outputFile = file('my-framework-headers.yaml')",
				"  }",
				"}",
				"").collect(Collectors.joining("\n")))))
			.build().writeTo(testDirectory.resolve("build.gradle"));

		ensureUpToDate(executer);
	}

	static void ensureUpToDate(GradleRunner executer) {
		executer.build();
		assertThat(executer.build().getTasks(), everyItem(skipped()));
	}

	@Nested
	class OverlaysParameter {
		@Test
		void outOfDateWhenNewOverlayAdded() throws IOException {
			ExpressionStatement.of(groovy(Stream.of(
				"tasks.named('verify', " + GenerateVirtualFileSystemOverlaysTask.class.getSimpleName() + ") {",
				"  parameters {",
				"    overlays.create('/my/other-root/dir') { entries.create('Bar.h') { location = '/my/path/to/Bar.h' } }",
				"  }",
				"}",
				"").collect(Collectors.joining("\n")))).appendTo(testDirectory.resolve("build.gradle"));

			assertThat(executer.build().task(":verify"), outOfDate());
		}

		@Test
		void outOfDateWhenOverlayRemoved() throws IOException {
			ExpressionStatement.of(groovy(Stream.of(
				"tasks.named('verify', " + GenerateVirtualFileSystemOverlaysTask.class.getSimpleName() + ") {",
				"  parameters {",
				"    overlays.clear()",
				"  }",
				"}",
				"").collect(Collectors.joining("\n")))).appendTo(testDirectory.resolve("build.gradle"));

			assertThat(executer.build().task(":verify"), outOfDate());
		}

		@Test
		void outOfDateWhenOverlayEntryAdded() throws IOException {
			ExpressionStatement.of(groovy(Stream.of(
				"tasks.named('verify', " + GenerateVirtualFileSystemOverlaysTask.class.getSimpleName() + ") {",
				"  parameters {",
				"    overlays.named('/my/root/dir') { entries.create('Bar.h') { location = '/my/path/to/Bar.h' } }",
				"  }",
				"}",
				"").collect(Collectors.joining("\n")))).appendTo(testDirectory.resolve("build.gradle"));

			assertThat(executer.build().task(":verify"), outOfDate());
		}

		@Test
		void outOfDateWhenOverlayEntryRemoved() throws IOException {
			ExpressionStatement.of(groovy(Stream.of(
				"tasks.named('verify', " + GenerateVirtualFileSystemOverlaysTask.class.getSimpleName() + ") {",
				"  parameters {",
				"    overlays.named('/my/root/dir') { entries.clear() }",
				"  }",
				"}",
				"").collect(Collectors.joining("\n")))).appendTo(testDirectory.resolve("build.gradle"));

			assertThat(executer.build().task(":verify"), outOfDate());
		}
	}

	@Nested
	class OutputFileParameter {
		@Test
		void outOfDateWhenPathChanged() throws IOException {
			ExpressionStatement.of(groovy(Stream.of(
				"tasks.named('verify', " + GenerateVirtualFileSystemOverlaysTask.class.getSimpleName() + ") {",
				"  parameters {",
				"    outputFile = file('other-my-framework-headers.yaml')",
				"  }",
				"}",
				"").collect(Collectors.joining("\n")))).appendTo(testDirectory.resolve("build.gradle"));

			assertThat(executer.build().task(":verify"), outOfDate());
		}

		@Test
		void outOfDateWhenFileDeleted() throws IOException {
			Files.delete(testDirectory.resolve("my-framework-headers.yaml"));

			assertThat(executer.build().task(":verify"), outOfDate());
		}

		@Test
		void outOfDateWhenFileChanged() throws IOException {
			Files.write(testDirectory.resolve("my-framework-headers.yaml"), Arrays.asList("", "", ""), StandardOpenOption.APPEND);

			assertThat(executer.build().task(":verify"), outOfDate());
		}
	}
}
