/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.model.internal.actions;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.google.common.testing.NullPointerTester;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.gradle.api.specs.Spec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.actions.ModelSpecTester.TestSpec.SATISFY_ALL;
import static dev.nokee.model.internal.actions.ModelSpecTester.TestSpec.SATISFY_NONE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

interface ModelSpecTester<T extends ModelSpec & Spec<DomainObjectIdentity>> {
	T subject();

	DomainObjectIdentity satisfyingInput();
	DomainObjectIdentity notSatisfyingInput();

	@BeforeEach
	default void verifyInvariance() {
		assertTrue(subject().isSatisfiedBy(satisfyingInput()));
		assertFalse(subject().isSatisfiedBy(notSatisfyingInput()));
	}

	@Test
	default void checkEquals() {
		EqualsVerifier.forClass(specClass()).verify();
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNullsOnAllPublicMethods() {
		new NullPointerTester().testAllPublicInstanceMethods(subject());
	}

	@Test
	@SuppressWarnings("unchecked")
	default void canCombineWithSatisfyNoneSpec() {
		assertFalse(((Spec<DomainObjectIdentity>) subject().and(SATISFY_NONE)).isSatisfiedBy(satisfyingInput()));
		assertFalse(((Spec<DomainObjectIdentity>) subject().and(SATISFY_NONE)).isSatisfiedBy(notSatisfyingInput()));
	}

	@Test
	@SuppressWarnings("unchecked")
	default void canCombineWithSatisfyAllSpec() {
		assertTrue(((Spec<DomainObjectIdentity>) subject().and(SATISFY_ALL)).isSatisfiedBy(satisfyingInput()));
		assertFalse(((Spec<DomainObjectIdentity>) subject().and(SATISFY_ALL)).isSatisfiedBy(notSatisfyingInput()));
	}

	@SuppressWarnings({"unchecked", "UnstableApiUsage"})
	default Class<T> specClass() {
		return (Class<T>) new TypeToken<T>(getClass()) {}.getRawType();
	}

	default DomainObjectIdentity emptyIdentity() {
		return DomainObjectIdentity.of(ImmutableSet.of());
	}

	enum TestSpec implements Spec<DomainObjectIdentity>, ModelSpec {
		SATISFY_NONE {
			@Override
			public boolean isSatisfiedBy(DomainObjectIdentity domainObjectIdentity) {
				return false;
			}
		},
		SATISFY_ALL {
			@Override
			public boolean isSatisfiedBy(DomainObjectIdentity domainObjectIdentity) {
				return true;
			}
		}
	}
}
