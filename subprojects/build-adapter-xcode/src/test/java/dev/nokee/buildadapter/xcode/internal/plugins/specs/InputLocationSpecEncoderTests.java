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

import dev.nokee.internal.testing.testdoubles.TestDouble;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.project.ValueEncoder;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.any;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class InputLocationSpecEncoderTests {
	ValueEncoder.Context context = newMock(ValueEncoder.Context.class).alwaysThrows().instance();
	TestDouble<ValueEncoder<PBXReference, PBXReference>> delegate = newMock(ValueEncoder.class)
		.when(any(callTo(method(ValueEncoder<PBXReference, PBXReference>::encode))).then(doReturn((it, args) -> args.getArgument(0))));

	PBXFileReference objectToEncode = PBXFileReference.ofSourceRoot("foo.c");
	InputLocationSpecEncoder<PBXReference> subject = new InputLocationSpecEncoder<>(delegate.instance());
	XCBuildSpec result = subject.encode(objectToEncode, context);

	@Test
	void canEncodeObjectToBuildSpec() {
		assertThat(result, equalTo(new InputLocationSpec(objectToEncode)));
	}

	@Test
	void callsDelegateWithInputValue() {
		assertThat(delegate.to(method(ValueEncoder<PBXReference, PBXReference>::encode)), calledOnceWith(objectToEncode, context));
	}
}
