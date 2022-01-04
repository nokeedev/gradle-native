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
package dev.nokee.model.internal.type;

import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelType.typeOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelType_TypeOfTest {
	@Test
	void canCreateModelTypeOfInstance() {
		assertEquals(of(MyType.class), typeOf(new MyType()), "type of MyType instance is expected to be MyType");
	}

	@Test
	void usesUndecoratedInstanceType() {
		assertEquals(of(MyType.class), typeOf(objectFactory().newInstance(MyType.class)));
	}

	static class MyType {}
}
