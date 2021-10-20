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

import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationContainerRegistryTest implements NamedDomainObjectRegistryTester<Configuration> {
	private NamedDomainObjectRegistry<Configuration> subject;
	private NamedDomainObjectProvider<Configuration> e0;
	private NamedDomainObjectProvider<Configuration> e1;
	private NamedDomainObjectProvider<Configuration> e2;

	@BeforeEach
	void setup() {
		val container = rootProject().getConfigurations();
		System.out.println(container);
		subject = NamedDomainObjectRegistry.of(container);
		e0 = container.register("a");
		e1 = container.register("b");
		e2 = container.register("c");
	}

	@Override
	public NamedDomainObjectRegistry<Configuration> subject() {
		return subject;
	}

	@Override
	public NamedDomainObjectProvider<Configuration> e0() {
		return e0;
	}

	@Override
	public NamedDomainObjectProvider<Configuration> e1() {
		return e1;
	}

	@Override
	public NamedDomainObjectProvider<Configuration> e2() {
		return e2;
	}

	@Test
	void checkToString() {
		assertThat(subject, hasToString("configuration container registry"));
	}

	@Nested
	class RegistrableTypesTest implements RegistrableTypeTester {
		@Override
		public NamedDomainObjectRegistry.RegistrableType subject() {
			return subject.getRegistrableType();
		}

		@Test
		void canRegisterContainerElementType() {
			assertTrue(subject().canRegisterType(Configuration.class));
		}
	}
}
