/*
 * Copyright 2020 the original author or authors.
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
import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.platform.base.ComponentSources;
import groovy.lang.Closure;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.reflect.TypeOf;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Stream;

import static dev.nokee.internal.testing.ExecuteWith.*;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.language.base.testing.LanguageSourceSetMatchers.sourceSetOf;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface ComponentSourcesTester<T extends ComponentSources> {
	T createSubject();
	Stream<SourceSetUnderTest> provideSourceSetUnderTest();

	@Test
	default void hasExpectedSourceSets() {
		assertThat(asView(createSubject()).get(), containsInAnyOrder(provideSourceSetUnderTest().map(SourceSetUnderTest::asMatcher).collect(toList())));
	}

	static FunctionalSourceSet asView(ComponentSources sources) {
		return (FunctionalSourceSet) sources;
	}

	@ParameterizedTest
	@MethodSource("provideSourceSetUnderTest")
	default void canAccessSourceSetViaTypeSafeAccessor(SourceSetUnderTest sourceSet) throws Throwable {
		assertThat(sourceSet.typeSafeAccessor(createSubject()).invoke(), providerOf(sourceSet.asMatcher()));
	}

	@ParameterizedTest
	@MethodSource("provideSourceSetUnderTest")
	default void canConfigureSourceSetViaTypeSafeMethodUsingAction(SourceSetUnderTest sourceSet) {
		assertThat(executeWith(action(sourceSet.typeSafeConfigureUsingAction(createSubject()))),
			allOf(called(anyOf(equalTo(1), equalTo(2))), lastArgument(sourceSet.asMatcher())));
	}

	@ParameterizedTest
	@MethodSource("provideSourceSetUnderTest")
	default void canConfigureSourceSetViaTypeSafeMethodUsingClosure(SourceSetUnderTest sourceSet) {
		assertThat(executeWith(closure(sourceSet.typeSafeConfigureUsingClosure(createSubject()))),
			allOf(called(anyOf(equalTo(1), equalTo(2))), lastArgument(sourceSet.asMatcher())));
	}

	@ParameterizedTest
	@MethodSource("provideSourceSetUnderTest")
	default void checkSourceSetTypeSafeAccessorAndConfigurationMethodsPrototypeIntegrity(SourceSetUnderTest sourceSet) throws Throwable {
		val subject = createSubject();
		assertThat("accessor should return a provider", TypeToken.of(sourceSet.typeSafeAccessor(subject).getReturnType()).getRawType(), equalTo(NamedDomainObjectProvider.class));
		assertThat("accessor provider should provide the public source set type", sourceSet.typeSafeAccessor(subject).invoke().get(), isA(sourceSet.getType()));
//		assertThat("configuration method using action should not return anything", sourceSet.configureUsingAction(subject).getReturnType(), equalTo(Void.TYPE));
//		assertThat("configuration method using closure should not return anything", sourceSet.configureUsingClosure(subject).getReturnType(), equalTo(Void.TYPE));
	}

	final class SourceSetUnderTest {
		private final String name;
		private final Class<? extends LanguageSourceSet> type;

		public SourceSetUnderTest(String name, Class<? extends LanguageSourceSet> type) {
			this.name = name;
			this.type = type;
		}

		String getName() {
			return name;
		}

		Class<? extends LanguageSourceSet> getType() {
			return type;
		}

		<T extends LanguageSourceSet> Matcher<T> asMatcher() {
			return sourceSetOf(getName(), getType());
		}

		@SneakyThrows
		InvokableMethod<NamedDomainObjectProvider<? extends LanguageSourceSet>> typeSafeAccessor(ComponentSources target) {
			return InvokableMethod.bind(target.getClass().getMethod("get" + StringUtils.capitalize(getName())), target);
		}

//		@SneakyThrows
//		@SuppressWarnings("unchecked")
//		ThrowingSupplier<DomainObjectProvider<? extends LanguageSourceSet>> typeSafeAccessor(ComponentSources target) {
//			val method = target.getClass().getMethod("get" + StringUtils.capitalize(getName()));
//
//			// Should we assert this here?
//			assertThat("accessor should return the public source set type", method.getReturnType(), equalTo(getType()));
//			return () -> (DomainObjectProvider<? extends LanguageSourceSet>) method.invoke(target);
//		}

		@SneakyThrows
		ThrowingConsumer<Action<? super LanguageSourceSet>> typeSafeConfigureUsingAction(ComponentSources target) {
			assumeSourceSetNameIsNotJavaKeyword("it cannot have a type safe configuration method using action");
			val method = target.getClass().getMethod(getName(), Action.class);

			// TODO: Should we assert this here?
			assertThat("configuration method should not return anything", method.getReturnType(), equalTo(Void.TYPE));
			return action -> method.invoke(target, action);
		}

		@SneakyThrows
		ThrowingConsumer<Closure<Void>> typeSafeConfigureUsingClosure(ComponentSources target) {
			assumeSourceSetNameIsNotJavaKeyword("it cannot have a type safe configuration method using closure");
			val method = target.getClass().getMethod(getName(), Closure.class);

			// TODO: Should we assert this here?
			assertThat("configuration method should not return anything", method.getReturnType(), equalTo(Void.TYPE));
			return closure -> method.invoke(target, closure);
		}

		private static final Set<String> JAVA_KEYWORD = ImmutableSet.of("public");
		private void assumeSourceSetNameIsNotJavaKeyword(String because) {
			Assumptions.assumeFalse(JAVA_KEYWORD.contains(getName()), "source set '" + getName() + "' is a Java keyword hence " + because);
		}
	}
}
