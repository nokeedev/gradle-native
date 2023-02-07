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
package dev.nokee.util.internal;

import dev.nokee.util.Unpacker;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;

public final class KotlinFunction0Unpacker implements Unpacker {
	private static final Class<?> KOTLIN_FUNCTION0_CLASS = loadKotlinFunction0Class();
	private final Unpacker delegate;

	public KotlinFunction0Unpacker(Unpacker delegate) {
		this.delegate = delegate;
	}

	@Nullable
	@Override
	public Object unpack(@Nullable Object target) {
		if (KOTLIN_FUNCTION0_CLASS != null && KOTLIN_FUNCTION0_CLASS.isInstance(target)) {
			try {
				return KOTLIN_FUNCTION0_CLASS.getMethod("invoke").invoke(target);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new RuntimeException(String.format("Could not access kotlin.jvm.functions.Function0#invoke() method on object of type '%s'.", target.getClass().getCanonicalName()), e);
			}
		} else {
			return delegate.unpack(target);
		}
	}

	@Override
	public boolean canUnpack(@Nullable Object target) {
		return (KOTLIN_FUNCTION0_CLASS != null && KOTLIN_FUNCTION0_CLASS.isInstance(target)) || delegate.canUnpack(target);
	}

	@Nullable
	private static Class<?> loadKotlinFunction0Class() {
		try {
			return Class.forName("kotlin.jvm.functions.Function0");
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}
