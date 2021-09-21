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
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

public interface TargetLinkageFactoryTester {
	TargetLinkageFactory subject();

	@Test
	default void canCreateSharedLinkage() {
		assertThat(subject().getShared(), isA(TargetLinkage.class));
		assertThat(subject().getShared(), is(TargetLinkages.SHARED));
	}

	@Test
	default void canCreateStaticLinkage() {
		assertThat(subject().getStatic(), isA(TargetLinkage.class));
		assertThat(subject().getStatic(), is(TargetLinkages.STATIC));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(subject().getShared(), subject().getShared())
			.addEqualityGroup(subject().getStatic(), subject().getStatic())
			.testEquals();
	}
}
