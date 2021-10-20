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
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdhocPolymorphicDomainObjetRegistryTest implements PolymorphicDomainObjectRegistryTester<AdhocPolymorphicDomainObjetRegistryTest.MyType> {
	private PolymorphicDomainObjectRegistry<MyType> subject;
	private NamedDomainObjectProvider<MyType.A> e0;
	private NamedDomainObjectProvider<MyType.B> e1;
	private NamedDomainObjectProvider<MyType.C> e2;
	private NamedDomainObjectProvider<MyType.D> e3;

	@BeforeEach
	void setup() {
		val container = objectFactory().polymorphicDomainObjectContainer(MyType.class);
		container.registerFactory(MyType.A.class, MyType.A::new);
		container.registerFactory(MyType.B.class, MyType.B::new);
		container.registerFactory(MyType.C.class, MyType.C::new);
		container.registerFactory(MyType.D.class, MyType.D::new);

		subject =  PolymorphicDomainObjectRegistry.of(container);
		e0 = container.register("a", MyType.A.class);
		e1 = container.register("b", MyType.B.class);
		e2 = container.register("c", MyType.C.class);
		e3 = container.register("d", MyType.D.class);
	}

	@Override
	public PolymorphicDomainObjectRegistry<MyType> subject() {
		return subject;
	}

	@Override
	public NamedDomainObjectProvider<? extends MyType> e0() {
		return e0;
	}

	@Override
	public NamedDomainObjectProvider<? extends MyType> e1() {
		return e1;
	}

	@Override
	public NamedDomainObjectProvider<? extends MyType> e2() {
		return e2;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends MyType> NamedDomainObjectProvider<? extends S> e3() {
		return (NamedDomainObjectProvider<? extends S>) e3;
	}

	@Test
	void checkToString() {
		assertThat(subject, hasToString("MyType container registry"));
	}

	@Nested
	class RegistrableTypesTest implements RegistrableTypesTester {
		@Override
		public PolymorphicDomainObjectRegistry.RegistrableTypes subject() {
			return subject.getRegistrableTypes();
		}

		@Test
		void canRegisterAllTypeWithRegisteredFactory() {
			assertTrue(subject().canRegisterType(MyType.A.class));
			assertTrue(subject().canRegisterType(MyType.B.class));
			assertTrue(subject().canRegisterType(MyType.C.class));
			assertTrue(subject().canRegisterType(MyType.D.class));
		}

		@Test
		void cannotRegisterTypesWithoutRegisteredFactory() {
			assertFalse(subject().canRegisterType(MyType.class));
		}
	}

	@EqualsAndHashCode
	public static class MyType implements Named {
		private final String name;

		public MyType(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@EqualsAndHashCode(callSuper = true)
		public static final class A extends MyType {
			public A(String name) {
				super(name);
			}
		}

		@EqualsAndHashCode(callSuper = true)
		public static final class B extends MyType {
			public B(String name) {
				super(name);
			}
		}

		@EqualsAndHashCode(callSuper = true)
		public static final class C extends MyType {
			public C(String name) {
				super(name);
			}
		}

		@EqualsAndHashCode(callSuper = true)
		public static final class D extends MyType {
			public D(String name) {
				super(name);
			}
		}
	}
}
