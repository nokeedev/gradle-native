package dev.nokee.utils

import dev.nokee.utils.internal.AssertingTaskAction
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Supplier

@Subject(TaskActionUtils)
class TaskActionUtilsTest extends Specification {
	@Rule TemporaryFolder temporaryFolder = new TemporaryFolder()

	def "can create task action to delete directories"() {
		given:
		def a = temporaryFolder.newFolder('a')
		def b = temporaryFolder.newFolder('b')
		def c = temporaryFolder.newFolder('c')

		and:
		temporaryFolder.newFile('a/a')
		temporaryFolder.newFile('b/b')

		and:
		def project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
		def providerOfB = project.layout.projectDirectory.dir('b')

		when:
		TaskActionUtils.deleteDirectories(a, providerOfB).execute(Mock(Task))

		then:
		noExceptionThrown()

		and:
		!a.exists()
		!b.exists()
		c.exists()
	}

	def "can create asserting task action"() {
		given:
		def expression = Mock(Supplier)
		def errorMessage  = 'error!'

		when:
		def action = TaskActionUtils.assertTrue(expression, errorMessage)

		then:
		action instanceof AssertingTaskAction
	}
}
