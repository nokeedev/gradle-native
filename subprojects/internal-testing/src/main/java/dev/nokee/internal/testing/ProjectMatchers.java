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

import org.gradle.api.NamedDomainObjectCollectionSchema;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionsSchema;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.equalTo;

public final class ProjectMatchers {
	private ProjectMatchers() {}

	public static Matcher<PluginAware> hasPlugin(String id) {
		requireNonNull(id);
		return new TypeSafeMatcher<PluginAware>() {
			@Override
			protected boolean matchesSafely(PluginAware actual) {
				return actual.getPluginManager().hasPlugin(id);
			}

			@Override
			protected void describeMismatchSafely(PluginAware item, Description description) {
				description.appendText("plugin ").appendValue(id).appendText(" is not applied to ").appendValue(item);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("a plugin aware object has plugin ").appendValue(id).appendText(" applied");
			}
		};
	}

	/**
	 * Returns a matcher for {@link ExtensionAware} objects.
	 * Note the matcher will throw an exception if the actual object is not extension aware.
	 *
	 * @param matcher  the extensions matcher, must not be null
	 * @return an extensions matcher, never null
	 */
	public static Matcher<Object> extensions(Matcher<? super Iterable<ExtensionsSchema.ExtensionSchema>> matcher) {
		return new FeatureMatcher<Object, Iterable<ExtensionsSchema.ExtensionSchema>>(matcher, "a extension aware object", "extension aware object") {
			@Override
			protected Iterable<ExtensionsSchema.ExtensionSchema> featureValueOf(Object actual) {
				return ((ExtensionAware) actual).getExtensions().getExtensionsSchema().getElements();
			}
		};
	}

	public static <T> Matcher<T> publicType(TypeOf<?> instance) {
		return publicType(equalTo(instance));
	}

	public static <T> Matcher<T> publicType(Matcher<? super TypeOf<?>> matcher) {
		return new FeatureMatcher<T, TypeOf<?>>(matcher, "an object with public type", "object with public type") {
			@Override
			protected TypeOf<?> featureValueOf(T actual) {
				if (actual instanceof HasPublicType) {
					return ((HasPublicType) actual).getPublicType();
				} else if (actual instanceof NamedDomainObjectCollectionSchema.NamedDomainObjectSchema) {
					return ((NamedDomainObjectCollectionSchema.NamedDomainObjectSchema) actual).getPublicType();
				}

				throw new UnsupportedOperationException(String.format("Object '%s' of type %s is not public type aware.", actual, actual.getClass().getCanonicalName()));
			}
		};
	}
}
