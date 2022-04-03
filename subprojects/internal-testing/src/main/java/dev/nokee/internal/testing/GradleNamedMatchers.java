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
package dev.nokee.internal.testing;

import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectCollectionSchema;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.SourceSet;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.equalTo;

public final class GradleNamedMatchers {
	private GradleNamedMatchers() {}

	public static <T> Matcher<T> named(String name) {
		return named(equalTo(requireNonNull(name)));
	}

	public static <T> Matcher<T> named(Matcher<? super String> matcher) {
		return new FeatureMatcher<T, String>(requireNonNull(matcher), "an object named", "the object's name") {
			@Override
			protected String featureValueOf(T actual) {
				if (actual instanceof Named) {
					return ((Named) actual).getName();
				}
				// Configuration class somewhat behave like a Named class
				else if (actual instanceof Configuration) {
					return ((Configuration) actual).getName();
				}
				// Task class somewhat behave like a Named class
				else if (actual instanceof Task) {
					return ((Task) actual).getName();
				}

				// SourceSet class somewhat behave like a Named class
				else if (actual instanceof SourceSet) {
					return ((SourceSet) actual).getName();
				}

				// NamedDomainObjectProvider class somewhat behave like a Named class
				//   in theory, all default implementation implements Named class
				else if (actual instanceof NamedDomainObjectProvider) {
					return ((NamedDomainObjectProvider) actual).getName();
				}

				// NamedDomainObjectSchema class somewhat behave like a Named class
				else if (actual instanceof NamedDomainObjectCollectionSchema.NamedDomainObjectSchema) {
					return ((NamedDomainObjectCollectionSchema.NamedDomainObjectSchema) actual).getName();
				}

				// Class with getName() returning String somewhat behave like a Named class
				try {
					final Method getNameMethod = actual.getClass().getMethod("getName");
					if (String.class.isAssignableFrom(getNameMethod.getReturnType())) {
						try {
							return (String) getNameMethod.invoke(actual);
						} catch (InvocationTargetException | IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					}
				} catch (NoSuchMethodException e) {
					// do nothing, no getName() methods
				}

				throw new UnsupportedOperationException(String.format("Object '%s' of type %s is not named-able.", actual, actual.getClass().getCanonicalName()));
			}
		};
	}
}
