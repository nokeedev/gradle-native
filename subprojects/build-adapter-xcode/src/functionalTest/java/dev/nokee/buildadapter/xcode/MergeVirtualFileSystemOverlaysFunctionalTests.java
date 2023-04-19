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
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.MergeVirtualFileSystemOverlaysTask;
import dev.nokee.buildadapter.xcode.testers.VirtualFileSystemOverlayMergeTester;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;
import java.util.stream.Stream;

import static dev.gradleplugins.buildscript.blocks.BuildScriptBlock.classpath;
import static dev.gradleplugins.buildscript.blocks.DependencyNotation.files;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovy;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class MergeVirtualFileSystemOverlaysFunctionalTests implements VirtualFileSystemOverlayMergeTester {
	@TestDirectory Path testDirectory;

	@BeforeEach
	void givenProjectWithMergeTask(GradleRunner runner) throws Exception {
		ProjectBlock.builder()
			.buildscript(it -> it.dependencies(classpath(files(runner.getPluginClasspath()))))
			.add(ExpressionStatement.of(groovy(Stream.of(
				"import " + MergeVirtualFileSystemOverlaysTask.class.getCanonicalName(),
				"tasks.register('verify', " + MergeVirtualFileSystemOverlaysTask.class.getSimpleName() + ") {",
				"  parameters {",
				"    sources.from(" + inputFiles().stream().map(it -> "new File('" + separatorsToUnix(it.toString()) + "')").collect(joining(", ")) + ")",
				"    derivedDataPath = new File('" + separatorsToUnix(derivedDataPath().toString()) + "')",
				"    outputFile = new File('" + separatorsToUnix(outputFile().toString()) + "')",
				"  }",
				"}",
				"").collect(joining("\n")))))
			.build().writeTo(testDirectory.resolve("build.gradle"));

		runner.withTasks("verify").build();
	}

	@Override
	public Path testDirectory() {
		return testDirectory;
	}
}
