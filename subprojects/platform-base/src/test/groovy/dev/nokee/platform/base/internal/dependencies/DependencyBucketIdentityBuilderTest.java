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
package dev.nokee.platform.base.internal.dependencies;

import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.names.ElementName.of;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.builder;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketType.Consumable;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketType.Declarable;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketType.Resolvable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DependencyBucketIdentityBuilderTest {
	@Test
	void defaultsTypeToDeclarable() {
		assertEquals(Declarable, builder().name(of("juno")).build().getType());
	}

	@Test
	void defaultsDisplayNameToBucketNameFollowedByDependenciesStringByDefault() {
		assertEquals("ronu dependencies", builder().name(of("ronu")).build().getDisplayName());
	}

	@Test
	void defaultsDisplayNameToBucketNameFollowedByDependenciesStringForDeclarableBucket() {
		assertEquals("pama dependencies", builder().name(of("pama")).type(Declarable).build().getDisplayName());
	}

	@Test
	void defaultsDisplayNameToBucketNameForConsumableBucket() {
		assertEquals("qipa", builder().name(of("qipa")).type(Consumable).build().getDisplayName());
	}

	@Test
	void defaultsDisplayNameToBucketNameForResolvableBucket() {
		assertEquals("qeba", builder().name(of("qeba")).type(Resolvable).build().getDisplayName());
	}

	@Test
	void canOverrideDisplayName() {
		val subject =  builder().name(of("mulu"));
		assertEquals("libever", subject.displayName("libever").build().getDisplayName());
		assertEquals("mevulum", subject.type(Declarable).displayName("mevulum").build().getDisplayName());
		assertEquals("wanoqaf", subject.type(Consumable).displayName("wanoqaf").build().getDisplayName());
		assertEquals("xosemut", subject.type(Resolvable).displayName("xosemut").build().getDisplayName());
	}

	@Test
	void useDisplayNameForToString() {
		assertEquals("leqijug", builder().name(of("nora")).displayName("leqijug").build().toString());
	}

	@Test
	void hasNameFromBuilder() {
		assertEquals(of("seza"), builder().name(of("seza")).build().getName());
	}

	@Test
	void hasTypeFromBuilder() {
		assertEquals(Consumable, builder().name(of("haka")).type(Consumable).build().getType());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicInstanceMethods(builder());
	}

	@Test
	void throwsExceptionWhenNoName() {
		assertThrows(IllegalStateException.class, () -> builder().build());
	}
}
