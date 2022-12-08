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
package dev.nokee.internal.testing;

import org.gradle.api.specs.Spec;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.is;

public final class GradleSpecMatchers {
	private GradleSpecMatchers() {}

	public static <T> Matcher<Spec<T>> unsatisfiedBy(T target) {
		return new FeatureMatcher<Spec<T>, Boolean>(is(false), "", "") {
			@Override
			protected Boolean featureValueOf(Spec<T> actual) {
				return actual.isSatisfiedBy(target);
			}
		};
	}

	public static <T> Matcher<Spec<T>> satisfiedBy(T target) {
		return new FeatureMatcher<Spec<T>, Boolean>(is(true), "", "") {
			@Override
			protected Boolean featureValueOf(Spec<T> actual) {
				return actual.isSatisfiedBy(target);
			}
		};
	}
}
