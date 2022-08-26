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

import dev.nokee.gradle.NamedDomainObjectProviderFactory;
import org.gradle.api.NamedDomainObjectProvider;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static dev.nokee.gradle.NamedDomainObjectProviderSpec.of;
import static dev.nokee.gradle.internal.util.ClassTestUtils.allInterfaces;
import static dev.nokee.gradle.internal.util.ProviderTestUtils.allNamedDomainObjectProviderInterfaces;
import static dev.nokee.gradle.internal.util.ProviderTestUtils.newNamedDomainObjectProviderProxy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NamedDomainObjectProviderInterfaceStructureTest {
	private static final NamedDomainObjectProvider<?> subject = new NamedDomainObjectProviderFactory().create(of(newNamedDomainObjectProviderProxy()));

	@Test
	void inheritsSameInterfaceStructure() {
		assertEquals(allNamedDomainObjectProviderInterfaces().collect(Collectors.toSet()),
			allInterfaces(subject.getClass()).collect(Collectors.toSet()));
	}
}
