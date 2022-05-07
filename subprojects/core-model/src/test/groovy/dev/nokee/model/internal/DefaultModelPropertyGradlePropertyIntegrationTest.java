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

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.GradlePropertyTypes.property;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.*;

class DefaultModelPropertyGradlePropertyIntegrationTest {
	private final ModelConfigurer modelConfigurer = Mockito.mock(ModelConfigurer.class);
	private final ModelNode node = newEntity(modelConfigurer);
	private final ModelElementFactory factory = new ModelElementFactory(objectFactory()::newInstance);
	private final ModelProperty<MyType> subject = factory.createProperty(node, of(MyType.class));

	private static ModelNode newEntity(ModelConfigurer modelConfigurer) {
		val property = objectFactory().property(MyType.class);
		val entity = node("beho", builder -> builder.withConfigurer(modelConfigurer));
		entity.addComponent(tag(ModelPropertyTag.class));
		entity.addComponent(new GradlePropertyComponent(property));
		entity.addComponent(new ModelPropertyTypeComponent(of(MyType.class)));
		entity.addComponent(new IdentifierComponent(ModelIdentifier.of("beho", Object.class)));
		return entity;
	}

	@Test
	void canConvertToPropertyUsingRawPropertyType() {
		assertThat(assertDoesNotThrow(() -> subject.asProperty(of(Property.class))), isA(Property.class));
	}

	@Test
	void canConvertToPropertyUsingModelPropertyType() {
		assertThat(assertDoesNotThrow(() -> subject.asProperty(property(of(MyType.class)))), isA(Property.class));
	}

	@Test
	void throwsExceptionPropertyTypeOfObjectConversion() {
		assertThrows(RuntimeException.class, () -> subject.asProperty(property(of(Object.class))));
	}

	@ParameterizedTest(name = "throwsExceptionOnInvalidPropertyConversion [{argumentsWithNames}]")
	@ValueSource(classes = {SetProperty.class, ListProperty.class, MapProperty.class, ConfigurableFileCollection.class, RegularFileProperty.class, DirectoryProperty.class})
	void throwsExceptionOnInvalidPropertyConversion(Class<? extends HasConfigurableValue> invalidPropertyType) {
		assertThrows(RuntimeException.class, () -> subject.asProperty(of(invalidPropertyType)));
	}

	@ParameterizedTest(name = "doesNotRealizeModelPropertyOnGradlePropertyConversion [{argumentsWithNames}]")
	@ValueSource(classes = {SetProperty.class, ListProperty.class, MapProperty.class, ConfigurableFileCollection.class, RegularFileProperty.class, DirectoryProperty.class, Property.class})
	void doesNotRealizeModelPropertyOnGradlePropertyConversion(Class<? extends HasConfigurableValue> propertyType) {
		assertDoesNotChangeEntityState(node, () -> subject.asProperty(of(propertyType)));
	}

	@Test
	void canSetValueViaGradleProperty() {
		val expectedValue = Mockito.mock(MyType.class);
		subject.asProperty(property(of(MyType.class))).set(expectedValue);
		assertSame(expectedValue, subject.get());
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
		assertEquals(of(MyType.class).getConcreteType(), factory.createProperty(node).getType());
	}

	@Test
	void hasModelPropertyTypeAsModelElement() {
		assertTrue(factory.createElement(node) instanceof ModelProperty);
		assertEquals(of(MyType.class).getConcreteType(), ((ModelProperty<?>) factory.createElement(node)).getType());
	}


	interface MyType {}
}
