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
package dev.nokee.gradle.internal.testers;

import com.google.common.collect.ImmutableList;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class CapturingInvocationHandler implements InvocationHandler {
	private final List<CapturedInvocation> invocations = new ArrayList<>();
	private Object returnValue;

	public void returnWhenInvoked(Object returnValue) {
		this.returnValue = returnValue;
	}

	public void resetCapture() {
		invocations.clear();
	}

	public List<CapturedInvocation> getCapturedInvocations() {
		return ImmutableList.copyOf(invocations);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		invocations.add(new CapturedInvocation(method, args));
		return returnValue;
	}
}
