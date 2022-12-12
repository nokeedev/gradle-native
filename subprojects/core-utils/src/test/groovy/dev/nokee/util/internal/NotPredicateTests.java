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
package dev.nokee.util.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Predicate;

import static dev.nokee.internal.testing.JDK8PredicateMatchers.accepts;
import static dev.nokee.internal.testing.JDK8PredicateMatchers.rejects;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class NotPredicateTests {
	@Mock Predicate<Object> delegate;
	@InjectMocks NotPredicate<Object> subject;

	@Nested
	class WhenDelegateAlwaysTrue {
		@BeforeEach
		void givenDelegateAcceptsEverything() {
			Mockito.when(delegate.test(any())).thenReturn(true);
		}

		@Test
		void rejectsAnyObject() {
			assertThat(subject, rejects(new Object()));
		}
	}

	@Nested
	class WhenDelegateAlwaysFalse {
		@BeforeEach
		void givenDelegateRejectsEverything() {
			Mockito.when(delegate.test(any())).thenReturn(false);
		}

		@Test
		void acceptsAnyObject() {
			assertThat(subject, accepts(new Object()));
		}
	}

	@Test
	void forwardsTestArgumentToDelegate() {
		Object value = new Object();
		subject.test(value);
		Mockito.verify(delegate).test(value);
	}
}
