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
import java.util.Arrays;

final class ByNameMethodInformation<ReceiverType, ReturnInfoType extends ReturnInformation> implements MethodInformation<ReceiverType, ReturnInfoType> {
	private final String methodName;

	ByNameMethodInformation(String methodName) {
		this.methodName = methodName;
	}

	@Override
	public Method resolve(Class<ReceiverType> type) {
		return Arrays.stream(type.getMethods()).filter(it -> it.getName().equals(methodName)).findFirst()
			.orElseThrow(RuntimeException::new);
	}

	@Override
	public String toString() {
		return methodName + "()";
	}
}
