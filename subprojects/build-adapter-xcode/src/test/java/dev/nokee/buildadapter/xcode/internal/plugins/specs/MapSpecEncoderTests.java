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

import com.google.common.collect.ImmutableMap;
import dev.nokee.internal.testing.testdoubles.TestDouble;
import dev.nokee.xcode.project.CodingKey;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.Encodeable;
import dev.nokee.xcode.project.KeyedObject;
import dev.nokee.xcode.project.ValueEncoder;
import dev.nokee.xcode.project.coders.BycopyObject;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.any;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class MapSpecEncoderTests {
	ValueEncoder.Context context = newMock(ValueEncoder.Context.class)
		.when(any(callTo(method(ValueEncoder.Context::encodeBycopyObject))).then(doReturn((it, args) -> new TestBycopyObject(args.getArgument(0)))))
		.alwaysThrows().instance();
	TestDouble<ValueEncoder<Encodeable, KeyedObject>> delegate = newMock(ValueEncoder.class)
		.when(any(callTo(method(ValueEncoder<Encodeable, KeyedObject>::encode))).then(doReturn((it, args) -> args.getArgument(0))));
	MapSpecEncoder<KeyedObject> subject = new MapSpecEncoder<>(delegate.instance());

	KeyedObject objectToEncode = DefaultKeyedObject.builder().put(key("a"), "A").put(key("b"), "B").build();
	XCBuildSpec result = subject.encode(objectToEncode, context);

	@Test
	void canEncodeObjectToBuildSpec() {
		assertThat(result, equalTo(new NestedMapSpec(ImmutableMap.of("a", spec("A"), "b", spec("B")))));
	}

	@Test
	void callsDelegateWithInputValue() {
		assertThat(delegate.to(method(ValueEncoder<Encodeable, KeyedObject>::encode)), calledOnceWith(objectToEncode, context));
	}

	private static CodingKey key(String name) {
		return () -> name;
	}

	private static TestBycopyObject.Spec spec(String value) {
		return new TestBycopyObject.Spec(value);
	}

	@EqualsAndHashCode
	private static final class TestBycopyObject implements BycopyObject {
		private final Encodeable object;

		public TestBycopyObject(Encodeable object) {
			this.object = object;
		}

		@Override
		public Map<String, ?> asMap() {
			val builder = ImmutableMap.<String, XCBuildSpec>builder();
			object.encode(new Encodeable.EncodeContext() {
				@Override
				public void base(Map<String, ?> fields) {

				}

				@Override
				public void gid(String globalID) {

				}

				@Override
				public void tryEncode(Map<CodingKey, ?> data) {
					data.forEach((k, v) -> builder.put(k.getName(), new Spec(v)));
				}

				@Override
				public void noGid() {

				}
			});
			return builder.build();
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
}
