package dev.nokee.language.base.internal

import dev.nokee.language.base.LanguageSourceSet
import org.gradle.api.Project
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.file.FileVisitor
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LanguageSourceSetImplTest extends Specification {
	@Rule
	TemporaryFolder temporaryFolder = new TemporaryFolder()
	Project project

	def setup() {
		project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
	}

	def "returns empty file tree on new source set"() {
		when:
		def subject = new LanguageSourceSetImpl(project.objects)

		then:
		subject.asFileTree.empty
	}

	def "returns empty source directories on new source set"() {
		when:
		def subject = new LanguageSourceSetImpl(project.objects)

		then:
		subject.sourceDirectories.empty
	}

	def "returns source set instance when from-ing"() {
		given:
		def subject = new LanguageSourceSetImpl(project.objects)

		and:
		def file = temporaryFolder.newFile('foo')

		when:
		def result = subject.from(file)

		then:
		result instanceof LanguageSourceSet
	}

	def "can add single files to source set"() {
		given:
		def subject = new LanguageSourceSetImpl(project.objects)

		and:
		def file = temporaryFolder.newFile('foo')

		when:
		subject.from(file)

		then:
		subject.asFileTree.files == [file] as Set
	}

	def "can add multiple files to source set"() {
		given:
		def subject = new LanguageSourceSetImpl(project.objects)

		and:
		def file1 = temporaryFolder.newFile('foo')
		def file2 = temporaryFolder.newFile('bar')

		when:
		subject.from(file1, file2)

		then:
		subject.asFileTree.files == [file1, file2] as Set
	}

	def "infers source directory of single files"() {
		given:
		def subject = new LanguageSourceSetImpl(project.objects)

		and:
		def file1 = temporaryFolder.newFile('foo')
		def file2 = temporaryFolder.newFile('bar')

		when:
		subject.from(file1, file2)

		then:
		!subject.sourceDirectories.empty
		subject.sourceDirectories.files == [temporaryFolder.root] as Set
	}

	def "can add directory with files to source set"() {
		given:
		def subject = new LanguageSourceSetImpl(project.objects)

		and:
		def file1 = temporaryFolder.newFile('foo')
		def file2 = temporaryFolder.newFile('bar')

		when:
		subject.from(temporaryFolder.root)

		then:
		subject.asFileTree.files == [file1, file2] as Set
	}

	def "can add file collection to source set"() {
		given:
		def subject = new LanguageSourceSetImpl(project.objects)

		and:
		def file1 = temporaryFolder.newFile('foo')
		def file2 = temporaryFolder.newFile('bar')

		and:
		def files = project.files(file1, file2)

		when:
		subject.from(files)

		then:
		subject.asFileTree.files == [file1, file2] as Set
	}

	def "includes in source directories added directories as-is"() {
		given:
		def subject = new LanguageSourceSetImpl(project.objects)

		and:
		temporaryFolder.newFile('foo')
		temporaryFolder.newFile('bar')

		when:
		subject.from(temporaryFolder.root)

		then:
		subject.sourceDirectories.files == [temporaryFolder.root] as Set
	}

	def "conserves build dependencies on source directories"() {
		given:
		def subject = new LanguageSourceSetImpl(project.objects)

		and:
		def buildTask = project.tasks.create('build')
		def files = project.files(project.file('foo')) { it.builtBy buildTask }

		and:
		subject.from(files)

		when:
		def sourceDirectories = subject.sourceDirectories

		then:
		sourceDirectories.buildDependencies.getDependencies(null) == [buildTask] as Set
	}

//	def "can access relative to base directory path"() {
//		given:
//		def subject = new LanguageSourceSetImpl(project.objects)
//
//		and:
//		def dir1 = temporaryFolder.newFolder('dir1')
//		def dir1file1 = temporaryFolder.newFile('dir1/foo')
//		def dir1file2 = temporaryFolder.newFile('dir1/bar')
//		def dir2 = temporaryFolder.newFolder('dir2')
//		def dir2file1 = temporaryFolder.newFile('dir2/foo')
//		def dir2file2 = temporaryFolder.newFile('dir2/bar')
//
//		when:
//		subject.from(temporaryFolder.root)
//
//		then:
//		relativePaths(subject) == ['dir1/foo', 'dir1/bar', 'dir2/foo', 'dir2/bar'] as Set
//	}

	private Set<String> relativePaths(LanguageSourceSet sourceSet) {
		def relativePaths = [] as Set
		sourceSet.asFileTree.visit(new FileVisitor() {
			@Override
			void visitDir(FileVisitDetails details) {

			}

			@Override
			void visitFile(FileVisitDetails details) {
				relativePaths << details.relativePath.toString()
			}
		})
		return relativePaths
	}
}
