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
package dev.nokee.internal.testing.util;

import groovy.lang.Closure;

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public final class ClosureTestUtils {
	private ClosureTestUtils() {}

	public static <S> Closure<Void> adaptToClosure(Consumer<? super S> action) {
		return new Closure<Void>(new Object()) {
			public Void doCall(S t) {
				assertThat("delegate should be the first parameter", getDelegate(), equalTo(t));
				assertThat("resolve strategy should be delegate first", getResolveStrategy(), equalTo(Closure.DELEGATE_FIRST));
				action.accept(t);
				return null;
			}
		};
	}
}
