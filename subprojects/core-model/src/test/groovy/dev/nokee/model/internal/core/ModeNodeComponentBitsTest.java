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
package dev.nokee.model.internal.core;

import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ModeNodeComponentBitsTest {
	private final ModelNode subject = new ModelNode();

	@Test
	void hasNoComponentBitsOnNewEntity() {
		assertEquals(Bits.empty(), subject.getComponentBits());
	}

	@Test
	void hasBitsOfAddedComponent() {
		subject.addComponent(objectFactory().newInstance(MyBaseComponent.class));
		assertEquals(ModelComponentType.assignedComponentTypes.getUnchecked(MyBaseComponent.class),
			subject.getComponentBits());
	}

	@Test
	void hasBitsOfAddedComponentFamily() {
		subject.addComponent(objectFactory().newInstance(MyComponent.class));
		assertEquals(ModelComponentType.assignedComponentTypeFamilies.getUnchecked(MyComponent.class),
			subject.getComponentBits());
	}

	@Test
	void hasBitsOfAllComponentFamilies() {
		subject.addComponent(objectFactory().newInstance(MyComponent.class));
		subject.addComponent(objectFactory().newInstance(MyOtherComponent.class));
		assertEquals(ModelComponentType.assignedComponentTypeFamilies.getUnchecked(MyComponent.class)
				.or(ModelComponentType.assignedComponentTypeFamilies.getUnchecked(MyOtherComponent.class)),
			subject.getComponentBits());
	}

	interface MyBaseComponent {}
	interface MyComponent extends MyBaseComponent {}
	interface MyOtherComponent {}
}
