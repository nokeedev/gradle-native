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

import dev.nokee.platform.base.testers.HasNameTester;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public interface DependencyBucketIdentityTester extends HasNameTester {
	DependencyBucketIdentity subject();

	@Test
	default void hasDisplayName() {
		assertNotNull(subject().getDisplayName());
	}

	@Test
	default void hasType() {
		assertNotNull(subject().getType());
	}

	@Test
	default void useDisplayNameForToString() {
		assertThat(subject(), hasToString(subject().getDisplayName()));
	}
}
