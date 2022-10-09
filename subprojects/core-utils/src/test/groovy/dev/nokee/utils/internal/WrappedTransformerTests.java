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
package dev.nokee.utils.internal;

import com.google.common.testing.EqualsTester;
import org.gradle.api.Transformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serializable;

import static dev.nokee.internal.testing.SerializableMatchers.isSerializable;
import static dev.nokee.utils.TransformerTestUtils.aTransformer;
import static dev.nokee.utils.TransformerTestUtils.anotherTransformer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WrappedTransformerTests {
	@Nested
	class WhenTransformed {
		@Mock Transformer<String, String> transformer;
		@InjectMocks WrappedTransformer<String, String> subject;
		String result;

		@BeforeEach
		void givenTransformer() {
			when(transformer.transform(any())).thenAnswer(it -> "transformed-" + it.getArgument(0));
			result = subject.transform("input-value");
		}

		@Test
		void forwardsTransformsToDelegate() {
			verify(transformer).transform("input-value");
		}

		@Test
		void returnsTransformedValue() {
			assertThat(result, equalTo("transformed-input-value"));
		}
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquality() {
		new EqualsTester()
			.addEqualityGroup(new WrappedTransformer<>(aTransformer()), new WrappedTransformer<>(aTransformer()))
			.addEqualityGroup(new WrappedTransformer<>(anotherTransformer()))
			.testEquals();
	}

	@Test
	void canSerialize() {
		assertThat(new WrappedTransformer<>(aTransformer()), isSerializable());
	}
}
