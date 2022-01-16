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

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public final class CallableMatchers {

	public static <T> Matcher<Object> callableOf(Matcher<? super T> matcher) {
		return new FeatureMatcher<Object, T>(matcher, "", "") {
			@Override
			@SuppressWarnings("unchecked")
			protected T featureValueOf(Object actual) {
				return assertDoesNotThrow(((Callable<T>) actual)::call);
			}
		};
	}
}
