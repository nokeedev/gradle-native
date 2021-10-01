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

import java.io.File;
import java.util.Arrays;

class DependencyNotationTest {
	@Nested
	class FromStringNotationTest extends AbstractCodeSegmentTester<DependencyNotation> {
		@Override
		public DependencyNotation subject() {
			return DependencyNotation.fromString("dev.nokee:platform-jni:0.6.0");
		}

		@Override
		public String groovyCode() {
			return "'dev.nokee:platform-jni:0.6.0'";
		}

		@Override
		public String kotlinCode() {
			return "\"dev.nokee:platform-jni:0.6.0\"";
		}
	}

	@Nested
	class FileCollectionNotationTest extends AbstractCodeSegmentTester<DependencyNotation> {
		private final File f0 = new File("foo");
		private final File f1 = new File("bar");
		private final File f2 = new File("far");

		@Override
		public DependencyNotation subject() {
			return DependencyNotation.files(Arrays.asList(f0, f1, f2));
		}

		@Override
		public String groovyCode() {
			return "files('" + f0.toURI() + "', '" + f1.toURI() + "', '" + f2.toURI() + "')";
		}

		@Override
		public String kotlinCode() {
			return "files(\"" + f0.toURI() + "\", \"" + f1.toURI() + "\", \"" + f2.toURI() + "\")";
		}
	}
}
