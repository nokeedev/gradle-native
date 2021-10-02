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

import org.junit.jupiter.api.Nested;

import static dev.nokee.internal.testing.runnerkit.DependenciesSectionBuilder.classpath;
import static dev.nokee.internal.testing.runnerkit.DependencyNotation.fromString;

class DependenciesBlockTest {
	@Nested
	class BlockWithStringLiteralDependencyTest extends AbstractCodeSegmentTester<DependenciesSection> {
		@Override
		public DependenciesSection subject() {
			return DependenciesSection.dependencies(it -> it.add("foo", "com.example:foo:4.2"));
		}

		@Override
		public String groovyCode() {
			return String.join(System.lineSeparator(),
				"dependencies {",
				"  foo 'com.example:foo:4.2'",
				"}"
			);
		}

		@Override
		public String kotlinCode() {
			return String.join(System.lineSeparator(),
				"dependencies {",
				"  foo(\"com.example:foo:4.2\")",
				"}"
			);
		}
	}

	@Nested
	class BlockWithClasspathDependencyTest extends AbstractCodeSegmentTester<DependenciesSection> {
		@Override
		public DependenciesSection subject() {
			return DependenciesSection.dependencies(classpath(fromString("com.example:foo:4.2")));
		}

		@Override
		public String groovyCode() {
			return String.join(System.lineSeparator(),
				"dependencies {",
				"  classpath 'com.example:foo:4.2'",
				"}"
			);
		}

		@Override
		public String kotlinCode() {
			return String.join(System.lineSeparator(),
				"dependencies {",
				"  classpath(\"com.example:foo:4.2\")",
				"}"
			);
		}
	}
}
