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

import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.testfixtures.ProjectBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.stream.Stream;

public final class ProviderTestUtils {
	private ProviderTestUtils() {}

	public static Stream<Class<?>> allTaskProviderInterfaces() {
		TaskContainer container = ProjectBuilder.builder().build().getTasks();
		container.create("existing");

		return Stream.concat(ClassTestUtils.allInterfaces(container.register("creating").getClass()), ClassTestUtils.allInterfaces(container.named("existing").getClass())).distinct();
	}

	public static <T extends Task> TaskProvider<T> newTaskProviderProxy() {
		return newTaskProviderProxy(AlwaysThrowingInvocationHandler.alwaysThrowingHandler());
	}

	@SuppressWarnings("unchecked")
	public static <T extends Task> TaskProvider<T> newTaskProviderProxy(InvocationHandler handler) {
		return (TaskProvider<T>) Proxy.newProxyInstance(ProviderTestUtils.class.getClassLoader(), allTaskProviderInterfaces().toArray(Class[]::new), handler);
	}

	public static Stream<Class<?>> allNamedDomainObjectProviderInterfaces() {
		ObjectFactory objectFactory = ProjectBuilder.builder().build().getObjects();
		NamedDomainObjectContainer<MyType> container = objectFactory.domainObjectContainer(MyType.class, MyType::new);
		container.create("existing");

		return Stream.concat(ClassTestUtils.allInterfaces(container.register("creating").getClass()), ClassTestUtils.allInterfaces(container.named("existing").getClass())).distinct();
	}

	public static <T> NamedDomainObjectProvider<T> newNamedDomainObjectProviderProxy() {
		return newNamedDomainObjectProviderProxy(AlwaysThrowingInvocationHandler.alwaysThrowingHandler());
	}

	@SuppressWarnings("unchecked")
	public static <T> NamedDomainObjectProvider<T> newNamedDomainObjectProviderProxy(InvocationHandler handler) {
		return (NamedDomainObjectProvider<T>) Proxy.newProxyInstance(ProviderTestUtils.class.getClassLoader(), allNamedDomainObjectProviderInterfaces().toArray(Class[]::new), handler);
	}

	private static final class MyType implements Named {
		private final String name;

		private MyType(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}
}
