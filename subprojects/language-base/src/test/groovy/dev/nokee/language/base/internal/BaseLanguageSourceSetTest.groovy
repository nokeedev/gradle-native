//package dev.nokee.language.base.internal
//
//import dev.nokee.platform.base.internal.DomainObjectIdentity
//import dev.nokee.platform.base.internal.GlobalDomainObjectIdentity
//import org.gradle.api.Project
//import org.gradle.api.file.FileVisitDetails
//import org.gradle.api.file.FileVisitor
//import org.gradle.testfixtures.ProjectBuilder
//import org.junit.Rule
//import org.junit.rules.TemporaryFolder
//import spock.lang.Specification
//import spock.lang.Subject
//
//@Subject(BaseLanguageSourceSet)
//class BaseLanguageSourceSetTest extends Specification {
//	@Rule TemporaryFolder temporaryFolder = new TemporaryFolder()
//	Project project
//
//	def setup() {
//		project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
//	}
//
//	def "can access files relative to base directory"() {
//		given:
//		def subject = BaseLanguageSourceSet.create(LanguageSourceSet, BaseLanguageSourceSet, DomainObjectIdentity.named('test'), project.objects)
//		temporaryFolder.newFolder('bar', 'a', 'b')
//		temporaryFolder.newFile('bar/a/b/b1.txt')
//		temporaryFolder.newFile('bar/a/b/b2.txt')
//		temporaryFolder.newFile('bar/a/a1.txt')
//		temporaryFolder.newFile('bar/foo.txt')
//
//		when:
//		subject.from(project.file('foo.txt'))
//		subject.from(project.fileTree('bar'))
//
//		then:
//		def relativePaths = [] as Set
//		subject.asFileTree.visit(new FileVisitor() {
//			@Override
//			void visitDir(FileVisitDetails details) {
//
//			}
//
//			@Override
//			void visitFile(FileVisitDetails details) {
//				relativePaths << details.relativePath.toString()
//			}
//		})
//		relativePaths == ['foo.txt', 'a/b/b1.txt', 'a/b/b2.txt', 'a/a1.txt'] as Set
//	}
//
//	def "can access source directories"() {
//		given:
//		def subject = BaseLanguageSourceSet.create(LanguageSourceSet, BaseLanguageSourceSet, DomainObjectIdentity.named('test'), project.objects)
//		temporaryFolder.newFolder('bar', 'a', 'b')
//		temporaryFolder.newFile('bar/a/b/b1.txt')
//		temporaryFolder.newFile('bar/a/b/b2.txt')
//		temporaryFolder.newFile('bar/a/a1.txt')
//		temporaryFolder.newFile('bar/foo.txt')
//
//		when:
//		subject.from('foo.txt')
//		subject.from('bar')
//
//		then:
//		subject.getSourceDirectories().files == [temporaryFolder.root.canonicalFile, new File(temporaryFolder.root, 'bar').canonicalFile] as Set
//	}
//
//	def "has useful display names"() {
//		def identifier = new GlobalDomainObjectIdentity("project", "parent").child("cpp").child("test")
//		def sourceSet = BaseLanguageSourceSet.create(TestSourceSet, BaseLanguageSourceSet, identifier, project.objects)
//
//		expect:
//		sourceSet.displayName == "Test source 'parent:cpp:test'"
//		sourceSet.toString() == sourceSet.displayName
//	}
//
//	def "calculates display name from public type name"() {
//		expect:
//		def sourceSet = BaseLanguageSourceSet.create(publicType, BaseLanguageSourceSet, DomainObjectIdentity.named("test"), project.objects)
//		sourceSet.displayName == displayName
//
//		where:
//		publicType                | displayName
//		SomeTypeLanguageSourceSet | "SomeType source 'test'"
//		SomeTypeSourceSet         | "SomeType source 'test'"
//		SomeTypeSource            | "SomeType source 'test'"
//		SomeTypeSet               | "SomeType source 'test'"
//		SomeType                  | "SomeType source 'test'"
//		SomeResourcesSet          | "SomeResources 'test'"
//	}
//
//	interface TestSourceSet extends LanguageSourceSet {}
//
//	interface SomeTypeLanguageSourceSet extends LanguageSourceSet {}
//
//	interface SomeTypeSourceSet extends LanguageSourceSet {}
//
//	interface SomeTypeSet extends LanguageSourceSet {}
//
//	interface SomeTypeSource extends LanguageSourceSet {}
//
//	interface SomeType extends LanguageSourceSet {}
//
//	interface SomeResourcesSet extends LanguageSourceSet {}
//
//	// TODO: Test dependency information from #getSourceDirectories()
//	// TODO: Test dependency information from #getAsFileTree()
//}
