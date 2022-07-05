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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.names.ElementName;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.names.ElementName.of;
import static dev.nokee.platform.base.internal.dependencies.DependencyBuckets.defaultDisplayName;
import static dev.nokee.platform.base.internal.dependencies.DependencyBuckets.defaultDisplayNameOfDeclarableBucket;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class DependencyBucketDefaultDisplayNameTest {
	@Test
	void canGenerateNameForSingleWordBucketName() {
		assertThat(defaultDisplayNameOfDeclarableBucket(of("implementation")), equalTo("implementation dependencies"));
	}

	@Test
	void canGenerateNameForTwoWordsBucketName() {
		assertThat(defaultDisplayNameOfDeclarableBucket(of("compileOnly")), equalTo("compile only dependencies"));
		assertThat(defaultDisplayNameOfDeclarableBucket(of("linkOnly")), equalTo("link only dependencies"));

		assertThat(defaultDisplayName(of("runtimeElements")), equalTo("runtime elements"));
		assertThat(defaultDisplayName(of("linkLibraries")), equalTo("link libraries"));
	}

	@Test
	void canGenerateNameForThreeWordsBucketName() {
		assertThat(defaultDisplayName(of("headerSearchPaths")), equalTo("header search paths"));
		assertThat(defaultDisplayName(of("importSwiftModules")), equalTo("import swift modules"));
	}

	@Test
	void alwaysCapitalizeApiWord() {
		assertThat(defaultDisplayNameOfDeclarableBucket(of("api")), equalTo("API dependencies"));

		assertThat(defaultDisplayName(of("apiElements")), equalTo("API elements"));
	}

	@Test
	void alwaysCapitalizeJvmWord() {
		assertThat(defaultDisplayNameOfDeclarableBucket(of("jvmImplementation")), equalTo("JVM implementation dependencies"));
		assertThat(defaultDisplayName(of("jvmRuntimeOnly")), equalTo("JVM runtime only dependencies"));
	}

	@Test
	void alwaysCapitalizeApiAndJvmWord() {
		assertThat(defaultDisplayName(of("jvmApiElements")), equalTo("JVM API elements"));
	}
}
