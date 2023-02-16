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
package dev.nokee.internal.testing.reflect;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Consumer;

final class ByProxyMethodInformation<ReceiverType, ReturnInfoType extends ReturnInformation, ArgumentInfoType extends ArgumentInformation> implements MethodInformation.WithArguments<ReceiverType, ReturnInfoType, ArgumentInfoType> {
	private final Consumer<ReceiverType> methodProxy;

	public ByProxyMethodInformation(Consumer<ReceiverType> methodProxy) {
		this.methodProxy = methodProxy;
	}

	@Override
	public Method resolve(Class<ReceiverType> type) {
		final Deque<Class<?>> queue = new ArrayDeque<>();
		queue.add(type);
		Method result = null;
		while (!queue.isEmpty()) {
			@SuppressWarnings("unchecked")
			final Class<ReceiverType> candidate = (Class<ReceiverType>) queue.pop();
			try {
				result = resolve(candidate, methodProxy);
			} catch (Throwable t) {
				// ignore for now...
				if (!candidate.equals(Object.class)) {
					queue.addAll(Arrays.asList(candidate.getInterfaces()));
					queue.add(candidate.getSuperclass());
				}
			}
		}
		return Objects.requireNonNull(result);
	}

	private static <ReceiverType> Method resolve(Class<ReceiverType> type, Consumer<ReceiverType> methodProxy) {
		try {
			return ReflectionProxyMethodReference.on(type).to(methodProxy);
		} catch (Throwable e) {
			return MockitoMethodReference.on(type).to(methodProxy);
		}
	}
}
