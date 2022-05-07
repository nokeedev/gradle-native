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
package dev.nokee.model.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.state.ModelStates;
import lombok.val;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.Set;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.GradlePropertyTypes.setProperty;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.*;

class DefaultModelPropertyGradleSetPropertyIntegrationTest {
	private final ModelConfigurer modelConfigurer = Mockito.mock(ModelConfigurer.class);
	private final ModelNode node = newEntity(modelConfigurer);
	private final ModelElementFactory factory = new ModelElementFactory(objectFactory()::newInstance);
	private final ModelProperty<Set<MyType>> subject = factory.createProperty(node, set(of(MyType.class)));

	private static ModelNode newEntity(ModelConfigurer modelConfigurer) {
		val property = objectFactory().setProperty(MyType.class);
		val entity = node("zimu", builder -> builder.withConfigurer(modelConfigurer));
		entity.addComponent(tag(ModelPropertyTag.class));
		entity.addComponent(new GradlePropertyComponent(property));
		entity.addComponent(new ModelPropertyTypeComponent(set(of(MyType.class))));
		entity.addComponent(new IdentifierComponent(ModelIdentifier.of("zimu", Object.class)));
		return entity;
	}

	@Test
	void canConvertToSetPropertyUsingRawSetPropertyType() {
		assertThat(assertDoesNotThrow(() -> subject.asProperty(of(SetProperty.class))), isA(SetProperty.class));
	}

	@Test
	void canConvertToSetPropertyUsingModelSetPropertyType() {
		assertThat(assertDoesNotThrow(() -> subject.asProperty(setProperty(of(MyType.class)))), isA(SetProperty.class));
	}

	@Test
	void throwsExceptionSetPropertyTypeOfObjectConversion() {
		assertThrows(RuntimeException.class, () -> subject.asProperty(setProperty(of(Object.class))));
	}

	@ParameterizedTest(name = "throwsExceptionOnInvalidSetPropertyConversion [{argumentsWithNames}]")
	@ValueSource(classes = {Property.class, ListProperty.class, MapProperty.class, ConfigurableFileCollection.class, RegularFileProperty.class, DirectoryProperty.class})
	void throwsExceptionOnInvalidSetPropertyConversion(Class<? extends HasConfigurableValue> invalidPropertyType) {
		assertThrows(RuntimeException.class, () -> subject.asProperty(of(invalidPropertyType)));
	}

	@ParameterizedTest(name = "doesNotRealizeModelPropertyOnGradleSetPropertyConversion [{argumentsWithNames}]")
	@ValueSource(classes = {SetProperty.class, ListProperty.class, MapProperty.class, ConfigurableFileCollection.class, RegularFileProperty.class, DirectoryProperty.class, Property.class})
	void doesNotRealizeModelPropertyOnGradleSetPropertyConversion(Class<? extends HasConfigurableValue> propertyType) {
		assertDoesNotChangeEntityState(node, () -> subject.asProperty(of(propertyType)));
	}

	@Test
	void canSetValueViaGradleSetProperty() {
		val expectedValue = ImmutableSet.of(Mockito.mock(MyType.class));
		subject.asProperty(setProperty(of(MyType.class))).set(expectedValue);
		assertEquals(expectedValue, subject.get());
	}

	private static void assertDoesNotChangeEntityState(ModelNode entity, Executable executable) {
		val expectedState = ModelStates.getState(entity);
		try {
			executable.execute();
		} catch (Throwable ex) {
			// ignore exception
		}
		assertEquals(expectedState, ModelStates.getState(entity));
	}

	@Test
	void hasModelPropertyTypeAsUntypedModelProperty() {
		assertEquals(set(of(MyType.class)).getConcreteType(), factory.createProperty(node).getType());
	}

	@Test
	void hasModelPropertyTypeAsModelElement() {
		assertTrue(factory.createElement(node) instanceof ModelProperty);
		assertEquals(set(of(MyType.class)).getConcreteType(), ((ModelProperty<?>) factory.createElement(node)).getType());
	}


	interface MyType {}
}
