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
package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelPropertyTag;
import dev.nokee.model.internal.core.ModelPropertyTypeComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import lombok.val;
import org.gradle.api.provider.SetProperty;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class VariantDimensionPropertyFactoryTest {
	private final DimensionPropertyRegistrationFactory factory = new DimensionPropertyRegistrationFactory(objectFactory());
	private final CoordinateAxis<MyAxis> testAxis = CoordinateAxis.of(MyAxis.class);

	@Nested
	class NewAxisPropertyTest {
		private final ModelRegistration subject = factory.newAxisProperty(testAxis);

		@Test
		void hasAxisComponent() {
			val component = subject.getComponents().stream()
				.filter(VariantDimensionAxisComponent.class::isInstance).map(VariantDimensionAxisComponent.class::cast).findFirst()
				.map(VariantDimensionAxisComponent::get);
			assertThat(component, optionalWithValue(equalTo(testAxis)));
		}

		@Test
		void hasVariantDimensionTag() {
			assertThat(subject.getComponents(), hasItem(is(tag(VariantDimensionTag.class))));
		}

		@Test
		void hasModelPropertyTag() {
			assertThat(subject.getComponents(), hasItem(is(tag(ModelPropertyTag.class))));
		}

		@Test
		void hasModelPropertyTypeComponent() {
			val component = subject.getComponents().stream()
				.filter(ModelPropertyTypeComponent.class::isInstance).map(ModelPropertyTypeComponent.class::cast).findFirst()
				.map(ModelPropertyTypeComponent::get);
			assertThat(component, optionalWithValue(equalTo(set(of(MyAxis.class)))));
		}

		@Test
		void hasGradlePropertyComponent() {
			val component = subject.getComponents().stream()
				.filter(GradlePropertyComponent.class::isInstance).map(GradlePropertyComponent.class::cast).findFirst()
				.map(GradlePropertyComponent::get);
			assertThat(component, optionalWithValue(isA(SetProperty.class)));
		}

		@Test
		void hasNoAxisFilterComponent() {
			assertThat(subject.getComponents(), not(hasItem(isA(VariantDimensionAxisFilterComponent.class))));
		}

		@Test
		void hasNoAxisValidatorComponent() {
			assertThat(subject.getComponents(), not(hasItem(isA(VariantDimensionAxisValidatorComponent.class))));
		}

		@Test
		void hasNoAxisOptionalTag() {
			assertThat(subject.getComponents(), not(hasItem(is(tag(VariantDimensionAxisOptionalTag.class)))));
		}
	}

	@Test
	void throwsExceptionWhenAxisIsMissing() {
		assertThrows(IllegalStateException.class, () -> factory.newAxisProperty().build());
	}

	@Test
	void canValidateAxisUsingConsumer() {
		@SuppressWarnings("unchecked")
		val validator = (Consumer<Iterable<Coordinate<MyAxis>>>) mock(Consumer.class);
		val subject = factory.newAxisProperty().axis(testAxis).validateUsing(validator).build();
		assertThat(subject.getComponents(), hasItem(isA(VariantDimensionAxisValidatorComponent.class)));
	}

	@Test
	void canValidateAxisByValues() {
		val subject = factory.newAxisProperty().axis(testAxis).validValues(mock(MyAxis.class), mock(MyAxis.class)).build();
		assertThat(subject.getComponents(), hasItem(isA(VariantDimensionAxisValidatorComponent.class)));
	}

	@Test
	void canRegisterFilter() {
		@SuppressWarnings("unchecked")
		val validator = (Predicate<BuildVariantInternal>) mock(Predicate.class);
		val subject = factory.newAxisProperty().axis(testAxis).filterVariant(validator).build();
		assertThat(subject.getComponents(), hasItem(isA(VariantDimensionAxisFilterComponent.class)));
	}

	@Test
	void canMarkAxisAsOptional() {
		val subject = factory.newAxisProperty().axis(testAxis).includeEmptyCoordinate().build();
		assertThat(subject.getComponents(), hasItem(is(tag(VariantDimensionAxisOptionalTag.class))));
	}

	@Test
	void canUseDifferentElementTypeThanAxisType() {
		val subject = factory.newAxisProperty().elementType(MyElementType.class).axis(testAxis).build();
		val component = subject.getComponents().stream()
			.filter(ModelPropertyTypeComponent.class::isInstance).map(ModelPropertyTypeComponent.class::cast).findFirst()
			.map(ModelPropertyTypeComponent::get);
		assertThat(component, optionalWithValue(equalTo(set(of(MyElementType.class)))));
	}

	private interface MyAxis {}
	private interface MyElementType {}
}
