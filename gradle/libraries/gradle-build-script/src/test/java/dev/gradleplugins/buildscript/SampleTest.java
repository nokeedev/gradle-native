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
package dev.gradleplugins.buildscript;

import dev.gradleplugins.buildscript.blocks.GradleBlock;
import dev.gradleplugins.buildscript.blocks.PluginManagementBlock;
import dev.gradleplugins.buildscript.blocks.ProjectBlock;
import dev.gradleplugins.buildscript.blocks.SettingsBlock;
import dev.gradleplugins.buildscript.statements.ExpressionStatement;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.buildscript.BuildScriptMatchers.groovyOf;
import static dev.gradleplugins.buildscript.statements.Statement.block;
import static dev.gradleplugins.buildscript.statements.Statement.expressionOf;
import static dev.gradleplugins.buildscript.syntax.Syntax.*;
import static org.hamcrest.MatcherAssert.assertThat;

class SampleTest {
	@Test
	void applyMultiplePluginsOnProjectUsingPluginsBlock() {
		assertThat(ProjectBlock.builder().plugins(t -> t.id("dev.nokee.c-application").id("dev.nokee.xcode-ide")).build(), groovyOf(
			"plugins {",
			"  id 'dev.nokee.c-application'",
			"  id 'dev.nokee.xcode-ide'",
			"}"
		));
	}

	@Test
	void configureRootProjectNameOnSettings() {
		assertThat(SettingsBlock.builder().add(ExpressionStatement.of(property(literal("rootProject.name")).assign(string("some-project-name")))).build(), groovyOf("rootProject.name = 'some-project-name'"));
	}

	@Test
	void configureTargetMachinesOnNativeLibraryProject() {
		assertThat(ProjectBlock.builder()
			.plugins(t -> t.id("dev.nokee.c-library"))
			.add(block("library", ExpressionStatement.of(gradleProperty(literal("targetMachines")).assign(setOf(literal("machines.linux"), literal("machines.windows"), literal("machines.macOS"))))))
			.build(),
			groovyOf(
				"plugins {",
				"  id 'dev.nokee.c-library'",
				"}",
				"",
				"library {",
				"  targetMachines = [machines.linux, machines.windows, machines.macOS]",
				"}"
			)
		);
	}

	@Test
	void includesMultipleProjectOnSettings() {
		assertThat(SettingsBlock.builder()
			.include("java-jni-greeter")
			.include("cpp-jni-greeter")
			.include("java-loader","cpp-greeter")
			.build(),
			groovyOf(
				"include 'java-jni-greeter'",
				"include 'cpp-jni-greeter'",
				"include('java-loader', 'cpp-greeter')"
			)
		);
	}

	@Test
	void configurePluginManagementFromSettings() {
		assertThat(SettingsBlock.builder()
			.pluginManagement(this::configureNokeePluginManagement)
			.build(),
			groovyOf(
				"pluginManagement {",
				"  repositories {",
				"    gradlePluginPortal()",
				"    maven {", // TODO: Collapse single value block
				"      url = uri('https://repo.nokee.dev/release')",
				"    }",
				"  }",
				"  def nokeeVersion = '0.5.0'",
				"  resolutionStrategy {",
				"    eachPlugin {",
				"      if (requested.id.id.startsWith('dev.nokee.')) {",
				"        useVersion(nokeeVersion)",
				"      }",
				"    }",
				"  }",
				"}"
			)
		);
	}

	@Test
	void configurePluginManagementFromInitScript() {
		assertThat(GradleBlock.builder()
				.settingsEvaluated(t -> t.pluginManagement(this::configureNokeePluginManagement))
				.build(),
			groovyOf(
				"settingsEvaluated {",
				"  it.pluginManagement {",
				"    repositories {",
				"      gradlePluginPortal()",
				"      maven {", // TODO: Collapse single value block
				"        url = uri('https://repo.nokee.dev/release')",
				"      }",
				"    }",
				"    def nokeeVersion = '0.5.0'",
				"    resolutionStrategy {",
				"      eachPlugin {",
				"        if (requested.id.id.startsWith('dev.nokee.')) {",
				"          useVersion(nokeeVersion)",
				"        }",
				"      }",
				"    }",
				"  }",
				"}"
			)
		);
	}

	private void configureNokeePluginManagement(PluginManagementBlock.Builder builder) {
		builder.repositories(repos -> {
			repos.gradlePluginPortal();
			repos.maven("https://repo.nokee.dev/release");
		});
		builder.add(expressionOf(var("nokeeVersion").assign(string("0.5.0"))));
		builder.add(block("resolutionStrategy", block("eachPlugin", block("if (requested.id.id.startsWith('dev.nokee.'))", expressionOf(literal("useVersion(nokeeVersion)"))))));
	}
}
