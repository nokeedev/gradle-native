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
package dev.nokee.platform.base.internal;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.platform.base.internal.ComponentIdentity.of;
import static dev.nokee.platform.base.internal.ComponentIdentity.ofMain;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.*;

class ComponentIdentityTest {
	@Nested
	class OfMainTest {
		private final ComponentIdentity subject = ofMain();

		@Test
		void hasName() {
			assertEquals(ComponentName.of("main"), subject.getName());
		}

		@Test
		void isMainComponent() {
			assertTrue(subject.isMainComponent());
		}

		@Test
		void hasEmptyToString() {
			assertThat(subject, hasToString(""));
		}
	}

	@Nested
	class OfStringTest {
		private final ComponentIdentity subject = of("sopa");

		@Test
		void hasName() {
			assertEquals(ComponentName.of("sopa"), subject.getName());
		}

		@Test
		void isNotMainComponent() {
			assertFalse(subject.isMainComponent());
		}

		@Test
		void hasNameToString() {
			assertThat(subject, hasToString("sopa"));
		}
	}

	@Nested
	class OfNameTest {
		private final ComponentIdentity subject = of(ComponentName.of("pema"));

		@Test
		void hasName() {
			assertEquals(ComponentName.of("pema"), subject.getName());
		}

		@Test
		void isNotMainComponent() {
			assertFalse(subject.isMainComponent());
		}

		@Test
		void hasNameToString() {
			assertThat(subject, hasToString("pema"));
		}
	}

	@Test
	void canCreateMainIdentityFromStringName() {
		assertTrue(of("main").isMainComponent());
	}

	@Test
	void canCreateMainIdentityFromComponentName() {
		assertTrue(of(ComponentName.of("main")).isMainComponent());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(ComponentIdentity.class);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(ofMain(), of("main"), of(ComponentName.of("main")))
			.addEqualityGroup(of("pumi"), of(ComponentName.of("pumi")))
			.testEquals();
	}
}
