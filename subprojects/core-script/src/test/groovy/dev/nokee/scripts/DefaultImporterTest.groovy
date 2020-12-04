package dev.nokee.scripts

import dev.nokee.internal.testing.utils.TestUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.junit.jupiter.api.Assertions.*

class DefaultImporterTest {
	def project = TestUtils.rootProject()

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
