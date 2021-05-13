package dev.nokee.utils

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(ProjectUtils)
class ProjectUtilsTest extends Specification {
	def "can detect root project"() {
		given:
		def root = ProjectBuilder.builder().withName('root').build()
		def child = ProjectBuilder.builder().withName('child').withParent(root).build()

		expect:
		ProjectUtils.isRootProject(root)
		!ProjectUtils.isRootProject(child)
	}

	def "returns a prefixable project path"() {
		given:
		def root = ProjectBuilder.builder().withName('root').build()
		def child = ProjectBuilder.builder().withName('child').withParent(root).build()

		expect:
		ProjectUtils.getPrefixableProjectPath(root) == ''
		ProjectUtils.getPrefixableProjectPath(child) == ':child'
	}
}
