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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ModeNodeComponentBitsTest {
	private final ModelNode subject = new ModelNode();

	@Test
	void hasNoComponentBitsOnNewEntity() {
		assertEquals(Bits.empty(), subject.getComponentBits());
	}

	@Test
	void hasBitsOfAddedComponent() {
		subject.addComponent(new MyComponent());
		assertEquals(ModelComponentType.componentBits(MyComponent.class),
			subject.getComponentBits());
		assertNotEquals(ModelComponentType.componentBits(MyOtherComponent.class), subject.getComponentBits());
	}

	static final class MyComponent implements ModelComponent {}
	static final class MyOtherComponent implements ModelComponent {}
}
