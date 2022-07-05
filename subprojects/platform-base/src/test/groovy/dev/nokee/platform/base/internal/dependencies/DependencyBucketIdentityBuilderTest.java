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
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.names.ElementName.of;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.builder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DependencyBucketIdentityBuilderTest {
	@Test
	void hasNameFromBuilder() {
		assertEquals(of("seza"), builder().name(of("seza")).build().getName());
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
