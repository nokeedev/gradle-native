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
package dev.nokee.xcode.project;

import com.google.common.reflect.TypeToken;
import dev.nokee.internal.testing.reflect.MethodCallable;
import dev.nokee.internal.testing.testdoubles.TestDouble;
import dev.nokee.xcode.project.forwarding.ForwardingWrapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.xcode.project.KeyedObjectCodeableAdapter.asKeyedObject;
import static dev.nokee.xcode.project.forwarding.ForwardingWrapper.forwarding;
import static dev.nokee.xcode.project.forwarding.ForwardingWrapperMatchers.forwardsToDelegate;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class CodeableAdapterTester<T extends Codeable> {
	public static MethodCallable.WithReturn.ForArg1<KeyedObject, Object, CodingKey, RuntimeException> KeyedObject_tryDecode() {
		return Decodeable::tryDecode;
	}

	public final T newSubject() {
		return newSubject(newMock(KeyedObject.class).alwaysThrows().<KeyedObject>instance());
	}

	public T newSubject(KeyedObject delegate) {
		@SuppressWarnings({"unchecked", "UnstableApiUsage"})
		final Class<T> adapterType = (Class<T>) new TypeToken<T>(getClass()) {}.getType();
		try {
			return adapterType.getConstructor(KeyedObject.class).newInstance(delegate);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public final T newSubject(Consumer<? super TestDouble<KeyedObject>> action) {
		TestDouble<KeyedObject> map = newMock(KeyedObject.class).alwaysThrows();
		action.accept(map);
		return newSubject(map.<KeyedObject>instance());
	}

	@Nested
	class ForwardTests {
		ForwardingWrapper<Codeable> wrapper = forwarding(Codeable.class, asKeyedObject(CodeableAdapterTester.this::newSubject));

		@Test
		void forwardsEncodingToDelegate() {
			assertThat(wrapper, forwardsToDelegate(method(Codeable::encode)));
		}

		@Test
		void forwardsIsaToDelegate() {
			assertThat(wrapper, forwardsToDelegate(method(Codeable::isa)));
		}

		@Test
		void forwardsGlobalIdToDelegate() {
			assertThat(wrapper, forwardsToDelegate(method(Codeable::globalId)));
		}

		@Test
		void forwardsTryDecodeToDelegate() {
			assertThat(wrapper, forwardsToDelegate(method((Codeable it, CodingKey a) -> it.tryDecode(a))));
		}
	}
}
