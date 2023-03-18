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

import com.google.common.collect.ImmutableList;
import dev.nokee.internal.testing.testdoubles.TestDouble;
import dev.nokee.xcode.project.ValueEncoder;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.any;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class AtNestedCollectionEncoderTests {
	ValueEncoder.Context context = newMock(ValueEncoder.Context.class).alwaysThrows().instance();
	TestDouble<ValueEncoder<List<XCBuildSpec>, List<String>>> delegate = newMock(ValueEncoder.class)
		.when(any(callTo(method(ValueEncoder<List<XCBuildSpec>, List<String>>::encode))).then(doReturn((it, args) -> {
			List<String> values = args.getArgument(0);
			return values.stream().map(Object::toString).map(AtNestedCollectionEncoderTests::spec).collect(Collectors.toList());
		})));

	List<String> objectToEncode = ImmutableList.of("a", "b", "c");
	AtNestedCollectionEncoder<List<String>> subject = new AtNestedCollectionEncoder<>(ImmutableList.toImmutableList(), delegate.instance());
	XCBuildSpec result = subject.encode(objectToEncode, context);

	@Test
	void canEncodeObjectToBuildSpec() {
		assertThat(result, equalTo(new AtNestedCollectionEncoder.Spec(ImmutableList.toImmutableList(), ImmutableList.of(spec("a"), spec("b"), spec("c")))));
	}

	@Test
	void callsDelegateWithInputValue() {
		assertThat(delegate.to(method(ValueEncoder<List<XCBuildSpec>, List<String>>::encode)), calledOnceWith(objectToEncode, context));
	}

	private static Spec spec(String value) {
		return new Spec(value);
	}

	@EqualsAndHashCode
	private static final class Spec implements XCBuildSpec {
		private final Object value;

		public Spec(Object value) {
			this.value = value;
		}

		@Override
		public XCBuildPlan resolve(ResolveContext context) {
			throw new UnsupportedOperationException();
		}
	}
}
