package dev.nokee.model.testing;

import dev.nokee.model.DomainObjectProvider;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class DomainObjectProviderMatchers {
	private DomainObjectProviderMatchers() {}

	public static <T> Matcher<DomainObjectProvider<T>> providerOf(Matcher<T> matcher) {
		return new FeatureMatcher<DomainObjectProvider<T>, T>(matcher, "provider of", "providing") {
			@Override
			protected T featureValueOf(DomainObjectProvider<T> actual) {
				return actual.get();
			}
		};
	}
}
