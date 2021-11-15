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
package dev.nokee.platform.base.testers;

import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.VariantAwareComponent;
import lombok.Value;
import lombok.val;
import org.gradle.api.Named;
import org.gradle.api.provider.SetProperty;
import org.hamcrest.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class VariantDimensionsIntegrationTester {
	public abstract VariantAwareComponent<?> subject();

	@Test
	void throwsExceptionWhenCreatingNewAxisWithNullType() {
		assertThrows(NullPointerException.class, () -> subject().getDimensions().newAxis(null));
	}

	@Nested
	class NewAxisByTypeOnlyTest {
		private SetProperty<MyAxis> subject;

		@BeforeEach
		void createAxis() {
			subject = subject().getDimensions().newAxis(MyAxis.class);
		}

		@Test
		void returnsSetPropertyOfAxisType() {
			assertThat(subject, Matchers.notNullValue(SetProperty.class));
		}

		@Test
		void hasEmptyValueSetByDefault() {
			assertThat(subject, providerOf(emptyIterable()));
		}

		@Test
		void canMutateAxisProperty() {
			assertDoesNotThrow(() -> subject.add(new MyAxis("cip")));
			assertDoesNotThrow(() -> subject.convention(ImmutableSet.of(new MyAxis("xoz"))));
			assertDoesNotThrow(() -> subject.set(ImmutableSet.of(new MyAxis("hap"))));
		}

		@Test
		void finalizeAxisValuesWhenVariantComputed() {
			subject.set(ImmutableSet.of(new MyAxis("lid")));
			subject().getVariants().get(); // realize variants
			val ex = assertThrows(RuntimeException.class, () -> subject.set(ImmutableSet.of(new MyAxis("fic"))));
			assertThat(ex.getMessage(), is("The value for this property is final and cannot be changed any further."));
		}

		@Test
		void participatesToVariantCalculation() {
			val v0 = new MyAxis("par");
			val v1 = new MyAxis("buk");
			subject.set(ImmutableSet.of(v0, v1));
			assertThat(subject().getBuildVariants(), providerOf(allOf(hasItem(hasAxisOf(v0)), hasItem(hasAxisOf(v1)))));
		}

		@Test
		void throwsExceptionIfAxisPropertyHasNoValues() {
			subject.value((Iterable<? extends MyAxis>) null);
			subject.convention((Iterable<? extends MyAxis>) null);
			val ex = assertThrows(RuntimeException.class, () -> subject().getVariants().get()); // realize variants
			assertThat(ex.getMessage(), is("Cannot query the value of this provider because it has no value available."));
		}

		@Test
		void throwsExceptionIfAxisPropertyHasEmptyValueSet() {
			subject.value(ImmutableSet.of());
			subject.convention(ImmutableSet.of());
			val ex = assertThrows(RuntimeException.class, () -> subject().getVariants().get()); // realize variants
			assertThat(ex.getMessage(), startsWith("A my axis needs to be specified for"));
		}
	}

	@Value
	private static class MyAxis implements Named {
		String name;
	}

	private static Matcher<BuildVariant> hasAxisOf(Object value) {
		return new TypeSafeMatcher<BuildVariant>() {
			@Override
			public void describeTo(Description description) {

			}

			@Override
			protected boolean matchesSafely(BuildVariant item) {
				return item.hasAxisOf(value);
			}
		};
	}
}
