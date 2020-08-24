package dev.nokee.language.c.internal

import dev.nokee.gradle.fixtures.TestUtils
import dev.nokee.language.base.AbstractLanguageSourceSetTest
import dev.nokee.language.c.internal.CSourceSetImpl
import dev.nokee.model.internal.NamedDomainObjectIdentifierImpl
import org.gradle.api.Project
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.file.FileVisitor
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class CSourceSetImplTest extends AbstractLanguageSourceSetTest {
	@Rule TemporaryFolder temporaryFolder = new TemporaryFolder()
	def identifier = new NamedDomainObjectIdentifierImpl('foo')
	Project project
	CSourceSetImpl subject


	def setup() {
		project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
		subject = new CSourceSetImpl(identifier, project.objects)
	}

	def "can access files relative to base directory"() {
		given:
		temporaryFolder.newFolder('bar', 'a', 'b')
		temporaryFolder.newFile('bar/a/b/b1.txt')
		temporaryFolder.newFile('bar/a/b/b2.txt')
		temporaryFolder.newFile('bar/a/a1.txt')
		temporaryFolder.newFile('bar/foo.txt')

		when:
		subject.from(project.file('foo.txt'))
		subject.from(project.fileTree('bar'))

		then:
		def relativePaths = [] as Set
		subject.asFileTree.visit(new FileVisitor() {
			@Override
			void visitDir(FileVisitDetails details) {

			}

			@Override
			void visitFile(FileVisitDetails details) {
				relativePaths << details.relativePath.toString()
			}
		})
		relativePaths == ['foo.txt', 'a/b/b1.txt', 'a/b/b2.txt', 'a/a1.txt'] as Set
	}
}
