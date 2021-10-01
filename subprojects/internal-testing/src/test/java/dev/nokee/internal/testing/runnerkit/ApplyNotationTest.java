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
package dev.nokee.internal.testing.runnerkit;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.junit.jupiter.api.Nested;

class ApplyNotationTest {
	@Nested
	class PluginIdNotationTest extends AbstractCodeSegmentTester<ApplySection.ApplySectionNotation> {
		@Override
		public ApplySection.ApplySectionNotation subject() {
			return ApplySection.ApplySectionNotation.plugin("some.plugin.id");
		}

		@Override
		public String groovyCode() {
			return "plugin: 'some.plugin.id'";
		}

		@Override
		public String kotlinCode() {
			return "plugin = \"some.plugin.id\"";
		}
	}

	@Nested
	class PluginTypeNotationTest extends AbstractCodeSegmentTester<ApplySection.ApplySectionNotation> {
		@Override
		public ApplySection.ApplySectionNotation subject() {
			return ApplySection.ApplySectionNotation.plugin(SomePlugin.class);
		}

		@Override
		public String groovyCode() {
			return "plugin: Class.forName('dev.nokee.internal.testing.runnerkit.ApplyNotationTest$SomePlugin')";
		}

		@Override
		public String kotlinCode() {
			return "plugin = Class.forName(\"dev.nokee.internal.testing.runnerkit.ApplyNotationTest$SomePlugin\")";
		}
	}

	private interface SomePlugin extends Plugin<Project> {}

	@Nested
	class FromNotationTest extends AbstractCodeSegmentTester<ApplySection.ApplySectionNotation> {
		@Override
		public ApplySection.ApplySectionNotation subject() {
			return ApplySection.ApplySectionNotation.from("some/path/to/script");
		}

		@Override
		public String groovyCode() {
			return "from: 'some/path/to/script.gradle'";
		}

		@Override
		public String kotlinCode() {
			return "from = \"some/path/to/script.gradle.kts\"";
		}
	}
}
