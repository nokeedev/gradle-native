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
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdhocNamedDomainObjectRegistryTest implements NamedDomainObjectRegistryTester<AdhocNamedDomainObjectRegistryTest.MyType> {
	private NamedDomainObjectRegistry<MyType> subject;
	private NamedDomainObjectProvider<MyType> e0;
	private NamedDomainObjectProvider<MyType> e1;
	private NamedDomainObjectProvider<MyType> e2;

	@BeforeEach
	void setup() {
		val container = objectFactory().domainObjectContainer(MyType.class, MyType::new);
		System.out.println(container);
		subject = NamedDomainObjectRegistry.of(container);
		e0 = container.register("a");
		e1 = container.register("b");
		e2 = container.register("c");
	}

	@Override
	public NamedDomainObjectRegistry<MyType> subject() {
		return subject;
	}

	@Override
	public NamedDomainObjectProvider<MyType> e0() {
		return e0;
	}

	@Override
	public NamedDomainObjectProvider<MyType> e1() {
		return e1;
	}

	@Override
	public NamedDomainObjectProvider<MyType> e2() {
		return e2;
	}

	@Test
	void checkToString() {
		assertThat(subject, hasToString("MyType container registry"));
	}

	@Nested
	class RegistrableTypesTest implements RegistrableTypeTester {
		@Override
		public NamedDomainObjectRegistry.RegistrableType subject() {
			return subject.getRegistrableType();
		}

		@Test
		void canRegisterContainerElementType() {
			assertTrue(subject().canRegisterType(MyType.class));
		}
	}

	@EqualsAndHashCode
	public static final class MyType implements Named {
		private final String name;

		public MyType(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}
}
