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

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Arrays.asList;

class CommandLineToolArgumentsEqualityTests {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(new CommandLineToolArguments(), new CommandLineToolArguments(of()))
			.addEqualityGroup(new CommandLineToolArguments(of("a1", "a2")), new CommandLineToolArguments(asList("a1", "a2")))
			.addEqualityGroup(new CommandLineToolArguments(of("a2", "a1")))
			.testEquals();
	}
}
