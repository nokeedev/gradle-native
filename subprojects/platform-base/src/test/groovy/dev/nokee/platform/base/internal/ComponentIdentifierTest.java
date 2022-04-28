/*
 * Copyright 2020 the original author or authors.
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
import dev.nokee.model.internal.ProjectIdentifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.platform.base.internal.ComponentIdentifier.builder;
import static dev.nokee.platform.base.internal.ComponentIdentifier.of;
import static dev.nokee.platform.base.internal.ComponentIdentifier.ofMain;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentIdentifierTest {
	private final ProjectIdentifier ownerIdentifier = ProjectIdentifier.of("jege");

	@Nested
	class OfMainIdentifierTest {
		private final ComponentIdentifier subject = ofMain(ownerIdentifier);

		@Test
		void isMainComponent() {
			assertTrue(subject.isMainComponent());
		}

		@Test
		void hasMainName() {
			assertEquals(ComponentName.ofMain(), subject.getName());
		}

		@Test
		void useDefaultDisplayName() {
			assertThat(subject, hasToString("component ':main'"));
		}
	}

	@Nested
	class OfNameIdentifierTest {
		private final ComponentIdentifier subject = of("roba", ownerIdentifier);

		@Test
		void isNotMainComponent() {
			assertFalse(subject.isMainComponent());
		}

		@Test
		void hasName() {
			assertEquals(ComponentName.of("roba"), subject.getName());
		}

		@Test
		void useDefaultDisplayName() {
			assertThat(subject, hasToString("component ':roba'"));
		}
	}

	@Nested
	class OfComponentNameIdentifierTest {
		private final ComponentIdentifier subject = of(ComponentName.of("nuwe"), ownerIdentifier);

		@Test
		void isNotMainComponent() {
			assertFalse(subject.isMainComponent());
		}

		@Test
		void hasName() {
			assertEquals(ComponentName.of("nuwe"), subject.getName());
		}

		@Test
		void useDefaultDisplayName() {
			assertThat(subject, hasToString("component ':nuwe'"));
		}
	}

	@Nested
	class BuilderIdentifierTest {
		private final ComponentIdentifier subject = builder().name(ComponentName.of("nafa")).displayName("FOO application").withProjectIdentifier(ownerIdentifier).build();

		@Test
		void isNotMainComponent() {
			assertFalse(subject.isMainComponent());
		}

		@Test
		void hasName() {
			assertEquals(ComponentName.of("nafa"), subject.getName());
		}

		@Test
		void useDisplayName() {
			assertThat(subject, hasToString("FOO application ':nafa'"));
		}
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(ofMain(ownerIdentifier), of("main", ownerIdentifier), of(ComponentName.ofMain(), ownerIdentifier),
				builder().name(ComponentName.ofMain()).withProjectIdentifier(ownerIdentifier).build())
			.addEqualityGroup(of("bino", ownerIdentifier), of(ComponentName.of("bino"), ownerIdentifier),
				builder().name(ComponentName.of("bino")).withProjectIdentifier(ownerIdentifier).build())
			.addEqualityGroup(of("bino", ProjectIdentifier.of("webo")))
			.addEqualityGroup(builder().name(ComponentName.of("bino")).displayName("BAR library").withProjectIdentifier(ownerIdentifier).build())
			.testEquals();
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester()
			.setDefault(ProjectIdentifier.class, ProjectIdentifier.ofRootProject())
			.setDefault(ComponentName.class, ComponentName.ofMain())
			.setDefault(String.class, "sled")
			.testAllPublicStaticMethods(ComponentIdentifier.class);
		new NullPointerTester().testAllPublicInstanceMethods(builder());
	}
}
