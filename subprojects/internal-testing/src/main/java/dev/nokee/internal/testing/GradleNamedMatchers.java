package dev.nokee.internal.testing;

import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectCollectionSchema;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.SourceSet;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

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

				// NamedDomainObjectProvider class somewhare behave like a Named class
				//   in theory, all default implementation implements Named class
				else if (actual instanceof NamedDomainObjectProvider) {
					return ((NamedDomainObjectProvider) actual).getName();
				}

				// NamedDomainObjectSchema class somewhat behave like a Named class
				else if (actual instanceof NamedDomainObjectCollectionSchema.NamedDomainObjectSchema) {
					return ((NamedDomainObjectCollectionSchema.NamedDomainObjectSchema) actual).getName();
				}

				throw new UnsupportedOperationException(String.format("Object '%s' of type %s is not named-able.", actual, actual.getClass().getCanonicalName()));
			}
		};
	}
}
