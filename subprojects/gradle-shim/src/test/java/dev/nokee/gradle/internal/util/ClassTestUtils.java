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
package dev.nokee.gradle.internal.util;

import com.google.common.collect.ImmutableSet;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Stream;

public final class ClassTestUtils {
	// These interfaces are mix-in by the Gradle instantiator which we don't use for our shims
	private static final Set<String> IGNORED_INTERFACES = ImmutableSet.of("GeneratedSubclass", "DynamicObjectAware", "GroovyObject", "ModelObject", "OwnerAware");

	public static Stream<Class<?>> allInterfaces(Class<?> type) {
		Set<Class<?>> result = new HashSet<>();
		Deque<Class<?>> queue = new ArrayDeque<>();
		Set<Class<?>> seen = new HashSet<>();
		queue.add(type);
		while (!queue.isEmpty()) {
			Class<?> clazz = queue.pop();
			if (clazz.isInterface() && !clazz.getSimpleName().endsWith("MixIn") && !IGNORED_INTERFACES.contains(clazz.getSimpleName())) {
				result.add(clazz);
			}

			Arrays.stream(clazz.getInterfaces()).forEach(c -> {
				if (seen.add(c)) {
					queue.add(c);
				}
			});
			Optional.ofNullable(clazz.getSuperclass()).ifPresent(queue::add);
		}
		return result.stream().distinct();
	}

	public static Object createInstance(Class<?> type) {
		if (type.isInterface()) {
			if (type.equals(Provider.class)) {
				return Proxy.newProxyInstance(ClassTestUtils.class.getClassLoader(), new Class<?>[]{ProviderInternal.class}, AlwaysThrowingInvocationHandler.INSTANCE);
			} else {
				return Proxy.newProxyInstance(ClassTestUtils.class.getClassLoader(), new Class<?>[]{type}, AlwaysThrowingInvocationHandler.INSTANCE);
			}
		} else if (type.equals(String.class)) {
			return "42";
		} else if (type.equals(Object.class)) {
			return new Object();
		} else if (type.equals(Class.class)) {
			return Object.class;
		} else if (type.getSimpleName().equals("ExecutionTimeValue")) {
			try {
				return Class.forName("org.gradle.api.internal.provider.ValueSupplier$MissingExecutionTimeValue").newInstance();
			} catch (Throwable ex) {
				return null;
			}
		} else if (type.isPrimitive()) {
			if (type.equals(boolean.class)) {
				return Boolean.TRUE;
			} else if (type.equals(int.class)) {
				return 42;
			}
		}
		return null;
	}

	@Nullable
	public static Object[] createInstances(Class<?>[] types) {
		Object[] result = Arrays.stream(types).map(ClassTestUtils::createInstance).toArray();
		if (result.length == 0) {
			return null;
		}
		return result;
	}
}
