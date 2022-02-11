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

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.HasConfigurableValue;
import org.gradle.api.provider.HasMultipleValues;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.Map;
import java.util.regex.Pattern;

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

	public static <T> Matcher<Provider<? extends T>> hasNoValue() {
		return new ProviderHasNoValueMatcher<>();
	}

	private static final class ProviderHasNoValueMatcher<T> extends TypeSafeDiagnosingMatcher<Provider<? extends T>> {
		// Matches message such as:
		//   - Cannot query the value of this provider because it has no value available.
		//   - Cannot query the value of this property because it has no value available.
		//   - Cannot query the value of property 'foo' because it has no value available.
		// Note that message may include additional lines explaining the provider chain
		// which we don't care for the purpose of this matcher.
		private static final Pattern NO_VALUE_EXCEPTION_MESSAGE_PATTERN = Pattern.compile("^Cannot query the value of .+ because it has no value available\\..*", Pattern.DOTALL);

		@Override
		protected boolean matchesSafely(Provider<? extends T> item, Description mismatchDescription) {
			try {
				final T actualValue = item.get();
				mismatchDescription.appendText("has a value (").appendValue(actualValue).appendText(")");
				return false; // has value
			} catch (Throwable ex) {
				if (isNoValueException(ex)) {
					return true;
				} else {
					mismatchDescription.appendText("had unexpected exception: ").appendText(ex.getMessage());
					return false; // wrong "no value" exception,
					// it may be a computing error which is not what this matcher checks
				}
			}
		}

		private static boolean isNoValueException(Throwable ex) {
			return ex instanceof IllegalStateException
				&& NO_VALUE_EXCEPTION_MESSAGE_PATTERN.matcher(ex.getMessage()).matches();
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("has no value");
		}
	}

	@SuppressWarnings("UnstableApiUsage")
	public static Matcher<HasConfigurableValue> finalizedValue() {
		return new ProviderFinalizedMatcher();
	}

	@SuppressWarnings("UnstableApiUsage")
	private static final class ProviderFinalizedMatcher extends TypeSafeDiagnosingMatcher<HasConfigurableValue> {
		// Matches message such as:
		//   - The value for this property is final and cannot be changed any further.
		//   - The value for property 'foo' is final and cannot be changed any further.
		//   - The value for this file collection is final and cannot be changed.
		// Notice the difference in the exception message for _file collection_.
		private static final Pattern FINALIZED_VALUE_EXCEPTION_MESSAGE_PATTERN = Pattern.compile("The value for .+ is final and cannot be changed( any further)?.");

		@Override
		protected boolean matchesSafely(HasConfigurableValue item, Description mismatchDescription) {
			try {
				tryConfigureValue(item);
				mismatchDescription.appendText("was not finalized");
				return false; // has value
			} catch (Throwable ex) {
				if (isFinalizedException(ex)) {
					return true;
				} else {
					mismatchDescription.appendText("had unexpected exception: ").appendText(ex.getMessage());
					return false; // wrong finalized exception,
					// it may be a validation check or changed disallowed but not finalized which is not what this matcher checks
				}
			}
		}

		private static boolean isFinalizedException(Throwable ex) {
			return ex instanceof IllegalStateException
				&& FINALIZED_VALUE_EXCEPTION_MESSAGE_PATTERN.matcher(ex.getMessage()).matches();
		}

		@SuppressWarnings("unchecked")
		private static void tryConfigureValue(HasConfigurableValue item) {
			// We use null or empty value as it's universally accepted by all configurable types.
			if (item instanceof Property) {
				((Property<Object>) item).set((Object) null);
			} else if (item instanceof HasMultipleValues) {
				((HasMultipleValues<Object>) item).set((Iterable<Object>) null);
			} else if (item instanceof MapProperty) {
				((MapProperty<Object, Object>) item).set((Map<Object, Object>) null);
			} else if (item instanceof ConfigurableFileCollection) {
				((ConfigurableFileCollection) item).setFrom();
			} else {
				throw new UnsupportedOperationException(String.format("Unsupported configurable value: %s", item));
			}
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("is finalized");
		}
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
