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

import org.gradle.api.provider.Provider;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public final class GradleProviderMatchers {
	private GradleProviderMatchers() {}

	public static <T> Matcher<Provider<? extends T>> providerOf(T instance) {
		return providerOf(equalTo(instance));
	}

	public static <T> Matcher<Provider<? extends T>> providerOf(Matcher<? super T> matcher) {
		return new FeatureMatcher<Provider<? extends T>, T>(matcher, "provider of", "providing") {
			@Override
			protected T featureValueOf(Provider<? extends T> actual) {
				return actual.get();
			}
		};
	}

	public static <T> Matcher<Provider<? extends T>> presentProvider() {
		return providerState(ProviderState.present);
	}

	public static <T> Matcher<Provider<? extends T>> absentProvider() {
		return providerState(ProviderState.absent);
	}

	private static <T> Matcher<Provider<? extends T>> providerState(ProviderState state) {
		return new FeatureMatcher<Provider<? extends T>, ProviderState>(is(state), "provider", "provider") {
			@Override
			protected ProviderState featureValueOf(Provider<? extends T> actual) {
				if (actual.isPresent()) {
					return ProviderState.present;
				} else {
					return ProviderState.absent;
				}
			}
		};
	}

	private enum ProviderState {
		present, absent
	}
}
