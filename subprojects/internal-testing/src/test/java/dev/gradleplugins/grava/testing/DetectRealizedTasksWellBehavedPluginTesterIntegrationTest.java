/*
 * Copyright 2021 the original author or authors.
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
package dev.gradleplugins.grava.testing;

import dev.nokee.internal.testing.WellBehavedPluginTester;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DetectRealizedTasksWellBehavedPluginTesterIntegrationTest {
	@Test
	void failsIfProjectPluginRealizedTasks() {
		val ex = assertThrows(AssertionError.class, new WellBehavedPluginTester().pluginClass(RealizedTasksProjectPlugin.class).supportedTarget(WellBehavedPluginTester.SupportedTarget.Project).doesNotWellBehaveWhenAppliedToUnsupportedTarget()::testWellBehavedPlugin);
		assertThat(ex.getMessage(), allOf(startsWith("Plugin is not well-behaved"), containsString("does not realize task")));
	}

	@Test
	void failsIfSettingsPluginRealizedTasks() {
		val ex = assertThrows(AssertionError.class, new WellBehavedPluginTester().pluginClass(RealizedTasksSettingsPlugin.class).supportedTarget(WellBehavedPluginTester.SupportedTarget.Settings).doesNotWellBehaveWhenAppliedToUnsupportedTarget()::testWellBehavedPlugin);
		assertThat(ex.getMessage(), allOf(startsWith("Plugin is not well-behaved"), containsString("does not realize task")));
	}

	@Test
	void failsIfInitPluginRealizedTasks() {
		val ex = assertThrows(AssertionError.class, new WellBehavedPluginTester().pluginClass(RealizedTasksInitPlugin.class).supportedTarget(WellBehavedPluginTester.SupportedTarget.Init).doesNotWellBehaveWhenAppliedToUnsupportedTarget()::testWellBehavedPlugin);
		assertThat(ex.getMessage(), allOf(startsWith("Plugin is not well-behaved"), containsString("does not realize task")));
	}

	@Test
	void failsIfPluginRealizedTasksInSubprojects() {
		val ex = assertThrows(AssertionError.class, new WellBehavedPluginTester().pluginClass(RealizedSubprojectsTasksPlugin.class).supportedTarget(WellBehavedPluginTester.SupportedTarget.Project).doesNotWellBehaveWhenAppliedToUnsupportedTarget()::testWellBehavedPlugin);
		assertThat(ex.getMessage(), allOf(startsWith("Plugin is not well-behaved"), containsString("does not realize task")));
	}

	public static class RealizedSubprojectsTasksPlugin implements Plugin<Project> {
		@Override
		public void apply(Project target) {
			target.subprojects(proj -> proj.getTasks().create("foo"));
		}
	}

	public static class RealizedTasksProjectPlugin implements Plugin<Project> {
		@Override
		public void apply(Project target) {
			target.getTasks().create("foo");
		}
	}

	public static class RealizedTasksSettingsPlugin implements Plugin<Settings> {
		@Override
		public void apply(Settings target) {
			target.getGradle().rootProject(it -> it.getTasks().create("foo"));
		}
	}

	public static class RealizedTasksInitPlugin implements Plugin<Gradle> {
		@Override
		public void apply(Gradle target) {
			target.rootProject(it -> it.getTasks().create("foo"));
		}
	}
}
