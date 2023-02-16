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

import com.google.common.reflect.TypeToken;
import dev.nokee.internal.testing.testdoubles.TestClosure;
import dev.nokee.internal.testing.testdoubles.TestDouble;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.VariantDimensionBuilder;
import dev.nokee.utils.ActionTestUtils;
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

import static com.google.common.collect.ImmutableSet.of;
import static dev.nokee.internal.testing.GradleProviderMatchers.finalizedValue;
import static dev.nokee.internal.testing.GradleProviderMatchers.hasNoValue;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnce;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.withClosureArguments;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.withDelegateFirstStrategy;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.withDelegateOf;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newSpy;
import static dev.nokee.internal.testing.testdoubles.TestDoubleTypes.ofAction;
import static dev.nokee.internal.testing.testdoubles.TestDoubleTypes.ofClosure;
import static dev.nokee.internal.testing.testdoubles.TestDoubleTypes.ofType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
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
		assertThrows(NullPointerException.class, () -> subject().getDimensions().newAxis(MyAxis.class, (Action<VariantDimensionBuilder<MyAxis>>) null));
	}

	@Test
	@SuppressWarnings("rawtypes")
	void throwsExceptionWhenCreatingNewAxisWithNullClosure() {
		assertThrows(NullPointerException.class, () -> subject().getDimensions().newAxis(MyAxis.class, (Closure)  null));
	}

	@Test
	void callsActionWithBuilder() {
		TestDouble<Action<VariantDimensionBuilder<MyAxis>>> action = newMock(ofAction(ofType(VariantDimensionBuilder.class, MyAxis.class)));
		subject().getDimensions().newAxis(MyAxis.class, action.instance());
		assertThat(action.to(method(Action<VariantDimensionBuilder<MyAxis>>::execute)), calledOnceWith(isA(VariantDimensionBuilder.class)));
	}

	@Test
	@SuppressWarnings("rawtypes")
	void callsClosureWithBuilder() {
		val closure = newSpy(ofClosure(VariantDimensionBuilder.class));
		subject().getDimensions().newAxis(MyAxis.class, closure.instance());
		assertThat(closure.to(method(TestClosure<Object, VariantDimensionBuilder>::execute)), calledOnce(withClosureArguments(isA(VariantDimensionBuilder.class))));
		assertThat(closure.to(method(TestClosure<Object, VariantDimensionBuilder>::execute)), calledOnce(allOf(withDelegateFirstStrategy(), withDelegateOf(isA(VariantDimensionBuilder.class)))));
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
			assertDoesNotThrow(() -> subject().convention(of(newAxisValue("xoz"))));
			assertDoesNotThrow(() -> subject().set(of(newAxisValue("hap"))));
		}

		@Test
		void finalizeAxisValuesWhenVariantComputed() {
			subject().set(of(newAxisValue("lid")));
			component().getVariants().get(); // realize variants
			assertThat(subject(), finalizedValue());
		}

		@Test
		void participatesToVariantCalculation() {
			val v0 = newAxisValue("par");
			val v1 = newAxisValue("buk");
			subject().set(of(v0, v1));
			assertThat(component().getBuildVariants(), providerOf(allOf(hasItem(hasAxisOf(v0)), hasItem(hasAxisOf(v1)))));
		}

		@Test
		void throwsExceptionIfAxisPropertyHasNoValues() {
			subject().value((Iterable<? extends T>) null);
			subject().convention((Iterable<? extends T>) null);
			assertThat(component().getVariants().getElements(), hasNoValue());
		}

		@Test
		void throwsExceptionIfAxisPropertyHasEmptyValueSet() {
			subject().value(of());
			subject().convention(of());
			val ex = assertThrows(RuntimeException.class, () -> component().getVariants().get()); // realize variants

			// if provider has display name, the exception is wrapped
			assertThat(ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage(),
				startsWith("A my axis needs to be specified for"));
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
			component().getDimensions().newAxis(MyOtherAxisA.class).value(of(a0, a1));
			component().getDimensions().newAxis(MyOtherAxisB.class).value(of(b0));
			subject = component().getDimensions().newAxis(MyAxis.class, builder -> builder.onlyOn(a0));
		}

		@Override
		public SetProperty<MyAxis> subject() {
			return subject;
		}

		@Test
		void filtersOutAllBuildVariantsForCurrentAxisNotComplementingSpecifiedAxisValue() {
			val v0 = new MyAxis("drf");
			subject.set(of(v0));
			assertThat(component().getBuildVariants(), providerOf(not(hasItem(allOf(hasAxisOf(v0), hasAxisOf(a1))))));
			assertThat(component().getBuildVariants(), providerOf(hasItem(allOf(hasAxisOf(v0), hasAxisOf(a0)))));
			assertThat(component().getBuildVariants(), providerOf(everyItem(hasAxisOf(b0))));
		}
	}

	@Nested
	class NewAxisExcludeAnotherAxisTest extends NewAxisTester<MyAxis> {
		private final MyOtherAxisA a0 = new MyOtherAxisA("a0");
		private final MyOtherAxisA a1 = new MyOtherAxisA("a1");
		private final MyOtherAxisB b0 = new MyOtherAxisB("b0");
		private SetProperty<MyAxis> subject;

		@BeforeEach
		void createAxis() {
			component().getDimensions().newAxis(MyOtherAxisA.class).value(of(a0, a1));
			component().getDimensions().newAxis(MyOtherAxisB.class).value(of(b0));
			subject = component().getDimensions().newAxis(MyAxis.class, builder -> builder.exceptOn(a0));
		}

		@Override
		public SetProperty<MyAxis> subject() {
			return subject;
		}

		@Test
		void filtersOutAllBuildVariantsWhereCurrentAxisAndSpecifiedAxisValue() {
			val v0 = new MyAxis("fig");
			subject.set(of(v0));
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
