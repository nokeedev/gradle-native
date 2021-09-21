/*
 * Copyright 2020 the original author or authors.
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
