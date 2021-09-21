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
package dev.nokee.runtime.nativebase;

import com.google.common.collect.Streams;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.runtime.nativebase.MachineArchitectureTestUtils.common32BitMachineArchitectures;
import static dev.nokee.runtime.nativebase.MachineArchitectureTestUtils.common64BitMachineArchitectures;
import static org.hamcrest.MatcherAssert.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface MachineArchitectureTester extends NamedValueTester<MachineArchitecture>, KnownMachineArchitecture32BitTester, KnownMachineArchitecture64BitTester, UnknownMachineArchitectureTester {
	MachineArchitecture createSubject(String name);

	default Stream<String> knownValues() {
		return provideMachineArchitecturesUnderTest().map(MachineArchitectureUnderTest::getName);
	}

	default Stream<MachineArchitectureUnderTest> provideMachineArchitecturesUnderTest() {
		return Streams.concat(common32BitMachineArchitectures(), common64BitMachineArchitectures());
	}

	@ParameterizedTest(name = "has name [{arguments}]")
	@MethodSource("provideMachineArchitecturesUnderTest")
	default void hasName(MachineArchitectureUnderTest architecture) {
		assertThat(createSubject(architecture.getName()), named(architecture.getName()));
	}

	@ParameterizedTest(name = "has canonical name [{arguments}]")
	@MethodSource("provideMachineArchitecturesUnderTest")
	default void hasCanonicalName(MachineArchitectureUnderTest architecture) {
		architecture.assertCanonicalName(createSubject(architecture.getName()));
	}
}
