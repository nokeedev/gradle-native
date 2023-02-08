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
import java.util.function.Consumer;

final class ByProxyMethodInformation<ReceiverType, ReturnInfoType extends ReturnInformation, ArgumentInfoType extends ArgumentInformation> implements MethodInformation.WithArguments<ReceiverType, ReturnInfoType, ArgumentInfoType> {
	private final Consumer<ReceiverType> methodProxy;

	public ByProxyMethodInformation(Consumer<ReceiverType> methodProxy) {
		this.methodProxy = methodProxy;
	}

	@Override
	public Method resolve(Class<ReceiverType> type) {
		try {
			return ReflectionProxyMethodReference.on(type).to(methodProxy);
		} catch (Throwable e) {
			return MockitoMethodReference.on(type).to(methodProxy);
		}
	}
}
