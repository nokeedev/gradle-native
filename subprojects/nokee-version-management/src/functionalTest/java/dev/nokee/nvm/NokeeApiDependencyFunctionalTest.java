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

import dev.gradleplugins.buildscript.blocks.DependenciesBlock;
import dev.gradleplugins.buildscript.blocks.DependencyNotation;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import dev.nokee.nvm.fixtures.TestLayout;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;
import java.util.function.Consumer;

import static dev.gradleplugins.buildscript.blocks.DependenciesBlock.dependencies;
import static dev.gradleplugins.buildscript.blocks.PluginsBlock.plugins;
import static dev.nokee.nvm.ProjectFixtures.applyPluginUnderTest;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class NokeeApiDependencyFunctionalTest {
	@TestDirectory Path testDirectory;

	@Test
	void canAddNokeeApiDependencyInKotlinDslScript(GradleRunner runner) {
		TestLayout.newBuild(testDirectory).configure(applyPluginUnderTest()).configure(layout -> {
			layout.getBuildFile().useKotlinDsl()
				.append(plugins(it -> it.id("java")))
				.append(dependencies(implementation(nokeeApi())));
		});
		runner.withTasks("help").build();
	}

	@Test
	void canAddNokeeApiDependencyInGroovyDslScript(GradleRunner runner) {
		TestLayout.newBuild(testDirectory).configure(applyPluginUnderTest()).configure(layout -> {
			layout.getBuildFile().useGroovyDsl()
				.append(plugins(it -> it.id("java")))
				.append(dependencies(implementation(nokeeApi())));
		});
		runner.withTasks("help").build();
	}

	private static Consumer<DependenciesBlock.Builder> implementation(DependencyNotation notation) {
		return it -> it.add("implementation", notation);
	}

	private static DependencyNotation nokeeApi() {
		return DependencyNotation.call("nokeeApi");
	}
}
