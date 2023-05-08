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
package dev.nokee.buildadapter.xcode.swiftpm;

import dev.gradleplugins.buildscript.blocks.ProjectBlock;
import dev.gradleplugins.buildscript.statements.ExpressionStatement;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.SwiftPackageProductDependencies;
import dev.nokee.buildadapter.xcode.internal.plugins.GenerateSwiftPackageManifestTask;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.gradleplugins.buildscript.blocks.BuildScriptBlock.classpath;
import static dev.gradleplugins.buildscript.blocks.DependencyNotation.files;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovy;
import static dev.nokee.internal.testing.GradleRunnerMatchers.succeed;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.UP_TO_NEXT_MAJOR_VERSION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.iterableWithSize;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class GenerateSwiftPackageManifestTaskFunctionalTests {
	@TestDirectory Path testDirectory;
	GradleRunner executer;

	@BeforeEach
	void given(GradleRunner runner) throws IOException {
		executer = runner.withTasks("verify");

		new SwiftPackageProductDependencies().writeToProject(testDirectory);

		ProjectBlock.builder()
			.buildscript(it -> it.dependencies(classpath(files(runner.getPluginClasspath()))))
			.add(ExpressionStatement.of(groovy(Stream.of(
				"import " + GenerateSwiftPackageManifestTask.class.getCanonicalName(),
				"tasks.register('verify', " + GenerateSwiftPackageManifestTask.class.getSimpleName() + ") {",
				"  parameters {",
				"    project.location = file('SwiftPackageProductDependencies.xcodeproj')",
				"    manifestFile = file('swift-pm.manifest')",
				"  }",
				"}",
				"").collect(Collectors.joining("\n")))))
			.build().writeTo(testDirectory.resolve("build.gradle"));
	}

	@Nested
	class NoSwiftPackageTests {
		List<XCSwiftPackageProductDependency> result;

		@BeforeEach
		void given() throws IOException {
			ExpressionStatement.of(groovy(Stream.of(
				"tasks.named('verify', " + GenerateSwiftPackageManifestTask.class.getSimpleName() + ") {",
				"  parameters {",
				"    targetName = 'WithoutSwiftProductDependencies'",
				"  }",
				"}",
				"").collect(Collectors.joining("\n")))).appendTo(testDirectory.resolve("build.gradle"));

			assertThat(executer.build().task(":verify"), succeed());

			try (val inStream = Files.newInputStream(testDirectory.resolve("swift-pm.manifest"))) {
				result = SerializationUtils.deserialize(inStream);
			}
		}

		@Test
		void hasNoSwiftPackageProductDependencies() {
			assertThat(result, emptyIterable());
		}
	}

	@Nested
	class WithSwiftPackageTests {
		List<XCSwiftPackageProductDependency> result;

		@BeforeEach
		void given() throws IOException {
			ExpressionStatement.of(groovy(Stream.of(
				"tasks.named('verify', " + GenerateSwiftPackageManifestTask.class.getSimpleName() + ") {",
				"  parameters {",
				"    targetName = 'WithSwiftProductDependencies'",
				"  }",
				"}",
				"").collect(Collectors.joining("\n")))).appendTo(testDirectory.resolve("build.gradle"));

			assertThat(executer.build().task(":verify"), succeed());

			try (val inStream = Files.newInputStream(testDirectory.resolve("swift-pm.manifest"))) {
				result = SerializationUtils.deserialize(inStream);
			}
		}

		@Test
		void hasExpectedSwiftPackageProductDependencies() {
			assertThat(result, iterableWithSize(2));
			assertThat(result.get(0).getProductName(), equalTo("NIO"));
			assertThat(result.get(0).getPackageReference().getRepositoryUrl(), equalTo("https://github.com/apple/swift-nio.git"));
			assertThat(result.get(0).getPackageReference().getRequirement().getKind(), equalTo(UP_TO_NEXT_MAJOR_VERSION));
			assertThat(result.get(1).getProductName(), equalTo("ArgumentParser"));
			assertThat(result.get(1).getPackageReference().getRepositoryUrl(), equalTo("https://github.com/apple/swift-argument-parser.git"));
			assertThat(result.get(1).getPackageReference().getRequirement().getKind(), equalTo(UP_TO_NEXT_MAJOR_VERSION));
		}
	}
}
