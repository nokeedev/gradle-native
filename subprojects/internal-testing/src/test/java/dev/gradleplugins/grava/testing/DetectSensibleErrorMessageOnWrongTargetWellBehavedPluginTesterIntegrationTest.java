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
import static org.junit.jupiter.api.Assertions.*;

class DetectSensibleErrorMessageOnWrongTargetWellBehavedPluginTesterIntegrationTest {

	@Test
	void failsIfPluginShowsDefaultErrorsWhenProjectPluginAppliesToWrongTarget() {
		val ex = assertThrows(AssertionError.class, new WellBehavedPluginTester().pluginClass(DefaultProjectPlugin.class).supportedTarget(WellBehavedPluginTester.SupportedTarget.Project)::testWellBehavedPlugin);
		assertThat(ex.getMessage(), allOf(startsWith("Plugin is not well-behaved"), containsString("throws exception when applying plugin on wrong target, e.g. Settings"), containsString("throws exception when applying plugin on wrong target, e.g. Init")));
	}

	@Test
	void failsIfPluginShowsDefaultErrorsWhenSettingsPluginAppliesToWrongTarget() {
		val ex = assertThrows(AssertionError.class, new WellBehavedPluginTester().pluginClass(DefaultSettingsPlugin.class).supportedTarget(WellBehavedPluginTester.SupportedTarget.Settings)::testWellBehavedPlugin);
		assertThat(ex.getMessage(), allOf(startsWith("Plugin is not well-behaved"), containsString("throws exception when applying plugin on wrong target, e.g. Project"), containsString("throws exception when applying plugin on wrong target, e.g. Init")));
	}

	@Test
	void failsIfPluginShowsDefaultErrorsWhenInitPluginAppliesToWrongTarget() {
		val ex = assertThrows(AssertionError.class, new WellBehavedPluginTester().pluginClass(DefaultInitPlugin.class).supportedTarget(WellBehavedPluginTester.SupportedTarget.Init)::testWellBehavedPlugin);
		assertThat(ex.getMessage(), allOf(startsWith("Plugin is not well-behaved"), containsString("throws exception when applying plugin on wrong target, e.g. Project"), containsString("throws exception when applying plugin on wrong target, e.g. Settings")));
	}

	@Test
	void doesNotFailIfPluginSafelyHandleApplyToWrongTarget() {
		assertAll(
			() -> assertDoesNotThrow(new WellBehavedPluginTester().pluginClass(WellBehavedProjectPlugin.class).supportedTarget(WellBehavedPluginTester.SupportedTarget.Project)::testWellBehavedPlugin),
			() -> assertDoesNotThrow(new WellBehavedPluginTester().pluginClass(WellBehavedSettingsPlugin.class).supportedTarget(WellBehavedPluginTester.SupportedTarget.Settings)::testWellBehavedPlugin),
			() -> assertDoesNotThrow(new WellBehavedPluginTester().pluginClass(WellBehavedInitPlugin.class).supportedTarget(WellBehavedPluginTester.SupportedTarget.Init)::testWellBehavedPlugin)
		);
	}

	@Test
	void canIgnoreMisbehavingPluginWhenAppliedToWrongTarget() {
		assertAll(
			() -> assertDoesNotThrow(new WellBehavedPluginTester().pluginClass(DefaultProjectPlugin.class).doesNotWellBehaveWhenAppliedToUnsupportedTarget().supportedTarget(WellBehavedPluginTester.SupportedTarget.Project)::testWellBehavedPlugin),
			() -> assertDoesNotThrow(new WellBehavedPluginTester().pluginClass(DefaultSettingsPlugin.class).doesNotWellBehaveWhenAppliedToUnsupportedTarget().supportedTarget(WellBehavedPluginTester.SupportedTarget.Settings)::testWellBehavedPlugin),
			() -> assertDoesNotThrow(new WellBehavedPluginTester().pluginClass(DefaultInitPlugin.class).doesNotWellBehaveWhenAppliedToUnsupportedTarget().supportedTarget(WellBehavedPluginTester.SupportedTarget.Init)::testWellBehavedPlugin)
		);
	}

	@Test
	void doesNotValidateWellBehaveCrossTargetSupportForDefaultPluginTests() {
		// By default, the plugin tester targets Project and, in general, project plugin don't behave well across targets.
		assertDoesNotThrow(new WellBehavedPluginTester().pluginClass(DefaultProjectPlugin.class)::testWellBehavedPlugin);
	}

	public static class DefaultProjectPlugin implements Plugin<Project> {
		@Override
		public void apply(Project target) {}
	}

	public static class DefaultSettingsPlugin implements Plugin<Settings> {
		@Override
		public void apply(Settings target) {}
	}

	public static class DefaultInitPlugin implements Plugin<Gradle> {
		@Override
		public void apply(Gradle target) {}
	}

	public static class WellBehavedProjectPlugin implements Plugin<Object> {
		@Override
		public void apply(Object target) {
			if (target instanceof Project) {
				// a project plugin
			} else {
				throw new RuntimeException("This plugin is not applicable to '" + target.getClass() + "'");
			}
		}
	}

	public static class WellBehavedSettingsPlugin implements Plugin<Object> {
		@Override
		public void apply(Object target) {
			if (target instanceof Settings) {
				// a project plugin
			} else {
				throw new RuntimeException("This plugin is not applicable to '" + target.getClass() + "'");
			}
		}
	}

	public static class WellBehavedInitPlugin implements Plugin<Object> {
		@Override
		public void apply(Object target) {
			if (target instanceof Gradle) {
				// a project plugin
			} else {
				throw new RuntimeException("This plugin is not applicable to '" + target.getClass() + "'");
			}
		}
	}
}
