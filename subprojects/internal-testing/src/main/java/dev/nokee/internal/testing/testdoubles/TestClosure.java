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
package dev.nokee.internal.testing.testdoubles;

import groovy.lang.Closure;

public abstract class TestClosure<R, T> extends Closure<R> {
	public TestClosure() {
		super(new Object());
	}

	public TestClosure(Object owner, Object thisObject) {
		super(owner, thisObject);
	}

	protected R doCall(T argument) {
		return execute(getDelegate(), getResolveStrategy(), new Object[] { argument });
	}

	public abstract R execute(Object delegate, int strategy, Object[] arguments);
}
