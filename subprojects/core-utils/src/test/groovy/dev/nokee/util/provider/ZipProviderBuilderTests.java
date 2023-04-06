/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.util.provider;

import com.google.common.collect.ImmutableList;
import dev.nokee.internal.testing.MockitoUtils;
import lombok.val;
import org.gradle.api.provider.Provider;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.absentProvider;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.ProjectMatchers.buildDependencies;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.util.provider.ZipProviderBuilder.newBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

class ZipProviderBuilderTests {
	interface ZipProviderBuilderTester {
		ZipProviderBuilder subject();

		@Test
		default void doesNotCallCombinerWhenZippedProviderNotRealized() {
			assertDoesNotThrow(() -> subject().zip(alwaysThrowCombiner()));
		}
	}

	@Nested
	class NoElementToZipProvider {
		ZipProviderBuilder0 subject = newBuilder(objectFactory());

		@Test
		void returnsUndefinedProviderWhenNoElementsToZip_aka_neverCallsCombiner() {
			assertThat(subject.zip(alwaysThrowCombiner()), absentProvider());
		}
	}

	@Nested
	class SingleElementToZipProvider implements ZipProviderBuilderTester {
		ZipProviderBuilder1<String> subject = newBuilder(objectFactory()) //
			.value(providerFactory().provider(() -> "first-value"));

		@Override
		public ZipProviderBuilder subject() {
			return subject;
		}

		@Test
		void returnsProviderOfOnlyElementToZip() {
			assertThat(subject.zip(), providerOf("first-value"));
		}

		@Test
		void returnsProviderOfCombinerResult() {
			ZipProviderBuilder.Combiner<Integer> combiner = MockitoUtils.spy(ZipProviderBuilder.Combiner.class, count());
			assertThat(subject.zip(combiner), providerOf("first-value".length()));
			verify(combiner).combine(argThat(valuesOf(contains("first-value"))));
		}
	}

	@Nested
	class TwoElementsToZipProvider implements ZipProviderBuilderTester {
		ZipProviderBuilder2<String, Integer> subject = newBuilder(objectFactory()) //
			.value(providerFactory().provider(() -> "first-value")) //
			.value(providerFactory().provider(() -> 42));

		@Override
		public ZipProviderBuilder subject() {
			return subject;
		}

		@Test
		void returnsProviderOfTwoElementsZipped() {
			ZipProviderBuilder.Combiner<String> combiner = MockitoUtils.spy(ZipProviderBuilder.Combiner.class, join());
			assertThat(subject.zip(combiner), providerOf("first-value 42"));
			verify(combiner).combine(argThat(valuesOf(contains("first-value", 42))));
		}

		@Test
		void returnsProviderOfTwoElementsZippedUsingFunction() {
			BiFunction<String, Integer, String> combiner = MockitoUtils.spy(BiFunction.class, (a, b) -> a + " " + b);
			assertThat(subject.zip(combiner), providerOf("first-value 42"));
			verify(combiner).apply("first-value", 42);
		}
	}

	@Nested
	class MultipleElementsToZipProvider implements ZipProviderBuilderTester {
		ZipProviderBuilder subject = newBuilder(objectFactory()) //
			.value(providerFactory().provider(() -> "first-value")) //
			.value(providerFactory().provider(() -> 42)) //
			.value(providerFactory().provider(() -> false)) //
			.value(providerFactory().provider(() -> 4.2f));

		@Override
		public ZipProviderBuilder subject() {
			return subject;
		}

		@Test
		void returnsProviderOfMultipleElementsZipped() {
			ZipProviderBuilder.Combiner<String> combiner = MockitoUtils.spy(ZipProviderBuilder.Combiner.class, join());
			assertThat(subject.zip(combiner), providerOf("first-value 42 false 4.2"));
			verify(combiner).combine(argThat(valuesOf(contains("first-value", 42, false, 4.2f))));
		}
	}

	@Nested
	class ImplicitTaskDependencies {
		@Test
		void keepsImplicitTaskDependenciesWhenZippingSingleValue() {
			val project = rootProject();
			val taskDependencies = project.files(newBuilder(objectFactory())
				.value(project.getTasks().register("firstTask").map(__ -> "first"))
				.zip(alwaysThrowCombiner()));
			assertThat(taskDependencies, buildDependencies(containsInAnyOrder(named("firstTask"))));
		}

		@Test
		void mergesImplicitTaskDependenciesWhenZippingTwoValues() {
			val project = rootProject();
			val taskDependencies = project.files(newBuilder(objectFactory())
				.value(project.getTasks().register("firstTask").map(__ -> "first"))
				.value(project.getTasks().register("secondTask").map(__ -> "second"))
				.zip(alwaysThrowCombiner()));
			assertThat(taskDependencies, buildDependencies(containsInAnyOrder(named("firstTask"), named("secondTask"))));
		}

		@Test
		void mergesImplicitTaskDependenciesWhenZippingMultipleValues() {
			val project = rootProject();
			val taskDependencies = project.files(newBuilder(objectFactory())
				.value(project.getTasks().register("firstTask").map(__ -> "first"))
				.value(project.getTasks().register("secondTask").map(__ -> "second"))
				.value(project.getTasks().register("thirdTask").map(__ -> "third"))
				.value(project.getTasks().register("fourthTask").map(__ -> "fourth"))
				.zip(alwaysThrowCombiner()));
			assertThat(taskDependencies, buildDependencies(containsInAnyOrder(named("firstTask"), named("secondTask"), named("thirdTask"), named("fourthTask"))));
		}
	}

	@Nested
	class ZipBuilderIsolation {
		ZipProviderBuilder1<String> builder1 = newBuilder(objectFactory())
			.value(providerFactory().provider(() -> "first"));
		ZipProviderBuilder2<String, String> builder2 = builder1
			.value(providerFactory().provider(() -> "second"));
		ZipProviderBuilder builder3 = builder2
			.value(providerFactory().provider(() -> "third"))
			.value(providerFactory().provider(() -> "fourth"));

		@Test
		void singleValueBuilderDoesNotContainsAddedValues() {
			assertThat(builder1.zip(ZipProviderBuilder.ValuesToZip::values),
				providerOf(arrayContaining("first")));
		}

		@Test
		void dualValueBuilderDoesNotContainsAddedValues() {
			assertThat(builder2.zip(ZipProviderBuilder.ValuesToZip::values),
				providerOf(arrayContaining("first", "second")));
		}

		@Test
		void multipleZippedValueOnlyContainsValuesUpToZipping() {
			Provider<Object[]> provider = builder3.zip(ZipProviderBuilder.ValuesToZip::values);

			// this value should not be present in provider
			builder3.value(providerFactory().provider(() -> "fifth"));

			assertThat(provider, providerOf(arrayContaining("first", "second", "third", "fourth")));
		}
	}

	static <R> ZipProviderBuilder.Combiner<R> alwaysThrowCombiner() {
		return __ -> { throw new UnsupportedOperationException(); };
	}

	static ZipProviderBuilder.Combiner<String> join() {
		return it -> Arrays.stream(it.values()).map(Object::toString).collect(Collectors.joining(" "));
	}

	static ZipProviderBuilder.Combiner<Integer> count() {
		return join().andThen(String::length);
	}

	static Matcher<ZipProviderBuilder.ValuesToZip> valuesOf(Matcher<? super Iterable<Object>> matcher) {
		return new FeatureMatcher<ZipProviderBuilder.ValuesToZip, Iterable<Object>>(matcher, "", "") {
			@Override
			protected Iterable<Object> featureValueOf(ZipProviderBuilder.ValuesToZip actual) {
				return ImmutableList.copyOf(actual.values());
			}
		};
	}
}
