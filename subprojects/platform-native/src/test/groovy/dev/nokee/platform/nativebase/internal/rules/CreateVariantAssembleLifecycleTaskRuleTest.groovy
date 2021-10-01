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
package dev.nokee.platform.nativebase.internal.rules

import dev.nokee.internal.testing.util.ProjectTestUtils
import org.gradle.api.Project
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.nokee.utils.TaskUtils.configureGroup

@Subject(CreateVariantAssembleLifecycleTaskRule)
class CreateVariantAssembleLifecycleTaskRuleTest extends Specification implements TaskEntityFixture, VariantEntityFixture {
	Project project = ProjectTestUtils.rootProject()
	def subject = new CreateVariantAssembleLifecycleTaskRule(taskRegistry)

	@Unroll
	def "creates the assemble task if absent"(variant) {
		given:
		discovered(variant)

		when:
		subject.execute(known(variant))

		then:
		def assembleTask = taskRepository.get(aTaskOfVariant('assemble', variant))
		assembleTask.group == 'build'

		where:
		variant << [aVariantOfMainComponent('macos', 'debug'), onlyVariantOfMainComponent()]
	}

	@Unroll
	def "does not configure the assemble task group if already present"(variant) {
		given:
		discovered(variant)
		def assembleTask = taskRegistry.register(aTaskOfVariant('assemble', variant), configureGroup('some group')).get()

		when:
		subject.execute(known(variant))

		then:
		assembleTask.group == 'some group'

		where:
		variant << [aVariantOfMainComponent('macos', 'debug'), onlyVariantOfMainComponent()]
	}

	def "configures assemble task with a dependency on the development binary for a known variant of a multi-variant component"() {
		def variant = discovered(aVariantOfMainComponent('macos', 'debug'))

		when:
		subject.execute(known(variant))

		then:
		def assembleTask = taskRepository.get(aTaskOfVariant('assemble', variant))
		assembleTask.dependsOn.size() == 1 // We assume the dependency is to the development binary
	}

	def "does not configure assemble task dependency for a known variant of a single-variant component"() {
		def variant = discovered(onlyVariantOfMainComponent())

		when:
		subject.execute(known(variant))

		then:
		def assembleTask = taskRepository.get(aTaskOfVariant('assemble', variant))
		assembleTask.dependsOn.empty
	}
}
