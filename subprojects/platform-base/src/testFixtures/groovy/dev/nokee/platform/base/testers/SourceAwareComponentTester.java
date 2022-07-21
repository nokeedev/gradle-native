/*
 * Copyright 2020-2021 the original author or authors.
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

import dev.nokee.internal.testing.FileSystemWorkspace;
import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.platform.base.SourceAwareComponent;
import groovy.lang.Closure;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.stream.Stream;

import static dev.nokee.internal.testing.ExecuteWith.action;
import static dev.nokee.internal.testing.ExecuteWith.called;
import static dev.nokee.internal.testing.ExecuteWith.closure;
import static dev.nokee.internal.testing.ExecuteWith.executeWith;
import static dev.nokee.internal.testing.ExecuteWith.lastArgument;
import static dev.nokee.language.base.testing.LanguageSourceSetMatchers.sourceSetOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface SourceAwareComponentTester<T extends SourceAwareComponent<?>> {
	default T createSubject() {
		return createSubject("main");
	}

	T createSubject(String componentName);

	Stream<SourcesUnderTest> provideSourceSetUnderTest();

	@ParameterizedTest
	@MethodSource("provideSourceSetUnderTest")
	default void canAccessSourceSetViaAccessor(SourcesUnderTest sources) throws Throwable {
		MatcherAssert.assertThat(sources.accessor(createSubject()).invoke(), sources.asMatcher());
	}

	@ParameterizedTest
	@MethodSource("provideSourceSetUnderTest")
	default void canConfigureSourceSetViaMethodUsingAction(SourcesUnderTest sources) {
		val subject = createSubject();
		((FunctionalSourceSet) subject.getSources()).get();
		assertThat(executeWith(action(sources.configureUsingAction(subject)::invoke)),
			allOf(called(anyOf(equalTo(1), equalTo(2))), lastArgument(sources.asMatcher())));
	}

	@ParameterizedTest
	@MethodSource("provideSourceSetUnderTest")
	default void canConfigureSourceSetViaMethodUsingClosure(SourcesUnderTest sources) {
		val subject = createSubject();
		((FunctionalSourceSet) subject.getSources()).get();
		assertThat(executeWith(closure(sources.configureUsingClosure(subject)::invoke)),
			allOf(called(anyOf(equalTo(1), equalTo(2))), lastArgument(sources.asMatcher())));
	}

	@ParameterizedTest
	@MethodSource("provideSourceSetUnderTest")
	default void checkSourceSetAccessorAndConfigurationMethodsPrototypeIntegrity(SourcesUnderTest sources) {
		val subject = createSubject();
		assertThat("accessor should return the public source set type", sources.accessor(subject).getReturnType(), equalTo(sources.getType()));
		assertThat("configuration method using action should not return anything", sources.configureUsingAction(subject).getReturnType(), equalTo(Void.TYPE));
		assertThat("configuration method using closure should not return anything", sources.configureUsingClosure(subject).getReturnType(), equalTo(Void.TYPE));
	}

	@ParameterizedTest
	@MethodSource("provideSourceSetUnderTest")
	default void hasConventionSourceSet(SourcesUnderTest sources) throws Throwable {
		// There may be more convention, but at the very least, use the default Maven convention
		val a = new FileSystemWorkspace(getTestDirectory());
		assertThat(sources.get(createSubject("main")).getSourceDirectories(),
			hasItem(a.file("src/main/" + sources.getName())));
		assertThat(sources.get(createSubject("test")).getSourceDirectories(),
			hasItem(a.file("src/test/" + sources.getName())));
	}

	File getTestDirectory();

	final class SourcesUnderTest {
		private final String name;
		private final Class<? extends LanguageSourceSet> type;
		private final String sourceSetAccessorName;

		public SourcesUnderTest(String name, Class<? extends LanguageSourceSet> type, String sourceSetAccessorName) {
			this.name = name;
			this.type = type;
			this.sourceSetAccessorName = sourceSetAccessorName;
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
		public LanguageSourceSet get(SourceAwareComponent<?> target) {
			return accessor(target).invoke();
		}

		@SneakyThrows
		InvokableMethod<LanguageSourceSet> accessor(SourceAwareComponent<?> target) {
			return InvokableMethod.bind(target.getClass().getMethod("get" + StringUtils.capitalize(sourceSetAccessorName)), target);
		}

		@SneakyThrows
		InvokableMethod<Void> configureUsingAction(SourceAwareComponent<?> target) {
			return InvokableMethod.bind(target.getClass().getMethod(sourceSetAccessorName, Action.class), target);
		}

		@SneakyThrows
		InvokableMethod<Void> configureUsingClosure(SourceAwareComponent<?> target) {
			return InvokableMethod.bind(target.getClass().getMethod(sourceSetAccessorName, Closure.class), target);
		}
	}
}
