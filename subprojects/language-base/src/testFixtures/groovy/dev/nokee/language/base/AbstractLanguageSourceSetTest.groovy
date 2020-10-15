package dev.nokee.language.base

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier
import dev.nokee.language.base.internal.LanguageSourceSetName
import dev.nokee.model.internal.DomainObjectIdentifierInternal
import org.apache.commons.lang3.RandomStringUtils
import org.gradle.api.Action
import org.gradle.api.Buildable
import org.gradle.api.Project
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.file.FileVisitor
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

abstract class AbstractLanguageSourceSetTest<T extends LanguageSourceSet> extends Specification {
	@Rule
	TemporaryFolder temporaryFolder = new TemporaryFolder()
	Project project

	def setup() {
		project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
	}

	protected LanguageSourceSetIdentifier<T> newIdentifier() {
		return LanguageSourceSetIdentifier.of(LanguageSourceSetName.of(newLowerCamelRandomString()), publicType, Stub(DomainObjectIdentifierInternal))
	}

	protected static String newLowerCamelRandomString() {
		return 'a' + RandomStringUtils.randomAlphanumeric(12)
	}

	protected abstract T newSubject()

	protected abstract Class<T> getPublicType()

	protected abstract Set<String> getDefaultFilterIncludes()

	protected abstract String fileName(String fileName)

	//region initialization
	def "returns empty file tree on new source set"() {
		when:
		def subject = newSubject()

		then:
		subject.asFileTree.empty
	}

	def "returns empty source directories on new source set"() {
		when:
		def subject = newSubject()

		then:
		subject.sourceDirectories.empty
	}

	def "has language-specific filter include pattern"() {
		when:
		def subject = newSubject()

		then:
		subject.filter.includes == defaultFilterIncludes
	}

	def "has no filter exclude pattern"() {
		when:
		def subject = newSubject()

		then:
		subject.filter.excludes == Collections.emptySet()
	}
	//endregion

	//region filter
	def "can configure filter pattern"() {
		given:
		def subject = newSubject()

		and:
		def action = Mock(Action)

		when:
		subject.filter(action)

		then:
		1 * action.execute(subject.filter)
	}

	def "returns source set instance when filter-ing"() {
		given:
		def subject = newSubject()

		when:
		def result = subject.filter(Stub(Action))

		then:
		result == subject
	}

	def "previous file tree honors changes to filter"() {
		given:
		def subject = newSubject()

		and:
		def file1 = temporaryFolder.newFile(fileName('foo'))
		def file2 = temporaryFolder.newFile(fileName('bar'))

		and:
		subject.from(file1, file2)
		def files = subject.asFileTree
		assert files.files == [file1, file2] as Set

		when:
		subject.filter {
			it.exclude(fileName('bar'))
		}

		then:
		files.files == [file1] as Set
	}

	def "new file tree honors current filter"() {
		given:
		def subject = newSubject()

		and:
		def file1 = temporaryFolder.newFile(fileName('foo'))
		def file2 = temporaryFolder.newFile(fileName('bar'))

		and:
		subject.from(file1, file2)

		when:
		subject.filter {
			it.exclude(fileName('bar'))
		}

		then:
		subject.asFileTree.files == [file1] as Set
	}

	def "filters does not affect per-file source directories"() {
		given:
		def subject = newSubject()

		and:
		def file1 = temporaryFolder.newFile(fileName('foo'))
		def nestedDirectory = temporaryFolder.newFolder('nested')
		def file2 = temporaryFolder.newFile(fileName('nested/bar'))

		and:
		subject.from(file1, file2)

		when:
		subject.filter {
			it.exclude('bar')
		}

		then:
		subject.sourceDirectories.files == [temporaryFolder.root, nestedDirectory] as Set
	}

	def "filters does not affect source directories"() {
		given:
		def subject = newSubject()

		and:
		def file1 = temporaryFolder.newFile(fileName('foo'))
		def nestedDirectory = temporaryFolder.newFolder('nested')
		def file2 = temporaryFolder.newFile(fileName('nested/bar'))

		and:
		subject.from(temporaryFolder.root, nestedDirectory)

		when:
		subject.filter {
			it.exclude(fileName('bar'))
		}

		then:
		subject.sourceDirectories.files == [temporaryFolder.root, nestedDirectory] as Set
	}
	//endregion

	//region from(file)
	def "returns source set instance when from-ing"() {
		given:
		def subject = newSubject()

		and:
		def file = temporaryFolder.newFile(fileName('foo'))

		when:
		def result = subject.from(file)

		then:
		result == subject
	}

	def "can add single files to source set"() {
		given:
		def subject = newSubject()

		and:
		def file = temporaryFolder.newFile(fileName('foo'))

		when:
		subject.from(file)

		then:
		subject.asFileTree.files == [file] as Set
	}

	def "can add multiple files to source set"() {
		given:
		def subject = newSubject()

		and:
		def file1 = temporaryFolder.newFile(fileName('foo'))
		def file2 = temporaryFolder.newFile(fileName('bar'))

		when:
		subject.from(file1, file2)

		then:
		subject.asFileTree.files == [file1, file2] as Set
	}

	def "infers source directory of single files"() {
		given:
		def subject = newSubject()

		and:
		def file1 = temporaryFolder.newFile(fileName('foo'))
		def file2 = temporaryFolder.newFile(fileName('bar'))

		when:
		subject.from(file1, file2)

		then:
		!subject.sourceDirectories.empty
		subject.sourceDirectories.files == [temporaryFolder.root] as Set
	}

	def "infers nested source directory of single files"() {
		given:
		def subject = newSubject()

		and:
		def file1 = temporaryFolder.newFile(fileName('foo'))
		def nestedDirectory = temporaryFolder.newFolder('nested')
		def file2 = temporaryFolder.newFile(fileName('nested/bar'))

		when:
		subject.from(file1, file2)

		then:
		!subject.sourceDirectories.empty
		subject.sourceDirectories.files == [temporaryFolder.root, nestedDirectory] as Set
	}

	def "infers source directory of single files from distinct root directory"() {
		given:
		def subject = newSubject()

		and:
		def rootDirectory1 = temporaryFolder.newFolder('a')
		def file1 = temporaryFolder.newFile(fileName('a/foo'))
		def rootDirectory2 = temporaryFolder.newFolder('b')
		def file2 = temporaryFolder.newFile(fileName('b/bar'))

		when:
		subject.from(file1, file2)

		then:
		subject.asFileTree.files == [file1, file2] as Set
		subject.sourceDirectories.files == [rootDirectory1, rootDirectory2] as Set
	}

	def "honors changes to source set from source set file tree"() {
		given:
		def subject = newSubject()

		and:
		def file1 = temporaryFolder.newFile(fileName('foo'))
		def file2 = temporaryFolder.newFile(fileName('bar'))

		and:
		subject.from(file1)
		def files = subject.asFileTree
		assert files.files == [file1] as Set

		when:
		subject.from(file2)

		then:
		files.files == [file1, file2] as Set
	}
	//endregion

	//region from(directory)
	def "can add directory with files to source set"() {
		given:
		def subject = newSubject()

		and:
		def file1 = temporaryFolder.newFile(fileName('foo'))
		def file2 = temporaryFolder.newFile(fileName('bar'))

		when:
		subject.from(temporaryFolder.root)

		then:
		subject.asFileTree.files == [file1, file2] as Set
		subject.sourceDirectories.files == [temporaryFolder.root] as Set
	}

	def "can add nested directory with files to source set"() {
		given:
		def subject = newSubject()

		and:
		def file1 = temporaryFolder.newFile(fileName('foo'))
		def nestedDirectory = temporaryFolder.newFolder('nested')
		def file2 = temporaryFolder.newFile(fileName('nested/bar'))

		when:
		subject.from(temporaryFolder.root, nestedDirectory)

		then:
		subject.asFileTree.files == [file1, file2] as Set
		subject.sourceDirectories.files == [temporaryFolder.root, nestedDirectory] as Set
	}

	def "can add distinct root directory with files to source set"() {
		given:
		def subject = newSubject()

		and:
		def rootDirectory1 = temporaryFolder.newFolder('a')
		def file1 = temporaryFolder.newFile(fileName('a/foo'))
		def rootDirectory2 = temporaryFolder.newFolder('b')
		def file2 = temporaryFolder.newFile(fileName('b/bar'))

		when:
		subject.from(rootDirectory1, rootDirectory2)

		then:
		subject.asFileTree.files == [file1, file2] as Set
		subject.sourceDirectories.files == [rootDirectory1, rootDirectory2] as Set
	}
	//endregion

	//region from(file collection)
	def "can add file collection to source set"() {
		given:
		def subject = newSubject()

		and:
		def file1 = temporaryFolder.newFile(fileName('foo'))
		def file2 = temporaryFolder.newFile(fileName('bar'))

		and:
		def files = project.files(file1, file2)

		when:
		subject.from(files)

		then:
		subject.asFileTree.files == [file1, file2] as Set
		subject.sourceDirectories.files == [temporaryFolder.root] as Set
	}
	//endregion

	//region from(file tree)
	def "can add file tree to source set"() {
		given:
		def subject = newSubject()

		and:
		def rootDirectory = temporaryFolder.newFolder('root')
		def file1 = temporaryFolder.newFile(fileName('root/foo'))
		def file2 = temporaryFolder.newFile(fileName('root/bar'))

		and:
		def files = project.fileTree(dir: rootDirectory)

		when:
		subject.from(files)

		then:
		subject.asFileTree.files == [file1, file2] as Set
		subject.sourceDirectories.files == [rootDirectory] as Set
	}

	def "honors filters already applied to the file tree"() {
		given:
		def subject = newSubject()

		and:
		def file1 = temporaryFolder.newFile(fileName('foo'))
		def file2 = temporaryFolder.newFile(fileName('bar'))

		and:
		def files = project.fileTree(dir: temporaryFolder.root, includes: [ fileName('foo') ])

		when:
		subject.from(files)

		then:
		subject.asFileTree.files == [file1] as Set
		subject.sourceDirectories.files == [temporaryFolder.root] as Set
	}
	//endregion

	//region build dependencies
	def "conserves build dependencies on source directories"() {
		given:
		def subject = newSubject()

		and:
		def buildTask = project.tasks.create('build')
		def files = project.files(project.file(fileName('foo'))) { it.builtBy buildTask }

		and:
		subject.from(files)

		when:
		def sourceDirectories = subject.sourceDirectories

		then:
		sourceDirectories.buildDependencies.getDependencies(null) == [buildTask] as Set
	}

	def "conserves build dependencies on file tree"() {
		given:
		def subject = newSubject()

		and:
		def buildTask = project.tasks.create('build')
		def files = project.files(project.file(fileName('foo'))) { it.builtBy buildTask }

		and:
		subject.from(files)

		when:
		def fileTree = subject.asFileTree

		then:
		fileTree.buildDependencies.getDependencies(null) == [buildTask] as Set
	}

	def "is buildable"() {
		expect:
		newSubject() instanceof Buildable
	}

	def "has build dependencies from sources"() {
		given:
		def subject = newSubject()

		and:
		def buildTask = project.tasks.create('build')
		def files = project.files(project.file(fileName('foo'))) { it.builtBy buildTask }

		and:
		subject.from(files)

		expect:
		subject.buildDependencies.getDependencies(null) == [buildTask] as Set
	}
	//endregion

	//region common usage
	def "can access relative to base directory path"() {
		given:
		def subject = newSubject()

		and:
		def dir1 = temporaryFolder.newFolder('dir1')
		def dir1file1 = temporaryFolder.newFile(fileName('dir1/foo'))
		def dir1file2 = temporaryFolder.newFile(fileName('dir1/bar'))
		def dir2 = temporaryFolder.newFolder('dir2')
		def dir2file1 = temporaryFolder.newFile(fileName('dir2/foo'))
		def dir2file2 = temporaryFolder.newFile(fileName('dir2/bar'))

		when:
		subject.from(temporaryFolder.root)

		then:
		relativePaths(subject) == [fileName('dir1/foo'), fileName('dir1/bar'), fileName('dir2/foo'), fileName('dir2/bar')] as Set
	}

	def "can access relative to respective base directory path"() {
		given:
		def subject = newSubject()

		and:
		def dir1 = temporaryFolder.newFolder('dir1')
		def dir1dir = temporaryFolder.newFolder('dir1', 'nestedDir')
		def dir1file1 = temporaryFolder.newFile(fileName('dir1/file1-1'))
		def dir1file2 = temporaryFolder.newFile(fileName('dir1/nestedDir/file1-2'))
		def dir2 = temporaryFolder.newFolder('dir2')
		def dir2dir = temporaryFolder.newFolder('dir2', 'nestedDir')
		def dir2file1 = temporaryFolder.newFile(fileName('dir2/file2-1'))
		def dir2file2 = temporaryFolder.newFile(fileName('dir2/nestedDir/file2-2'))

		when:
		subject.from(dir1, dir2)

		then:
		relativePaths(subject) == [fileName('file1-1'), fileName('nestedDir/file1-2'), fileName('file2-1'), fileName('nestedDir/file2-2')] as Set
	}

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
	//endregion
}
