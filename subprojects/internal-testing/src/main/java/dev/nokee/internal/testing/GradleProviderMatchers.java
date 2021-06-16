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

	public static <T> Matcher<Provider<? extends T>> providerOf(Matcher<T> matcher) {
		return new FeatureMatcher<Provider<? extends T>, T>(matcher, "provider of", "providing") {
			@Override
			protected T featureValueOf(Provider<? extends T> actual) {
				return actual.get();
			}
		};
	}

	public static <T> Matcher<Provider<T>> absentProvider() {
		return new FeatureMatcher<Provider<T>, ProviderState>(is(ProviderState.absent), "provider", "provider") {
			@Override
			protected ProviderState featureValueOf(Provider<T> actual) {
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
