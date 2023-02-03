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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

final class ReflectionProxyMethodReference<T> implements MethodReference<T> {
	private final T proxy;

	public ReflectionProxyMethodReference(T proxy) {
		assert isMethodCaptureProxy(proxy);
		this.proxy = proxy;
	}

	@Override
	public Method to(Consumer<? super T> methodConsumer) {
		methodConsumer.accept(proxy);
		return ((MethodCapture) Proxy.getInvocationHandler(proxy)).getLastMethodInvocation();
	}

	public static <T> ReflectionProxyMethodReference<T> on(Class<T> target) {
		return new ReflectionProxyMethodReference<>(newProxy(target));
	}

	private static <T> T newProxy(Class<T> target) {
		@SuppressWarnings("unchecked")
		final T result = (T) Proxy.newProxyInstance(target.getClassLoader(), new Class<?>[]{ target }, new MethodCapture());
		return result;
	}

	private static boolean isMethodCaptureProxy(Object proxy) {
		return Proxy.getInvocationHandler(proxy) instanceof MethodCapture;
	}

	private static final class MethodCapture implements InvocationHandler {
		private Method method = null;

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			this.method = method;
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
