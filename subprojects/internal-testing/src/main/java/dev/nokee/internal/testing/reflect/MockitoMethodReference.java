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

import org.mockito.invocation.InvocationOnMock;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class MockitoMethodReference<T> implements MethodReference<T> {
	private final T proxy;

	public MockitoMethodReference(T proxy) {
		assert proxy != null;
		assert isMethodCaptureProxy(proxy);
		this.proxy = proxy;
	}

	@Override
	public Method to(Consumer<? super T> methodConsumer) {
		methodConsumer.accept(proxy);
		return ((MethodCapture) org.mockito.Mockito.mockingDetails(proxy).getMockCreationSettings().getDefaultAnswer()).getLastMethodInvocation();
	}

	public static <T> MockitoMethodReference<T> on(Class<T> target) {
		return new MockitoMethodReference<>(newProxy(target));
	}

	private static <T> T newProxy(Class<T> target) {
		return org.mockito.Mockito.mock(target, org.mockito.Mockito.withSettings().defaultAnswer(new MethodCapture()));
	}

	private static boolean isMethodCaptureProxy(Object proxy) {
		return org.mockito.Mockito.mockingDetails(proxy).getMockCreationSettings().getDefaultAnswer() instanceof MethodCapture;
	}

	private static final class MethodCapture implements org.mockito.stubbing.Answer<Object> {
		private Method method = null;

		@Override
		public Object answer(InvocationOnMock invocation) throws Throwable {
			this.method = invocation.getMethod();
			final Class<?> returnType = method.getReturnType();
			if (returnType.equals(boolean.class)) {
				return false;
			} else if (returnType.equals(int.class)) {
				return 0;
			} else if (returnType.equals(double.class)) {
				return 0;
			} else if (returnType.equals(short.class)) {
				return 0;
			} else if (returnType.equals(long.class)) {
				return 0L;
			} else if (returnType.equals(float.class)) {
				return 0.0f;
			} else {
				return null;
			}
		}

		public Method getLastMethodInvocation() {
			return method;
		}
	}
}
