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
package dev.nokee.model;

import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskContainerRegistryTest implements PolymorphicDomainObjectRegistryTester<Task> {
	private PolymorphicDomainObjectRegistry<Task> subject;
	private NamedDomainObjectProvider<MyTask.A> e0;
	private NamedDomainObjectProvider<MyTask.B> e1;
	private NamedDomainObjectProvider<MyTask.C> e2;
	private NamedDomainObjectProvider<MyTask.D> e3;

	@BeforeEach
	void setup() {
		val container = rootProject().getTasks();

		subject =  PolymorphicDomainObjectRegistry.of(container);
		e0 = container.register("a", MyTask.A.class);
		e1 = container.register("b", MyTask.B.class);
		e2 = container.register("c", MyTask.C.class);
		e3 = container.register("d", MyTask.D.class);
	}

	@Override
	public PolymorphicDomainObjectRegistry<Task> subject() {
		return subject;
	}

	@Override
	public NamedDomainObjectProvider<? extends Task> e0() {
		return e0;
	}

	@Override
	public NamedDomainObjectProvider<? extends Task> e1() {
		return e1;
	}

	@Override
	public NamedDomainObjectProvider<? extends Task> e2() {
		return e2;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends Task> NamedDomainObjectProvider<? extends S> e3() {
		return (NamedDomainObjectProvider<? extends S>) e3;
	}

	@Test
	void checkToString() {
		assertThat(subject, hasToString("task set registry"));
	}

	@Nested
	class RegistrableTypesTest implements RegistrableTypesTester {
		@Override
		public PolymorphicDomainObjectRegistry.RegistrableTypes subject() {
			return subject.getRegistrableTypes();
		}

		@Test
		void canRegisterAnyTaskType() {
			assertTrue(subject().canRegisterType(MyTask.class));
			assertTrue(subject().canRegisterType(MyTask.A.class));
			assertTrue(subject().canRegisterType(MyTask.B.class));
			assertTrue(subject().canRegisterType(MyTask.C.class));
			assertTrue(subject().canRegisterType(MyTask.D.class));
			assertTrue(subject().canRegisterType(MyOtherTask.class));
		}
	}

	@EqualsAndHashCode(callSuper = true)
	public static class MyTask extends DefaultTask {
		@EqualsAndHashCode(callSuper = true)
		public static /*final*/ class A extends MyTask {}

		@EqualsAndHashCode(callSuper = true)
		public static /*final*/ class B extends MyTask {}

		@EqualsAndHashCode(callSuper = true)
		public static /*final*/ class C extends MyTask {}

		@EqualsAndHashCode(callSuper = true)
		public static /*final*/ class D extends MyTask {}
	}

	public interface MyOtherTask extends Task {}
}
