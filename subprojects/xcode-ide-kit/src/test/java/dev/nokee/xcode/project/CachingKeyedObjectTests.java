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
package dev.nokee.xcode.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.nokee.xcode.project.CachingKeyedObjectTests.CodingKeys.keyA;
import static dev.nokee.xcode.project.CachingKeyedObjectTests.CodingKeys.keyB;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CachingKeyedObjectTests {
	@Mock KeyedObject delegate;


	enum CodingKeys implements CodingKey {
		keyA, keyB;

		@Override
		public String getName() {
			return name();
		}
	}

	@Nested
	class Equality {
		CachingKeyedObject left;
		CachingKeyedObject right;

		@BeforeEach
		void givenSubjects() {
			left = new CachingKeyedObject(delegate);
			right = new CachingKeyedObject(delegate);
		}

		@Nested
		class WhenNoCaching {
			@Test
			void isEqual() {
				assertThat(left, equalTo(right));
			}
		}

		@Nested
		class WhenDifferentDelegates {
			@Test
			void isNotEqual() {
				assertThat(left, not(equalTo(new CachingKeyedObject(mock(KeyedObject.class)))));
			}
		}

		@Nested
		class WhenDifferentCaching {
			@BeforeEach
			void givenDifferentCachingStatus() {
				when(delegate.tryDecode(keyA)).thenReturn("value-a");
				when(delegate.tryDecode(keyB)).thenReturn("value-b");

				left.tryDecode(keyA);
				right.tryDecode(keyA);
				right.tryDecode(keyB);
			}

			@Test
			void isEqual() {
				assertThat(left, equalTo(right));
			}
		}
	}

	@Nested
	class TryDecode {
		CachingKeyedObject subject;

		@BeforeEach
		void givenSubject() {
			when(delegate.tryDecode(any())).thenReturn("value");
			subject = new CachingKeyedObject(delegate);
		}

		@Test
		void forwardsTryDecodeToDelegate() {
			assertThat(subject.tryDecode(keyA), equalTo("value"));
			verify(delegate).tryDecode(keyA);
		}
	}

	@Nested
	class Isa {
		CachingKeyedObject subject;

		@BeforeEach
		void givenSubject() {
			when(delegate.tryDecode(any())).thenReturn("PBXObject");
			subject = new CachingKeyedObject(delegate);
		}

		@Test
		void forwardsIsaToDelegate() {
			assertThat(subject.tryDecode(KeyedCoders.ISA), equalTo("PBXObject"));
			verify(delegate).tryDecode(KeyedCoders.ISA);
		}
	}

	@Nested
	class Encode {
		@Mock Codeable.EncodeContext context;
		CachingKeyedObject subject;

		@BeforeEach
		void givenSubject() {
			subject = new CachingKeyedObject(delegate);
		}

		@Test
		void forwardsEncodeToDelegate() {
			subject.encode(context);
			verify(delegate).encode(context);
		}
	}
}
