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
package dev.nokee.gradle.internal;

import dev.nokee.gradle.TaskProviderFactory;
import dev.nokee.gradle.internal.testers.MethodCallForwardingTester;
import org.gradle.api.tasks.TaskProvider;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import static dev.nokee.gradle.NamedDomainObjectProviderSpec.of;
import static dev.nokee.gradle.internal.util.ProviderTestUtils.allTaskProviderInterfaces;
import static dev.nokee.gradle.internal.util.ProviderTestUtils.newTaskProviderProxy;

class TaskProviderDelegateTest extends MethodCallForwardingTester {
	private static final TaskProvider<?> subject = new TaskProviderFactory().create(of(newTaskProviderProxy(handler)));

	@Override
	public Object subject() {
		return subject;
	}

	@Override
	public Stream<Method> allMethodsUnderTest() {
		return allTaskProviderInterfaces().flatMap(it -> Arrays.stream(it.getMethods()));
	}
}
