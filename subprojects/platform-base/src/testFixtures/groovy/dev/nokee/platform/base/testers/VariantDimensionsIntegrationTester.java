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
import com.google.common.reflect.TypeToken;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.VariantDimensionBuilder;
import dev.nokee.utils.ActionTestUtils;
import dev.nokee.utils.ClosureTestUtils;
import groovy.lang.Closure;
import lombok.Value;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.provider.SetProperty;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.utils.FunctionalInterfaceMatchers.calledOnceWith;
import static dev.nokee.utils.FunctionalInterfaceMatchers.delegateFirstStrategy;
import static dev.nokee.utils.FunctionalInterfaceMatchers.delegateOf;
import static dev.nokee.utils.FunctionalInterfaceMatchers.singleArgumentOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class VariantDimensionsIntegrationTester {
	public abstract VariantAwareComponent<?> subject();

	private VariantAwareComponent<?> component() {
		return subject();
	}

	@Test
	void throwsExceptionWhenCreatingNewAxisWithNullType() {
		assertThrows(NullPointerException.class, () -> subject().getDimensions().newAxis(null));
		assertThrows(NullPointerException.class, () -> subject().getDimensions().newAxis(null, ActionTestUtils.doSomething()));
	}

	@Test
	void throwsExceptionWhenCreatingNewAxisWithNullAction() {
		assertThrows(NullPointerException.class, () -> subject().getDimensions().newAxis(MyAxis.class, (Action<VariantDimensionBuilder>) null));
	}

	@Test
	@SuppressWarnings("rawtypes")
	void throwsExceptionWhenCreatingNewAxisWithNullClosure() {
		assertThrows(NullPointerException.class, () -> subject().getDimensions().newAxis(MyAxis.class, (Closure)  null));
	}

	@Test
	void callsActionWithBuilder() {
		val action = ActionTestUtils.mockAction(VariantDimensionBuilder.class);
		subject().getDimensions().newAxis(MyAxis.class, action);
		assertThat(action, calledOnceWith(singleArgumentOf(isA(VariantDimensionBuilder.class))));
	}

	@Test
	void callsClosureWithBuilder() {
		val closure = ClosureTestUtils.mockClosure(VariantDimensionBuilder.class);
		subject().getDimensions().newAxis(MyAxis.class, closure);
		assertThat(closure, calledOnceWith(singleArgumentOf(isA(VariantDimensionBuilder.class))));
		assertThat(closure, calledOnceWith(allOf(delegateFirstStrategy(), delegateOf(isA(VariantDimensionBuilder.class)))));
	}

	abstract class NewAxisTester<T extends Named> {
		public abstract SetProperty<T> subject();

		private T newAxisValue(String name) {
			return assertDoesNotThrow(() -> (T) new TypeToken<T>(getClass()) {}.getRawType().getConstructor(String.class).newInstance(name));
		}

		@Test
		void returnsSetPropertyOfAxisType() {
			assertThat(subject(), Matchers.notNullValue(SetProperty.class));
		}

		@Test
		void hasEmptyValueSetByDefault() {
			assertThat(subject(), providerOf(emptyIterable()));
		}

		@Test
		void canMutateAxisProperty() {
			assertDoesNotThrow(() -> subject().add(newAxisValue("cip")));
			assertDoesNotThrow(() -> subject().convention(ImmutableSet.of(newAxisValue("xoz"))));
			assertDoesNotThrow(() -> subject().set(ImmutableSet.of(newAxisValue("hap"))));
		}

		@Test
		void finalizeAxisValuesWhenVariantComputed() {
			subject().set(ImmutableSet.of(newAxisValue("lid")));
			component().getVariants().get(); // realize variants
			val ex = assertThrows(RuntimeException.class, () -> subject().set(ImmutableSet.of(newAxisValue("fic"))));
			assertThat(ex.getMessage(), matchesRegex("The value for .+ is final and cannot be changed any further."));
		}

		@Test
		void participatesToVariantCalculation() {
			val v0 = newAxisValue("par");
			val v1 = newAxisValue("buk");
			subject().set(ImmutableSet.of(v0, v1));
			assertThat(component().getBuildVariants(), providerOf(allOf(hasItem(hasAxisOf(v0)), hasItem(hasAxisOf(v1)))));
		}

		@Test
		void throwsExceptionIfAxisPropertyHasNoValues() {
			subject().value((Iterable<? extends T>) null);
			subject().convention((Iterable<? extends T>) null);
			val ex = assertThrows(RuntimeException.class, () -> component().getVariants().get()); // realize variants
			assertThat(ex.getMessage(), matchesRegex(Pattern.compile("^Cannot query the value of .+ because it has no value available\\..+", Pattern.DOTALL)));
		}

		@Test
		void throwsExceptionIfAxisPropertyHasEmptyValueSet() {
			subject().value(ImmutableSet.of());
			subject().convention(ImmutableSet.of());
			val ex = assertThrows(RuntimeException.class, () -> component().getVariants().get()); // realize variants
			assertThat(ex.getMessage(), startsWith("A my axis needs to be specified for"));
		}
	}

	@Nested
	class NewAxisByTypeOnlyTest extends NewAxisTester<MyAxis> {
		private SetProperty<MyAxis> subject;

		@BeforeEach
		void createAxis() {
			subject = component().getDimensions().newAxis(MyAxis.class);
		}

		@Override
		public SetProperty<MyAxis> subject() {
			return subject;
		}
	}

	@Nested
	class NewAxisComplementingAnotherAxisTest extends NewAxisTester<MyAxis> {
		private MyOtherAxisA a0 = new MyOtherAxisA("a0");
		private MyOtherAxisA a1 = new MyOtherAxisA("a1");
		private MyOtherAxisB b0 = new MyOtherAxisB("b0");
		private SetProperty<MyAxis> subject;

		@BeforeEach
		void createAxis() {
			component().getDimensions().newAxis(MyOtherAxisA.class).value(ImmutableSet.of(a0, a1));
			component().getDimensions().newAxis(MyOtherAxisB.class).value(ImmutableSet.of(b0));
			subject = component().getDimensions().newAxis(MyAxis.class, builder -> builder.onlyOn(a0));
		}

		@Override
		public SetProperty<MyAxis> subject() {
			return subject;
		}

		@Test
		void filtersOutAllBuildVariantsForCurrentAxisNotComplementingSpecifiedAxisValue() {
			val v0 = new MyAxis("drf");
			subject.set(ImmutableSet.of(v0));
			assertThat(component().getBuildVariants(), providerOf(not(hasItem(allOf(hasAxisOf(v0), hasAxisOf(a1))))));
			assertThat(component().getBuildVariants(), providerOf(hasItem(allOf(hasAxisOf(v0), hasAxisOf(a0)))));
			assertThat(component().getBuildVariants(), providerOf(everyItem(hasAxisOf(b0))));
		}
	}

	@Nested
	class NewAxisExcludeAnotherAxisTest extends NewAxisTester<MyAxis> {
		private MyOtherAxisA a0 = new MyOtherAxisA("a0");
		private MyOtherAxisA a1 = new MyOtherAxisA("a1");
		private MyOtherAxisB b0 = new MyOtherAxisB("b0");
		private SetProperty<MyAxis> subject;

		@BeforeEach
		void createAxis() {
			component().getDimensions().newAxis(MyOtherAxisA.class).value(ImmutableSet.of(a0, a1));
			component().getDimensions().newAxis(MyOtherAxisB.class).value(ImmutableSet.of(b0));
			subject = component().getDimensions().newAxis(MyAxis.class, builder -> builder.exceptOn(a0));
		}

		@Override
		public SetProperty<MyAxis> subject() {
			return subject;
		}

		@Test
		void filtersOutAllBuildVariantsWhereCurrentAxisAndSpecifiedAxisValue() {
			val v0 = new MyAxis("fig");
			subject.set(ImmutableSet.of(v0));
			assertThat(component().getBuildVariants(), providerOf(not(hasItem(allOf(hasAxisOf(v0), hasAxisOf(a0))))));
			assertThat(component().getBuildVariants(), providerOf(hasItem(allOf(hasAxisOf(v0), hasAxisOf(a1)))));
			assertThat(component().getBuildVariants(), providerOf(everyItem(hasAxisOf(b0))));
		}
	}

	@Value
	private static class MyAxis implements Named {
		String name;
	}

	@Value
	private static class MyOtherAxisA implements Named {
		String name;
	}

	@Value
	private static class MyOtherAxisB implements Named {
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
