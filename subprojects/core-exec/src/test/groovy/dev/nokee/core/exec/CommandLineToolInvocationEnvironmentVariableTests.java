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
package dev.nokee.core.exec;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class CommandLineToolInvocationEnvironmentVariableTests {
	@Nested
	class PutOrReplace {
		@Nested
		class WhenKeyAlreadyExists {
			CommandLineToolInvocationEnvironmentVariables subject = new CommandLineToolInvocationEnvironmentVariables(of("k1", "v1"));

			@Test
			void replacesEnvironmentVariableWithNewValue() {
				assertThat(subject.putOrReplace("k1", "new-v1"),
					equalTo(new CommandLineToolInvocationEnvironmentVariables(of("k1", "new-v1"))));
			}
		}

		@Nested
		class WhenKeyExists {
			CommandLineToolInvocationEnvironmentVariables subject = new CommandLineToolInvocationEnvironmentVariables(of("some-key", "some-value"));

			@Test
			void addsEnvironmentVariableWithNewValue() {
				assertThat(subject.putOrReplace("k1", "v1"),
					equalTo(new CommandLineToolInvocationEnvironmentVariables(of("some-key", "some-value", "k1", "v1"))));
			}
		}
	}
}
