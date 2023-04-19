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
import dev.gradleplugins.buildscript.statements.BlockStatement;
import dev.gradleplugins.buildscript.statements.ExpressionStatement;
import dev.gradleplugins.buildscript.statements.GroupStatement;
import dev.gradleplugins.buildscript.statements.Statement;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.buildadapter.xcode.internal.plugins.DefaultXcodeInstallation;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.GenerateVirtualFileSystemOverlaysTask;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VirtualFileSystemOverlay;
import dev.nokee.buildadapter.xcode.testers.VirtualFileSystemOverlayGenerateTester;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import dev.nokee.xcode.XCProjectReference;
import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.gradleplugins.buildscript.blocks.BuildScriptBlock.classpath;
import static dev.gradleplugins.buildscript.blocks.DependencyNotation.files;
import static dev.gradleplugins.buildscript.statements.Statement.expressionOf;
import static dev.gradleplugins.buildscript.syntax.Syntax.gradleProperty;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovy;
import static dev.gradleplugins.buildscript.syntax.Syntax.invoke;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.buildscript.syntax.Syntax.string;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class GenerateVirtualFileSystemOverlaysFunctionalTests implements VirtualFileSystemOverlayGenerateTester {
	@TestDirectory
	Path testDirectory;

	@BeforeEach
	void givenProjectWithGenerateTask(GradleRunner runner) throws IOException {
		ProjectBlock.builder()
			.buildscript(it -> it.dependencies(classpath(files(runner.getPluginClasspath()))))
			.add(ExpressionStatement.of(groovy(Stream.of(
				"import " + GenerateVirtualFileSystemOverlaysTask.class.getCanonicalName(),
				"import " + XCProjectReference.class.getCanonicalName(),
				"import " + DefaultXcodeInstallation.class.getCanonicalName(),
				"tasks.register('verify', " + GenerateVirtualFileSystemOverlaysTask.class.getSimpleName() + ") {",
				"  parameters {").collect(Collectors.joining("\n")))))
			.add(toOverlays(overlay()))
			.add(ExpressionStatement.of(groovy(Stream.of(
				"    outputFile = new File('" + separatorsToUnix(outputFile().toString()) + "')",
				"  }",
				"}",
				"").collect(Collectors.joining("\n")))))
			.build().writeTo(testDirectory.resolve("build.gradle"));

		runner.withTasks("verify").build();
	}

	static Statement toOverlays(Iterable<VirtualFileSystemOverlay.VirtualDirectory> roots) {
		val builder = GroupStatement.builder();
		for (VirtualFileSystemOverlay.VirtualDirectory root : roots) {
			builder.add(BlockStatement.of(invoke("create", string(root.getName())).alwaysUseParenthesis(), toEntries(root)));
		}
		return BlockStatement.of(literal("overlays"), builder.build());
	}

	static Statement toEntries(Iterable<VirtualFileSystemOverlay.VirtualDirectory.RemappedEntry> entries) {
		val builder = GroupStatement.builder();
		for (VirtualFileSystemOverlay.VirtualDirectory.RemappedEntry entry : entries) {
			builder.add(BlockStatement.of(invoke("create", string(entry.getName())).alwaysUseParenthesis(),
				GroupStatement.of(expressionOf(gradleProperty(literal("location")).assign(string(entry.getExternalContents()))))));
		}
		return BlockStatement.of(literal("entries"), builder.build());
	}

	@Override
	public Path testDirectory() {
		return testDirectory;
	}
}
