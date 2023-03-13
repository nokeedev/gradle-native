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
import dev.nokee.internal.testing.forwarding.ForwardingWrapper;
import dev.nokee.internal.testing.reflect.MethodCallable;
import dev.nokee.internal.testing.testdoubles.TestDouble;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

import static dev.nokee.internal.testing.forwarding.ForwardingWrapper.forwarding;
import static dev.nokee.internal.testing.forwarding.ForwardingWrapperMatchers.forwardsToDelegate;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.xcode.project.KeyedObjectCodeableAdapter.asKeyedObject;
import static dev.nokee.xcode.project.RecodeableKeyedObject.of;
import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public abstract class CodeableAdapterTester<T extends Codeable> {
	public static MethodCallable.WithReturn.ForArg1<KeyedObject, Object, CodingKey, RuntimeException> KeyedObject_tryDecode() {
		return Decodeable::tryDecode;
	}

	public final T newSubject() {
		return newSubject(newMock(KeyedObject.class).alwaysThrows().instance());
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
		return newSubject(map.instance());
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

	public final T newSubjectInstance(KeyedObject delegate) {
		@SuppressWarnings({"unchecked", "UnstableApiUsage"})
		final Class<T> adapterType = (Class<T>) new TypeToken<T>(getClass()) {}.getType();
		try {
			@SuppressWarnings("unchecked")
			final T result = (T) adapterType.getMethod("newInstance", KeyedObject.class).invoke(null, delegate);
			return result;
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public Matcher<T> encodeIsaCodingKeys(KeyedObject delegate) {
		return equalTo(newSubject(new RecodeableKeyedObject(delegate, ofIsaAnd(knownCodingKeys()))));
	}

	public Matcher<T> encodeKnownCodingKeys(KeyedObject delegate) {
		return equalTo(newSubject(new RecodeableKeyedObject(delegate, of(knownCodingKeys()))));
	}

	private CodingKey[] knownCodingKeys() {
		@SuppressWarnings({"unchecked", "UnstableApiUsage"})
		final Class<T> adapterType = (Class<T>) new TypeToken<T>(getClass()) {}.getType();
		try {
			@SuppressWarnings("unchecked")
			final Class<Enum<?>> codingKeysType = (Class<Enum<?>>) Class.forName(adapterType.getCanonicalName() + "$CodingKeys");
			return (CodingKey[]) codingKeysType.getEnumConstants();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
