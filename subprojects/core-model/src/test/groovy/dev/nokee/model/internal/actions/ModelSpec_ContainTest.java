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
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.actions.ModelSpec.has;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ModelSpec_ContainTest implements ModelSpecTester<ContainSpec> {
	private static final Object MATCHING_OBJECT = new Object();
	private static final Object NOT_MATCHING_OBJECT = new Object();

	@Override
	public ContainSpec subject() {
		return new ContainSpec(MATCHING_OBJECT);
	}

	@Override
	public DomainObjectIdentity satisfyingInput() {
		return DomainObjectIdentity.of(ImmutableSet.of(MATCHING_OBJECT));
	}

	@Override
	public DomainObjectIdentity notSatisfyingInput() {
		return DomainObjectIdentity.of(ImmutableSet.of(NOT_MATCHING_OBJECT));
	}

	@Test
	void doesNotSatisfyOnEmptyIdentity() {
		assertFalse(subject().isSatisfiedBy(emptyIdentity()));
	}

	@Test
	void canCreateSpecUsingFactoryMethod() {
		val obj = new Object();
		assertEquals(new ContainSpec(obj), has(obj));
	}
}
