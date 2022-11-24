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

import dev.gradleplugins.runnerkit.BuildResult;
import lombok.val;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsNot;

import java.util.regex.Pattern;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

public final class GradleConfigurationCacheMatchers {
	private GradleConfigurationCacheMatchers() {}

	public static Matcher<BuildResult> configurationCache(Matcher<? super ConfigurationCacheResult> matcher) {
		assert !(matcher instanceof IsNot) : "not matcher is almost certain to be wrong, use reused(), calculated() or recalculated()";

		return new FeatureMatcher<BuildResult, ConfigurationCacheResult>(matcher, "", "") {
			@Override
			protected ConfigurationCacheResult featureValueOf(BuildResult actual) {
				if (actual.getOutput().contains("Reusing configuration cache.")) {
					return new ReusedConfigurationCacheResult();
				}

				val matcher = Pattern.compile("^Calculating task graph as (.+)\\.$", Pattern.MULTILINE).matcher(actual.getOutput());
				if (matcher.find()) {
					return new CalculatedConfigurationCacheResult(matcher.group(1));
				}

				return new DisabledConfigurationCacheResult();
			}
		};
	}

	public static Matcher<ConfigurationCacheResult> reused() {
		return isA(ReusedConfigurationCacheResult.class);
	}

	public static Matcher<ConfigurationCacheResult> calculated() {
		return isA(CalculatedConfigurationCacheResult.class);
	}

	public static Matcher<ConfigurationCacheResult> calculated(Matcher<? super String> reasonMatcher) {
		return allOf(isA(CalculatedConfigurationCacheResult.class), new FeatureMatcher<ConfigurationCacheResult, String>(reasonMatcher, "", "") {
			@Override
			protected String featureValueOf(ConfigurationCacheResult actual) {
				assert actual instanceof CalculatedConfigurationCacheResult;
				return ((CalculatedConfigurationCacheResult) actual).getCalculatedReason();
			}
		});
	}

	public static Matcher<ConfigurationCacheResult> recalculated() {
		return calculated(not(startsWith("no configuration cache is available")));
	}

	public interface ConfigurationCacheResult {
		boolean wasEnabled();
	}

	private static final class DisabledConfigurationCacheResult implements ConfigurationCacheResult {
		@Override
		public boolean wasEnabled() {
			return false;
		}
	}

	private static final class ReusedConfigurationCacheResult implements ConfigurationCacheResult {
		@Override
		public boolean wasEnabled() {
			return true;
		}
	}

	private static final class CalculatedConfigurationCacheResult implements ConfigurationCacheResult {
		private final String because;

		private CalculatedConfigurationCacheResult(String because) {
			this.because = because;
		}

		public String getCalculatedReason() {
			return because;
		}

		@Override
		public boolean wasEnabled() {
			return true;
		}
	}

	private static Matcher<BuildResult> outputMatches(Matcher<? super String> matcher) {
		return new FeatureMatcher<BuildResult, String>(matcher, "", "") {
			@Override
			protected String featureValueOf(BuildResult actual) {
				return actual.getOutput();
			}
		};
	}
}
