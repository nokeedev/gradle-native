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

import com.google.common.collect.ImmutableList;
import dev.gradleplugins.buildscript.blocks.ProjectBlock;
import dev.gradleplugins.buildscript.statements.ExpressionStatement;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.MergeVirtualFileSystemOverlaysTask;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.DefaultWritable;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VirtualFileSystemOverlay;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VirtualFileSystemOverlayWriter;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.Writable;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
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
import static dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VirtualFileSystemOverlay.VirtualDirectory.file;
import static dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VirtualFileSystemOverlay.VirtualDirectory.from;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static dev.nokee.internal.testing.GradleRunnerMatchers.skipped;
import static dev.nokee.internal.testing.GradleRunnerMatchers.upToDate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class MergeVirtualFileSystemOverlaysTaskUpToDateDetectionFunctionalTests {
	@TestDirectory Path testDirectory;
	GradleRunner executer;

	@BeforeEach
	void givenProjectWithMergeTask(GradleRunner runner) throws Exception {
		executer = runner.withTasks("verify");
		ProjectBlock.builder()
			.buildscript(it -> it.dependencies(classpath(files(runner.getPluginClasspath()))))
			.add(ExpressionStatement.of(groovy(Stream.of(
				"import " + MergeVirtualFileSystemOverlaysTask.class.getCanonicalName(),
				"tasks.register('verify', " + MergeVirtualFileSystemOverlaysTask.class.getSimpleName() + ") {",
				"  parameters {",
				"    sources.from(file('foo.yaml'), file('bar.yaml'))",
				"    derivedDataPath = file('derived-data')",
				"    outputFile = file('all-products-headers.yaml')",
				"  }",
				"}",
				"").collect(Collectors.joining("\n")))))
			.build().writeTo(testDirectory.resolve("build.gradle"));

		overlayOf(from("Build/Products/Foo.framework/Headers")
			.remap(file("Foo.h", testDirectory.resolve("Foo/Foo.h")))
			.remap(file("MyFoo.h", testDirectory.resolve("Foo/MyFoo.h"))).build())
			.writeTo(testDirectory.resolve("foo.yaml"));
		overlayOf(from("Build/Products/Bar.framework/Headers")
			.remap(file("Bar.h", testDirectory.resolve("Bar/Bar.h"))).build())
			.writeTo(testDirectory.resolve("bar.yaml"));

		ensureUpToDate(executer);
	}

	static void ensureUpToDate(GradleRunner executer) {
		executer.build();
		assertThat(executer.build().getTasks(), everyItem(skipped()));
	}

	@Nested
	class SourcesParameter {
		@Test
		void outOfDateWhenFileChanged() throws Exception {
			// Rewrite foo.yaml to remove MyFoo.h file
			overlayOf(from("Build/Products/Foo.framework/Headers")
				.remap(file("Foo.h", testDirectory.resolve("Foo/Foo.h"))).build())
				.writeTo(testDirectory.resolve("foo.yaml"));

			assertThat(executer.build().task(":verify"), outOfDate());
		}

		@Test
		void outOfDateWhenFileAdded() throws Exception {
			ExpressionStatement.of(groovy(Stream.of(
				"tasks.named('verify', " + MergeVirtualFileSystemOverlaysTask.class.getSimpleName() + ") {",
				"  parameters {",
				"    sources.from(file('far.yaml'))",
				"  }",
				"}",
				"").collect(Collectors.joining("\n")))).appendTo(testDirectory.resolve("build.gradle"));

			overlayOf(from("Build/Products/Far.framework/Headers")
				.remap(file("Far.h", testDirectory.resolve("Far/Far.h"))).build())
				.writeTo(testDirectory.resolve("far.yaml"));

			assertThat(executer.build().task(":verify"), outOfDate());
		}

		@Test
		void ignoresMissingNewFiles() throws Exception {
			ExpressionStatement.of(groovy(Stream.of(
				"tasks.named('verify', " + MergeVirtualFileSystemOverlaysTask.class.getSimpleName() + ") {",
				"  parameters {",
				"    sources.from(file('missing.yaml'))",
				"  }",
				"}",
				"").collect(Collectors.joining("\n")))).appendTo(testDirectory.resolve("build.gradle"));

			assertThat(executer.build().task(":verify"), upToDate());
		}

		@Test
		void outOfDateWhenFileRemoved() throws IOException {
			ExpressionStatement.of(groovy(Stream.of(
				"tasks.named('verify', " + MergeVirtualFileSystemOverlaysTask.class.getSimpleName() + ") {",
				"  parameters {",
				"    sources.setFrom(file('foo.yaml'))",
				"  }",
				"}",
				"").collect(Collectors.joining("\n")))).appendTo(testDirectory.resolve("build.gradle"));

			assertThat(executer.build().task(":verify"), outOfDate());
		}
	}

	@Nested
	class DerivedDataPathParameter {
		@Test
		void outOfDateWhenPathChanged() throws IOException {
			ExpressionStatement.of(groovy(Stream.of(
				"tasks.named('verify', " + MergeVirtualFileSystemOverlaysTask.class.getSimpleName() + ") {",
				"  parameters {",
				"    derivedDataPath = file('my-derived-data')",
				"  }",
				"}",
				"").collect(Collectors.joining("\n")))).appendTo(testDirectory.resolve("build.gradle"));

			assertThat(executer.build().task(":verify"), outOfDate());
		}

		@Test
		void ignoreChangesToDirectoryContent() throws IOException {
			Files.createDirectory(testDirectory.resolve("derived-data"));
			Files.createFile(testDirectory.resolve("derived-data/foo.txt"));

			assertThat(executer.build().task(":verify"), upToDate());
		}
	}

	@Nested
	class OutputFileParameter {
		@Test
		void outOfDateWhenPathChanged() throws IOException {
			ExpressionStatement.of(groovy(Stream.of(
				"tasks.named('verify', " + MergeVirtualFileSystemOverlaysTask.class.getSimpleName() + ") {",
				"  parameters {",
				"    outputFile = file('other-my-framework-headers.yaml')",
				"  }",
				"}",
				"").collect(Collectors.joining("\n")))).appendTo(testDirectory.resolve("build.gradle"));

			assertThat(executer.build().task(":verify"), outOfDate());
		}

		@Test
		void outOfDateWhenFileRemoved() throws IOException {
			Files.delete(testDirectory.resolve("all-products-headers.yaml"));

			assertThat(executer.build().task(":verify"), outOfDate());
		}

		@Test
		void outOfDateWhenFileChanged() throws IOException {
			Files.write(testDirectory.resolve("all-products-headers.yaml"), Arrays.asList("", "", ""), StandardOpenOption.APPEND);

			assertThat(executer.build().task(":verify"), outOfDate());
		}
	}

	private Writable<VirtualFileSystemOverlay> overlayOf(VirtualFileSystemOverlay.VirtualDirectory virtualDirectory) {
		return new DefaultWritable<>(VirtualFileSystemOverlayWriter::new, new VirtualFileSystemOverlay(ImmutableList.of(virtualDirectory)));
	}
}
