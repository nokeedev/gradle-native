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

import org.apache.commons.lang3.SerializationUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.Serializable;

public class SerializableMatchers {
	public static Matcher<Object> isSerializable() {
		return new TypeSafeMatcher<Object>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("serializable object");
			}

			@Override
			protected void describeMismatchSafely(Object item, Description mismatchDescription) {
				if (!(item instanceof Serializable)) {
					mismatchDescription.appendText("not serializable");
				} else if (!item.equals(SerializationUtils.clone((Serializable) item))) {
					mismatchDescription.appendText("not equals after serialization");
				}
			}

			@Override
			protected boolean matchesSafely(Object actual) {
				return actual instanceof Serializable && actual.equals(SerializationUtils.clone((Serializable) actual));
			}
		};
	}
}
