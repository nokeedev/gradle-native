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
package dev.nokee.platform.cpp;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public final class RegressionMatchers {
	public static Matcher<Iterable<BuildExperimentResult>> hasNotRegressed() {
		return new TypeSafeDiagnosingMatcher<Iterable<BuildExperimentResult>>() {
			@Override
			protected boolean matchesSafely(Iterable<BuildExperimentResult> item, Description mismatchDescription) {
				return false;
			}

			@Override
			public void describeTo(Description description) {

			}
		};
	}

	public static Matcher<BuildExperimentResult> hasRegressed(BuildExperimentResult baseline) {
		return new TypeSafeDiagnosingMatcher<BuildExperimentResult>() {
			@Override
			protected boolean matchesSafely(BuildExperimentResult item, Description mismatchDescription) {
				return false;
			}

			@Override
			public void describeTo(Description description) {

			}
		};
	}
}
