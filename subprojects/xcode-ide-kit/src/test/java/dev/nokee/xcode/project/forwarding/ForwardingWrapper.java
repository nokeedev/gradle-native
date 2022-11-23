/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.xcode.project.forwarding;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;

public final class ForwardingWrapper<T> {
	private final Class<T> interfaceType;
	private final Function<? super T, ? extends T> wrapperFactory;

	private ForwardingWrapper(Class<T> interfaceType, Function<? super T, ? extends T> wrapperFactory) {
		this.interfaceType = interfaceType;
		this.wrapperFactory = wrapperFactory;
	}

	public Method getMethod(String methodName) {
		return Arrays.stream(interfaceType.getMethods()).filter(it -> it.getName().equals(methodName)).findFirst()
			.orElseThrow(RuntimeException::new);
	}

	public T wrap(T proxy) {
		return wrapperFactory.apply(proxy);
	}

	public static <T> ForwardingWrapper<T> forwarding(Class<T> interfaceType, Function<? super T, ? extends T> wrapperFactory) {
		return new ForwardingWrapper<>(interfaceType, wrapperFactory);
	}

	public Class<T> getForwardType() {
		return interfaceType;
	}
}
