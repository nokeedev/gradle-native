package dev.nokee.language.base.testing;

import dev.nokee.language.base.LanguageSourceSet;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.*;

public final class LanguageSourceSetMatchers {
	private LanguageSourceSetMatchers() {}

	public static <T extends LanguageSourceSet> Matcher<T> sourceSetOf(String name, Class<? extends LanguageSourceSet> type) {
		return allOf(nameOf(name), instanceOf(type));
	}

	public static <T extends LanguageSourceSet> Matcher<T> nameOf(String name) {
		return new FeatureMatcher<T, String>(equalTo(name), "has name", "nameOf") {
			@Override
			protected String featureValueOf(T actual) {
				return actual.getName();
			}
		};
	}
}
