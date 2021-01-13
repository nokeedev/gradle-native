package dev.nokee.internal.testing;

import org.gradle.api.provider.Provider;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class GradleProviderMatchers {
	private GradleProviderMatchers() {}

	public static <T> Matcher<Provider<T>> providerOf(Matcher<T> matcher) {
		return new FeatureMatcher<Provider<T>, T>(matcher, "provider of", "providing") {
			@Override
			protected T featureValueOf(Provider<T> actual) {
				return actual.get();
			}
		};
	}
}
