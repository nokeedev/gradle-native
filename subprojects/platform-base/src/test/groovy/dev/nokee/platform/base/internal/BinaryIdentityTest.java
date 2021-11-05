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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BinaryIdentityTest {
	@Nested
	class NameOnlyTest {
		private final BinaryIdentity subject = BinaryIdentity.of("veke");

		@Test
		void hasName() {
			assertEquals(BinaryName.of("veke"), subject.getName());
		}

		@Test
		void hasDisplayName() {
			assertEquals("binary", subject.getDisplayName());
		}

		@Test
		void isNotMainBinary() {
			assertFalse(subject.isMain());
		}
	}

	@Nested
	class WithDisplayNameTest {
		private final BinaryIdentity subject = BinaryIdentity.of("kapi", "Xibe suda");

		@Test
		void hasName() {
			assertEquals(BinaryName.of("kapi"), subject.getName());
		}

		@Test
		void hasDisplayName() {
			assertEquals("Xibe suda", subject.getDisplayName());
		}

		@Test
		void isNotMainBinary() {
			assertFalse(subject.isMain());
		}
	}

	@Nested
	class MainBinaryTest {
		private final BinaryIdentity subject = BinaryIdentity.ofMain("leve", "huce qafa");

		@Test
		void hasName() {
			assertEquals(BinaryName.of("leve"), subject.getName());
		}

		@Test
		void hasDisplayName() {
			assertEquals("huce qafa", subject.getDisplayName());
		}

		@Test
		void isMainBinary() {
			assertTrue(subject.isMain());
		}
	}
}
