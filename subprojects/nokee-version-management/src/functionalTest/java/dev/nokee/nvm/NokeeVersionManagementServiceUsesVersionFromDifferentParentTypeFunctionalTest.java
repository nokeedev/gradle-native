/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.nvm;

import dev.gradleplugins.buildscript.blocks.PluginsDslBlock;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.gradleplugins.testscript.TestGradleBuild;
import dev.gradleplugins.testscript.TestLayout;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import dev.nokee.internal.testing.junit.jupiter.GradleAtLeast;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.function.Consumer;

import static dev.gradleplugins.buildscript.blocks.ArtifactRepositoryStatements.mavenCentral;
import static dev.gradleplugins.buildscript.blocks.GradleBuildScriptBlocks.repositories;
import static dev.nokee.nvm.ProjectFixtures.applyPluginUnderTest;
import static dev.nokee.nvm.ProjectFixtures.expect;
import static dev.nokee.nvm.ProjectFixtures.registerVerifyTask;
import static dev.nokee.nvm.ProjectFixtures.withVersion;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class NokeeVersionManagementServiceUsesVersionFromDifferentParentTypeFunctionalTest {
	@TestDirectory Path testDirectory;
	TestLayout testLayout;
	GradleRunner runner;

	@BeforeEach
	void setUp(GradleRunner runner) {
		testLayout = TestLayout.newBuild(testDirectory)
			.configure(withVersion("0.6.9").andThen(registerVerifyTask()))
			.configure(applyPluginUnderTest())
			.configure(applyPrecompiledScriptPlugin());
		this.runner = runner.withTasks("verify");
	}

	abstract class Tester {
		@Test
		void withoutBuildScansPlugin() {
			runner.build();
		}

		@Test
		void withBuildScansPlugin() {
			runner.publishBuildScans().build();
		}
	}

	@Nested
	class BuildSrcTest {
		@Nested
		class KotlinPrecompiledPluginTest extends Tester {
			@BeforeEach
			void setUp() {
				testLayout.buildSrc(configureKotlinPrecompiledScriptPluginBuild());
			}
		}

		@Nested
		@GradleAtLeast("6.4")
		class GroovyPrecompiledPluginTest extends Tester {
			@BeforeEach
			void setUp() {
				testLayout.buildSrc(configureGroovyPrecompiledScriptPluginBuild());
			}
		}
	}





	@Nested
	class PluginBuildTest {
		@Nested
		class KotlinPrecompiledPluginTest extends Tester {
			@BeforeEach
			void setUp() {
				testLayout.pluginBuild("my-build-src",
					configureKotlinPrecompiledScriptPluginBuild());
			}
		}

		@Nested
		@GradleAtLeast("6.4")
		class GroovyPrecompiledScriptTest extends Tester {
			@BeforeEach
			void setUp() {
				testLayout.pluginBuild("my-build-src",
					configureGroovyPrecompiledScriptPluginBuild());
			}
		}
	}


	@Nested
	class IncludedBuildTest {
		@Nested
		class KotlinPrecompiledScriptPluginTest extends Tester {
			@BeforeEach
			void setUp() {
				testLayout.includeBuild("my-build-src",
					configureKotlinPrecompiledScriptPluginBuild());
			}
		}


		@Nested
		@GradleAtLeast("6.4")
		class GroovyPrecompiledScriptPluginTest extends Tester {
			@BeforeEach
			void setUp() {
				testLayout.includeBuild("my-build-src",
					configureGroovyPrecompiledScriptPluginBuild());
			}
		}
	}

	private static Consumer<TestGradleBuild> configureKotlinPrecompiledScriptPluginBuild() {
		return build -> {
			build.getBuildFile().useKotlinDsl();
			build.configure(applyPluginUnderTest());
			build.getBuildFile().plugins(i -> i.id("kotlin-dsl", PluginsDslBlock.IdStatement.Builder::useKotlinAccessor))
				.append(repositories(it -> it.add(mavenCentral())));
			build.file("src/main/kotlin/foobuild.compile.gradle.kts", "plugins {", "  java", "}");
			build.configure(registerVerifyTask().andThen(expect("0.6.9")));
		};
	}

	@Nonnull
	private static Consumer<TestGradleBuild> configureGroovyPrecompiledScriptPluginBuild() {
		return it -> {
			it.configure(applyPluginUnderTest());
			it.getBuildFile().plugins(i -> i.id("groovy-gradle-plugin"));
			it.file("src/main/groovy/foobuild.compile.gradle", "plugins {", "  id('java')", "}");
			it.configure(registerVerifyTask().andThen(expect("0.6.9")));
		};
	}

	@Nonnull
	private static Consumer<TestGradleBuild> applyPrecompiledScriptPlugin() {
		return build -> build.getBuildFile().plugins(it -> it.id("foobuild.compile"));
	}
}
