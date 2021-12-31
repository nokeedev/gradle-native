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
package dev.nokee.model.internal.type;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.type.GradlePropertyTypes.setProperty;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelType.untyped;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GradlePropertyTypes_SetPropertyTest {
	@Test
	void returnsSetPropertyOfMyTypeModelType() {
		assertEquals(of(new TypeOf<SetProperty<MyType>>() {}), setProperty(of(MyType.class)));
	}

	@Test
	void returnsSetPropertyOfObjectModelType() {
		assertEquals(of(new TypeOf<SetProperty<Object>>() {}), setProperty(untyped()));
	}

	@Test
	void returnsModelTypeForSetPropertyOfMyTypeInstance() {
		assertEquals(of(new TypeOf<SetProperty<MyType>>() {}), GradlePropertyTypes.of(objectFactory().setProperty(MyType.class)));
	}

	private interface MyType {}
}
