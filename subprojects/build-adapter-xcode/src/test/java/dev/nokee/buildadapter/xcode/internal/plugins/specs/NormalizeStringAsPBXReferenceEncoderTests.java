/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal.plugins.specs;

import com.google.common.reflect.TypeToken;
import dev.nokee.internal.testing.testdoubles.TestDouble;
import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.project.ValueEncoder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.neverCalled;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.any;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.xcode.objects.files.PBXFileReference.ofAbsolutePath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class NormalizeStringAsPBXReferenceEncoderTests {
	ValueEncoder.Context context = newAlwaysThrowingMock(ValueEncoder.Context.class);
	TestDouble<ValueEncoder<PBXReference, Object>> delegate = newMock(new TypeToken<ValueEncoder<PBXReference, Object>>() {})
		.when(any(callTo(method(ValueEncoder<PBXReference, Object>::encode))).then(doReturn(ofAbsolutePath("/dummy/path.txt"))));
	NormalizeStringAsPBXReferenceEncoder subject = new NormalizeStringAsPBXReferenceEncoder(delegate);

	@Nested
	class WhenEncodingValidValues {
		PBXReference result = subject.encode("$(SOURCE_ROOT)/foo.txt", context);

		@Test
		void canEncodePBXFileReference() {
			assertThat(result, equalTo(ofAbsolutePath("$(SOURCE_ROOT)/foo.txt")));
		}

		@Test
		void doesNotDelegate() {
			assertThat(delegate.to(method(ValueEncoder<PBXReference, Object>::encode)), neverCalled());
		}
	}

	@Nested
	class WhenEncodingInvalidValues {
		Object value = new Object();
		PBXReference ignored = subject.encode(value, context);

		@Test
		void callsDelegate() {
			assertThat(delegate.to(method(ValueEncoder<PBXReference, Object>::encode)), calledOnceWith(value, context));
		}
	}
}
