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

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.runtime.nativebase.OperatingSystemFamily.forName;
import static dev.nokee.runtime.nativebase.OperatingSystemFamily.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class OperatingSystemFamilyTest {
	@Nested
	class ObjectFactoryTest implements OperatingSystemFamilyTester {
		@Override
		public OperatingSystemFamily createSubject(String name) {
			return objectFactory().named(OperatingSystemFamily.class, name);
		}
	}

	@Nested
	class MethodFactoryTest implements OperatingSystemFamilyTester {
		@Override
		public OperatingSystemFamily createSubject(String name) {
			return named(name);
		}
	}

	@Nested
	class CanonicalMethodFactoryTest implements OperatingSystemFamilyTester {
		@Override
		public OperatingSystemFamily createSubject(String name) {
			return forName(name);
		}

		@Override
		public Stream<OperatingSystemFamilyUnderTest> provideOperatingSystemFamiliesUnderTest() {
			// OperatingSystemFamily#forName use canonical name as operating system family name
			return OperatingSystemFamilyTester.super.provideOperatingSystemFamiliesUnderTest()
				.map(it -> it.withName(it.getCanonicalName()));
		}
	}

	@Test
	void hasWindowsOperatingSystemFamilyCanonicalName() {
		assertThat(OperatingSystemFamily.WINDOWS, equalTo("windows"));
	}

	@Test
	void hasLinuxOperatingSystemFamilyCanonicalName() {
		assertThat(OperatingSystemFamily.LINUX, equalTo("linux"));
	}

	@Test
	void hasMacOsOperatingSystemFamilyCanonicalName() {
		assertThat(OperatingSystemFamily.MACOS, equalTo("macos"));
	}

	@Test
	void hasSolarisOperatingSystemFamilyCanonicalName() {
		assertThat(OperatingSystemFamily.SOLARIS, equalTo("solaris"));
	}

	@Test
	void hasIosOperatingSystemFamilyCanonicalName() {
		assertThat(OperatingSystemFamily.IOS, equalTo("ios"));
	}

	@Test
	void hasFreeBsdOperatingSystemFamilyCanonicalName() {
		assertThat(OperatingSystemFamily.FREE_BSD, equalTo("freebsd"));
	}

	@Test
	void hasHpUxOperatingSystemFamilyCanonicalName() {
		assertThat(OperatingSystemFamily.HP_UX, equalTo("hpux"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEqualityAgainstObjectFactoryNamedInstance() {
		new EqualsTester()
			.addEqualityGroup((Object[]) equalityGroupFor("windows"))
			.addEqualityGroup((Object[]) equalityGroupFor("MacOS"))
			.addEqualityGroup((Object[]) equalityGroupFor("some-os"))
			.addEqualityGroup((Object[]) equalityGroupFor("Another-OS"))
			.testEquals();
	}

	private static OperatingSystemFamily[] equalityGroupFor(String name) {
		return new OperatingSystemFamily[] {
			objectFactory().named(OperatingSystemFamily.class, name),
			forName(name),
			objectFactory().named(OperatingSystemFamily.class, name)
		};
	}
}
