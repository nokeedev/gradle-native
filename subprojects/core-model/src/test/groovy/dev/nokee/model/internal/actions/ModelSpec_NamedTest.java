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

import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.actions.ModelSpec.named;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ModelSpec_NamedTest implements ModelSpecTester<NamedSpec> {
	@Override
	public NamedSpec subject() {
		return new NamedSpec("ldok");
	}

	@Override
	public DomainObjectIdentity satisfyingInput() {
		return DomainObjectIdentity.of("ldok");
	}

	@Override
	public DomainObjectIdentity notSatisfyingInput() {
		return DomainObjectIdentity.of("nope");
	}

	@Test
	void doesNotSatisfyOnEmptyIdentity() {
		assertFalse(subject().isSatisfiedBy(emptyIdentity()));
	}

	@Test
	void canCreateSpecUsingFactoryMethod() {
		assertEquals(new NamedSpec("same"), named("same"));
	}
}
