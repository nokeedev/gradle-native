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

import com.google.common.reflect.TypeToken;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProperty;
import dev.nokee.model.internal.core.ModelPropertyTag;
import dev.nokee.model.internal.core.ModelPropertyTypeComponent;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.TypeOf;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelType.untyped;
import static dev.nokee.model.internal.type.ModelTypes.list;
import static dev.nokee.model.internal.type.ModelTypes.map;
import static dev.nokee.model.internal.type.ModelTypes.set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelElementFactoryModelPropertyTest {
	private final ModelElementFactory subject = new ModelElementFactory(objectFactory()::newInstance);
	private final ModelNode entity = createPropertyEntity();

	private static ModelNode createPropertyEntity() {
		val result = new ModelNode();
		result.addComponent(tag(ModelPropertyTag.class));
		result.addComponent(ofInstance(objectFactory().newInstance(MyOtherType.class)));
		return result;
	}

	interface ModelPropertyFactoryTester<T> {
		ModelElementFactory subject();

		@SuppressWarnings({"unchecked", "UnstableApiUsage"})
		default ModelType<T> propertyType() {
			return (ModelType<T>) of(new TypeToken<T>(getClass()) {}.getType());
		}

		ModelNode entity();

		@Test
		default void returnsModelPropertyOnCreateElement() {
			assertThat(subject().createElement(entity()), isA(ModelProperty.class));
		}

		@Test
		default void defaultsToPropertyTypeOnCreateElement() {
			assertEquals(propertyType().getConcreteType(), ((ModelProperty<?>) subject().createElement(entity())).getType());
		}

		@Test
		default void defaultsToPropertyTypeOnCreatePropertyByEntityOnly() {
			assertEquals(propertyType().getConcreteType(), subject().createProperty(entity()).getType());
		}

		@Test
		default void returnsModelPropertyOnCreateObjectOfPropertyType() {
			assertThat(subject().createObject(entity(), propertyType()), isA(ModelProperty.class));
		}

		@Test
		default void returnsModelObjectOnCreateObjectOfNonPropertyType() {
			assertThat(subject().createObject(entity(), of(MyOtherType.class)),
				allOf(isA(DomainObjectProvider.class), not(isA(ModelProperty.class))));
		}

		@Test
		default void throwsExceptionWhenCreatePropertyOfWrongType() {
			assertThrows(RuntimeException.class, () -> subject().createProperty(entity(), of(WrongType.class)));
		}

		@Test
		default void throwsExceptionWhenCreateObjectOfWrongType() {
			assertThrows(RuntimeException.class, () -> subject().createObject(entity(), of(WrongType.class)));
		}

		@Test
		default void canCreatePropertyOfObjectType() {
			assertEquals(propertyType().getConcreteType(), subject().createProperty(entity(), untyped()).getType());
		}
	}

	@Nested
	class GradleSetPropertyTest implements ModelPropertyFactoryTester<Set<MyType>> {
		@BeforeEach
		void configureEntity() {
			entity.addComponent(new GradlePropertyComponent(objectFactory().setProperty(MyType.class)));
			entity.addComponent(new ModelPropertyTypeComponent(propertyType()));
		}

		public ModelElementFactory subject() {
			return subject;
		}

		public ModelNode entity() {
			return entity;
		}

		@Test
		void returnsModelPropertyOfFullTypeOnCreateObject() {
			assertEquals(set(of(MyType.class)).getConcreteType(), subject.createObject(entity, of(new TypeOf<Set<? extends IMyType>>() {})).getType());
		}

		@Test
		void returnsModelPropertyOfFullTypeOnCreateProject() {
			assertEquals(set(of(MyType.class)).getConcreteType(), subject.createProperty(entity, of(new TypeOf<Set<? extends IMyType>>() {})).getType());
		}
	}

	@Nested
	class GradleListPropertyTest implements ModelPropertyFactoryTester<List<MyType>> {
		@BeforeEach
		void configureEntity() {
			entity.addComponent(new GradlePropertyComponent(objectFactory().listProperty(MyType.class)));
			entity.addComponent(new ModelPropertyTypeComponent(propertyType()));
		}

		public ModelElementFactory subject() {
			return subject;
		}

		public ModelNode entity() {
			return entity;
		}

		@Test
		void returnsModelPropertyOfFullTypeOnCreateObject() {
			assertEquals(list(of(MyType.class)).getConcreteType(), subject.createObject(entity, of(new TypeOf<List<? extends IMyType>>() {})).getType());
		}

		@Test
		void returnsModelPropertyOfFullTypeOnCreateProject() {
			assertEquals(list(of(MyType.class)).getConcreteType(), subject.createProperty(entity, of(new TypeOf<List<? extends IMyType>>() {})).getType());
		}
	}

	@Nested
	class GradleMapPropertyTest implements ModelPropertyFactoryTester<Map<Integer, MyType>> {
		@BeforeEach
		void configureEntity() {
			entity.addComponent(new GradlePropertyComponent(objectFactory().mapProperty(Integer.class, MyType.class)));
			entity.addComponent(new ModelPropertyTypeComponent(propertyType()));
		}

		public ModelElementFactory subject() {
			return subject;
		}

		public ModelNode entity() {
			return entity;
		}

		@Test
		void returnsModelPropertyOfFullTypeOnCreateObject() {
			assertEquals(map(of(Integer.class), of(MyType.class)).getConcreteType(), subject.createObject(entity, of(new TypeOf<Map<? extends Number, ? extends IMyType>>() {})).getType());
		}

		@Test
		void returnsModelPropertyOfFullTypeOnCreateProject() {
			assertEquals(map(of(Integer.class), of(MyType.class)).getConcreteType(), subject.createProperty(entity, of(new TypeOf<Map<? extends Number, ? extends IMyType>>() {})).getType());
		}
	}


	@Nested
	class GradlePropertyTest implements ModelPropertyFactoryTester<MyType> {
		@BeforeEach
		void configureEntity() {
			entity.addComponent(new GradlePropertyComponent(objectFactory().property(MyType.class)));
			entity.addComponent(new ModelPropertyTypeComponent(propertyType()));
		}

		public ModelElementFactory subject() {
			return subject;
		}

		public ModelNode entity() {
			return entity;
		}

		@Test
		void returnsModelPropertyOfFullTypeOnCreateObject() {
			assertEquals(of(MyType.class).getConcreteType(), subject.createObject(entity, of(IMyType.class)).getType());
		}

		@Test
		void returnsModelPropertyOfFullTypeOnCreateProject() {
			assertEquals(of(MyType.class).getConcreteType(), subject.createProperty(entity, of(IMyType.class)).getType());
		}
	}

	@Nested
	class GradleFileCollectionTest implements ModelPropertyFactoryTester<Set<File>> {
		@BeforeEach
		void configureEntity() {
			entity.addComponent(new GradlePropertyComponent(objectFactory().fileCollection()));
			entity.addComponent(new ModelPropertyTypeComponent(propertyType()));
		}

		public ModelElementFactory subject() {
			return subject;
		}

		public ModelNode entity() {
			return entity;
		}
	}

	interface IMyType {}
	interface MyType extends IMyType {}
	interface MyOtherType {}
	interface WrongType {}
}
