/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.utils;

import groovy.lang.Closure;

final class ExecutionArgumentsFactory {
	public static <T> ExecutionArgument<T> create(Object thiz, T t) {
		return ExecutionArgument.of(thiz, t);
	}

	public static <T, U> ExecutionBiArguments<T, U> create(Object thiz, T t, U u) {
		return ExecutionBiArguments.of(thiz, t, u);
	}

	public static <T> ExecutionClosureArgument<T> create(Closure<?> thiz, T t) {
		return ExecutionClosureArgument.of(thiz, t);
	}

	public static <T, U> ExecutionClosureBiArguments<T, U> create(Closure<?> thiz, T t, U u) {
		return ExecutionClosureBiArguments.of(thiz, t, u);
	}
}
