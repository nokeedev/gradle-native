/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.scripts

import dev.gradleplugins.grava.testing.util.ProjectTestUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import spock.lang.Subject

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.junit.jupiter.api.Assertions.*

@Subject(DefaultImporter.class)
class DefaultImporterTest {
	def project = ProjectTestUtils.rootProject()

	@BeforeEach
	void "import some types"() {
		project.extensions.add(TypeCollidingWithExtension.simpleName, project.objects.newInstance(TypeCollidingWithExtension))
		DefaultImporter.forProject(project)
			.defaultImport(DefaultImportedType0)
			.defaultImport(DefaultImportedType1)
			.defaultImport(DefaultImportedType2)
	}

	@Test
	void "can access default import"() {
		assertAll({
			assertThat(project.DefaultImportedType0, equalTo(DefaultImportedType0))
			assertThat(project.DefaultImportedType1, equalTo(DefaultImportedType1))
			assertThat(project.DefaultImportedType2, equalTo(DefaultImportedType2))
		} as Executable)
	}

	@Test
	void "cannot access non imported type"() {
		def ex = assertThrows(MissingPropertyException, { project.NonImportedType })
		assertEquals("Could not get unknown property 'NonImportedType' for root project 'test' of type org.gradle.api.Project.", ex.message)
	}

	@Test
	void "do nothing when duplicate default import"() {
		assertDoesNotThrow({ DefaultImporter.forProject(project).defaultImport(DefaultImportedType0) } as Executable)
	}

	@Test
	void "throws exception when default import collide with an extension"() {
		def ex = assertThrows(IllegalArgumentException, { DefaultImporter.forProject(project).defaultImport(TypeCollidingWithExtension) })
		assertEquals("Could not default import type '${TypeCollidingWithExtension.canonicalName}'.".toString(), ex.message)
	}

	interface DefaultImportedType0 {}
	interface DefaultImportedType1 {}
	interface DefaultImportedType2 {}
	interface NonImportedType {}
	interface TypeCollidingWithExtension {}
}
