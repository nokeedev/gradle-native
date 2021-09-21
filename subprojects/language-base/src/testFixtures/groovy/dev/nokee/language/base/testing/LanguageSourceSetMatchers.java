/*
 * Copyright 2020 the original author or authors.
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
