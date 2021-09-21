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
import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.runtime.nativebase.MachineArchitecture.forName;
import static dev.nokee.runtime.nativebase.MachineArchitecture.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class MachineArchitectureTest {
	@Nested
	class ObjectFactoryTest implements MachineArchitectureTester {
		@Override
		public MachineArchitecture createSubject(String name) {
			return objectFactory().named(MachineArchitecture.class, name);
		}
	}

	@Nested
	class MethodFactoryTest implements MachineArchitectureTester {
		@Override
		public MachineArchitecture createSubject(String name) {
			return named(name);
		}
	}

	@Nested
	class CanonicalMethodFactoryTest implements MachineArchitectureTester {
		@Override
		public MachineArchitecture createSubject(String name) {
			return forName(name);
		}

		@Override
		public Stream<MachineArchitectureUnderTest> provideMachineArchitecturesUnderTest() {
			// MachineArchitecture#forName use canonical name as machine architecture name
			return MachineArchitectureTester.super.provideMachineArchitecturesUnderTest()
				.map(it -> it.withName(it.getCanonicalName()));
		}
	}

	@Test
	void hasIntel32BitMachineArchitectureCanonicalName() {
		assertThat(MachineArchitecture.X86, equalTo("x86"));
	}

	@Test
	void hasIntel64BitMachineArchitectureCanonicalName() {
		assertThat(MachineArchitecture.X86_64, equalTo("x86-64"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEqualityAgainstObjectFactoryNamedInstance() {
		new EqualsTester()
			.addEqualityGroup((Object[]) equalityGroupFor("x86"))
			.addEqualityGroup((Object[]) equalityGroupFor("X86-64"))
			.addEqualityGroup((Object[]) equalityGroupFor("some-arch"))
			.addEqualityGroup((Object[]) equalityGroupFor("Another-Arch"))
			.testEquals();
	}

	private static MachineArchitecture[] equalityGroupFor(String name) {
		return new MachineArchitecture[] {
			objectFactory().named(MachineArchitecture.class, name),
			forName(name),
			objectFactory().named(MachineArchitecture.class, name)
		};
	}
}
