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
package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.ModelNode;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelElementFactoryModelObjectTest {
	private final ModelElementFactory subject = new ModelElementFactory(objectFactory()::newInstance);
	private final ModelNode entity = createObjectEntity();

	private static ModelNode createObjectEntity() {
		val result = new ModelNode();
		result.addComponent(ofInstance(objectFactory().newInstance(MyType.class)));
		result.addComponent(ofInstance(objectFactory().newInstance(MyOtherType.class)));
		return result;
	}

	@Test
	void canCreateObjectOfValidType() {
		assertThat(subject.createObject(entity, of(MyType.class)), isA(DomainObjectProvider.class));
		assertThat(subject.createObject(entity, of(MyOtherType.class)), isA(DomainObjectProvider.class));
	}

	@Test
	void returnsModelObjectOfFullType() {
		assertEquals(of(MyType.class).getConcreteType(), subject.createObject(entity, of(IMyType.class)).getType());
	}

	@Test
	void throwsExceptionWhenCreateProperty() {
		assertThrows(RuntimeException.class, () -> subject.createProperty(entity, of(MyType.class)));
		assertThrows(RuntimeException.class, () -> subject.createProperty(entity, of(MyOtherType.class)));
		assertThrows(RuntimeException.class, () -> subject.createProperty(entity, of(WrongType.class)));
	}

	@Test
	void throwsExceptionWhenCreateObjectOfWrongType() {
		assertThrows(RuntimeException.class, () -> subject.createObject(entity, of(WrongType.class)));
	}

	interface IMyType {}
	interface MyType extends IMyType {}
	interface MyOtherType {}
	interface WrongType {}
}
