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
package dev.nokee.platform.base.internal.tasks

import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import dev.nokee.platform.base.internal.VariantIdentifier
import org.gradle.api.Task
import spock.lang.Specification
import spock.lang.Subject

@Subject(TaskIdentifier)
class TaskIdentifierTest extends Specification {
	def "can create identifier owner by a project using factory method"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def taskName = TaskName.of('create')

		when:
		def identifier = TaskIdentifier.of(taskName, TestableTask, projectIdentifier)

		then:
		identifier.name == taskName
		identifier.type == TestableTask
		identifier.ownerIdentifier == projectIdentifier
	}

	def "can create identifier owner by a component using factory method"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)
		def taskName = TaskName.of('create')

		when:
		def identifier = TaskIdentifier.of(taskName, TestableTask, componentIdentifier)

		then:
		identifier.name == taskName
		identifier.type == TestableTask
		identifier.ownerIdentifier == componentIdentifier
	}

	def "can create identifier owner by a variant using factory method"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('debug', componentIdentifier)
		def taskName = TaskName.of('create')

		when:
		def identifier = TaskIdentifier.of(taskName, TestableTask, variantIdentifier)

		then:
		identifier.name == taskName
		identifier.type == TestableTask
		identifier.ownerIdentifier == variantIdentifier
	}

	def "task name for project owned identifier is the same as a verb-only task name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()

		expect:
		identifier('compile', projectIdentifier).taskName == 'compile'
		identifier('link', projectIdentifier).taskName == 'link'
		identifier('objects', projectIdentifier).taskName == 'objects'
	}

	def "task name for project owned identifier is the same as a verb/object concatenation task name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()

		expect:
		identifier('compile', 'c', projectIdentifier).taskName == 'compileC'
		identifier('link', 'sharedLibrary', projectIdentifier).taskName == 'linkSharedLibrary'
		identifier('generate', 'proto', projectIdentifier).taskName == 'generateProto'
	}

	def "task name for main component owned identifier is the same as a verb-only task name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)

		expect:
		identifier('compile', componentIdentifier).taskName == 'compile'
		identifier('link', componentIdentifier).taskName == 'link'
		identifier('objects', componentIdentifier).taskName == 'objects'
	}

	def "task name for main component owned identifier is the same as a verb/object concatenation task name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)

		expect:
		identifier('compile', 'c', componentIdentifier).taskName == 'compileC'
		identifier('link', 'sharedLibrary', componentIdentifier).taskName == 'linkSharedLibrary'
		identifier('generate', 'proto', componentIdentifier).taskName == 'generateProto'
	}

	def "task name for non-main component owned identifier is the same as a verb-only task name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def testComponentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), projectIdentifier)
		def integTestComponentIdentifier = ComponentIdentifier.of(ComponentName.of('integTest'), projectIdentifier)

		expect:
		identifier('compile', testComponentIdentifier).taskName == 'compileTest'
		identifier('link', testComponentIdentifier).taskName == 'linkTest'
		identifier('objects', testComponentIdentifier).taskName == 'objectsTest'

		and:
		identifier('compile', integTestComponentIdentifier).taskName == 'compileIntegTest'
		identifier('link', integTestComponentIdentifier).taskName == 'linkIntegTest'
		identifier('objects', integTestComponentIdentifier).taskName == 'objectsIntegTest'
	}

	def "task name for non-main component owned identifier is the same as a verb/object concatenation task name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def testComponentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), projectIdentifier)
		def integTestComponentIdentifier = ComponentIdentifier.of(ComponentName.of('integTest'), projectIdentifier)

		expect:
		identifier('compile', 'c', testComponentIdentifier).taskName == 'compileTestC'
		identifier('link', 'sharedLibrary', testComponentIdentifier).taskName == 'linkTestSharedLibrary'
		identifier('objects', 'fromC', testComponentIdentifier).taskName == 'objectsTestFromC'

		and:
		identifier('compile', 'c', integTestComponentIdentifier).taskName == 'compileIntegTestC'
		identifier('link', 'sharedLibrary', integTestComponentIdentifier).taskName == 'linkIntegTestSharedLibrary'
		identifier('generate', 'proto', integTestComponentIdentifier).taskName == 'generateIntegTestProto'
	}

	def "task name for variant owned identifier of main component is the concatenation of verb-only task name and variant name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('macosDebug', componentIdentifier)

		expect:
		identifier('compile', variantIdentifier).taskName == 'compileMacosDebug'
		identifier('link', variantIdentifier).taskName == 'linkMacosDebug'
		identifier('objects', variantIdentifier).taskName == 'objectsMacosDebug'
	}

	def "task name for variant owned identifier of main component is the concatenation of verb/object task name and variant name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('macosDebug', componentIdentifier)

		expect:
		identifier('compile', 'c', variantIdentifier).taskName == 'compileMacosDebugC'
		identifier('link', 'sharedLibrary', variantIdentifier).taskName == 'linkMacosDebugSharedLibrary'
		identifier('generate', 'proto', variantIdentifier).taskName == 'generateMacosDebugProto'
	}

	def "task name for variant owned identifier of non-main component is the concatenation of verb-only task name, component name and variant name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('macosDebug', componentIdentifier)

		expect:
		identifier('compile', variantIdentifier).taskName == 'compileTestMacosDebug'
		identifier('link', variantIdentifier).taskName == 'linkTestMacosDebug'
		identifier('objects', variantIdentifier).taskName == 'objectsTestMacosDebug'
	}

	def "task name for variant owned identifier of non-main component is the concatenation of verb/object task name, component name and variant name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('macosDebug', componentIdentifier)

		expect:
		identifier('compile', 'c', variantIdentifier).taskName == 'compileTestMacosDebugC'
		identifier('link', 'sharedLibrary', variantIdentifier).taskName == 'linkTestMacosDebugSharedLibrary'
		identifier('generate', 'proto', variantIdentifier).taskName == 'generateTestMacosDebugProto'
	}

	def "task name for unique variant owned identifier of main component is the same as task name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('', componentIdentifier)

		expect:
		identifier('create', variantIdentifier).taskName == 'create'
		identifier('link', variantIdentifier).taskName == 'link'
		identifier('objects', variantIdentifier).taskName == 'objects'
	}

	def "task name for unique variant owned identifier of non-main component is the same as concatenation task name and component name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('', componentIdentifier)

		expect:
		identifier('create', variantIdentifier).taskName == 'createTest'
		identifier('link', variantIdentifier).taskName == 'linkTest'
		identifier('objects', variantIdentifier).taskName == 'objectsTest'
	}

	def "throw exception if task name is null"() {
		given:
		def ownerIdentifier = ProjectIdentifier.ofRootProject()

		when:
        TaskIdentifier.of((TaskName) null, TestableTask, ownerIdentifier)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a task identifier because the task name is null.'
	}

	def "throw exception if type is null"() {
		given:
		def ownerIdentifier = ProjectIdentifier.ofRootProject()

		when:
        TaskIdentifier.of(TaskName.of('create'), null, ownerIdentifier)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a task identifier because the task type is null.'
	}

	def "throw exception if owner is null"() {
		when:
        TaskIdentifier.of(TaskName.of('create'), TestableTask, null)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a task identifier because the owner identifier is null.'
	}

	def "can compare identifier based on task name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('macosDebug', componentIdentifier)

		expect:
		identifier('foo', projectIdentifier) == identifier('foo', projectIdentifier)
		identifier('foo', projectIdentifier) != identifier('bar', projectIdentifier)

		and:
		identifier('foo', componentIdentifier) == identifier('foo', componentIdentifier)
		identifier('foo', componentIdentifier) != identifier('bar', componentIdentifier)

		and:
		identifier('foo', variantIdentifier) == identifier('foo', variantIdentifier)
		identifier('foo', variantIdentifier) != identifier('bar', variantIdentifier)
	}

	def "can compare identifier based on task type"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('macosDebug', componentIdentifier)

		expect:
		identifier('foo', TestableTask, projectIdentifier) == identifier('foo', TestableTask, projectIdentifier)
		identifier('foo', TestableTask, projectIdentifier) != identifier('foo', AnotherDummyTask, projectIdentifier)

		and:
		identifier('foo', TestableTask, componentIdentifier) == identifier('foo', TestableTask, componentIdentifier)
		identifier('foo', TestableTask, componentIdentifier) != identifier('foo', AnotherDummyTask, componentIdentifier)

		and:
		identifier('foo', TestableTask, variantIdentifier) == identifier('foo', TestableTask, variantIdentifier)
		identifier('foo', TestableTask, variantIdentifier) != identifier('foo', AnotherDummyTask, variantIdentifier)
	}

	def "can compare identifier based on owner"() {
		given:
		def ownerProject = ProjectIdentifier.ofRootProject()
		def ownerComponent = ComponentIdentifier.of(ComponentName.of('test'), ownerProject)
		def ownerVariant = VariantIdentifier.of('macosDebug', ownerComponent)

		expect:
		identifier('foo', ownerProject) == identifier('foo', ownerProject)
		identifier('foo', ownerProject) != identifier('foo', ownerComponent)
		identifier('foo', ownerProject) != identifier('foo', ownerVariant)

		and:
		identifier('foo', ownerComponent) == identifier('foo', ownerComponent)
		identifier('foo', ownerComponent) != identifier('foo', ownerProject)
		identifier('foo', ownerComponent) != identifier('foo', ownerVariant)

		and:
		identifier('foo', ownerVariant) == identifier('foo', ownerVariant)
		identifier('foo', ownerVariant) != identifier('foo', ownerProject)
		identifier('foo', ownerVariant) != identifier('foo', ownerComponent)
	}

	def "can create pure lifecycle identifier owned by a variant of main component"() {
		given:
		def ownerProject = ProjectIdentifier.ofRootProject()
		def ownerComponent = ComponentIdentifier.ofMain(ownerProject)
		def ownerVariant = VariantIdentifier.of('macosDebug', ownerComponent)

		when:
		def result = TaskIdentifier.ofLifecycle(ownerVariant)

		then:
		result.name == TaskName.empty()
		result.taskName == 'macosDebug'
		result.type == Task
		result.ownerIdentifier == ownerVariant
	}

	def "can create pure lifecycle identifier owned by a variant of non-main component"() {
		given:
		def ownerProject = ProjectIdentifier.ofRootProject()
		def ownerComponent = ComponentIdentifier.of(ComponentName.of('test'), ownerProject)
		def ownerVariant = VariantIdentifier.of('macosDebug', ownerComponent)

		when:
		def result = TaskIdentifier.ofLifecycle(ownerVariant)

		then:
		result.name == TaskName.empty()
		result.taskName == 'testMacosDebug'
		result.type == Task
		result.ownerIdentifier == ownerVariant
	}

	def "can create pure lifecycle identifier owned by a single-variant of non-main component"() {
		given:
		def ownerProject = ProjectIdentifier.ofRootProject()
		def ownerComponent = ComponentIdentifier.of(ComponentName.of('test'), ownerProject)
		def ownerVariant = VariantIdentifier.of('', ownerComponent)

		when:
		def result = TaskIdentifier.ofLifecycle(ownerVariant)

		then:
		result.name == TaskName.empty()
		result.taskName == 'test'
		result.type == Task
		result.ownerIdentifier == ownerVariant
	}

	def "can create pure lifecycle identifier owned by a non-main component"() {
		given:
		def ownerProject = ProjectIdentifier.ofRootProject()
		def ownerComponent = ComponentIdentifier.of(ComponentName.of('integTest'), ownerProject)

		when:
		def result = TaskIdentifier.ofLifecycle(ownerComponent)

		then:
		result.name == TaskName.empty()
		result.taskName == 'integTest'
		result.type == Task
		result.ownerIdentifier == ownerComponent
	}

	def "throws exception when creating pure lifecycle identifier owned by a main component"() {
		given:
		def ownerProject = ProjectIdentifier.ofRootProject()
		def ownerComponent = ComponentIdentifier.ofMain(ownerProject)

		when:
		TaskIdentifier.ofLifecycle(ownerComponent)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a lifecycle task identifier for specified owner as it will result into an invalid task name.'
	}

	def "throws exception when creating pure lifecycle identifier owned by a project"() {
		given:
		def ownerProject = ProjectIdentifier.ofRootProject()

		when:
		TaskIdentifier.ofLifecycle(ownerProject)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a lifecycle task identifier for specified owner as it will result into an invalid task name.'
	}

	def "throws exception when creating pure lifecycle identifier owned by a single-variant of a main component"() {
		given:
		def ownerProject = ProjectIdentifier.ofRootProject()
		def ownerComponent = ComponentIdentifier.ofMain(ownerProject)
		def ownerVariant = VariantIdentifier.of('', ownerComponent)

		when:
		TaskIdentifier.ofLifecycle(ownerVariant)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a lifecycle task identifier for specified owner as it will result into an invalid task name.'
	}

	def "has meaningful toString() implementation"() {
		given:
		def ownerRootProject = ProjectIdentifier.ofRootProject()
		def ownerComponentInRootProject = ComponentIdentifier.ofMain(ownerRootProject)
		def ownerVariantInRootProject = VariantIdentifier.of('macosDebug', ownerComponentInRootProject)

		and:
		def ownerChildProject = ProjectIdentifier.ofChildProject('foo')
		def ownerComponentInChildProject = ComponentIdentifier.ofMain(ownerChildProject)
		def ownerVariantInChildProject = VariantIdentifier.of('macosDebug', ownerComponentInChildProject)

		expect: 'in a root project'
		TaskIdentifier.ofLifecycle(ownerVariantInRootProject).toString() == "task ':main:macosDebug'"
		TaskIdentifier.ofLifecycle(ComponentIdentifier.of(ComponentName.of('test'), ownerRootProject)).toString() == "task ':test'"
		TaskIdentifier.of(TaskName.of('compile', 'c'), TestableTask, ownerVariantInRootProject).toString() == "task ':main:macosDebug:compileC'"
		TaskIdentifier.of(TaskName.of('create', 'jar'), TestableTask, ownerComponentInRootProject).toString() == "task ':main:createJar'"
		TaskIdentifier.of(TaskName.of('link'), TestableTask, ownerRootProject).toString() == "task ':link'"

		and: 'in a child project'
		TaskIdentifier.ofLifecycle(ownerVariantInChildProject).toString() == "task ':main:macosDebug'"
		TaskIdentifier.ofLifecycle(ComponentIdentifier.of(ComponentName.of('test'), ownerChildProject)).toString() == "task ':test'"
		TaskIdentifier.of(TaskName.of('compile', 'c'), TestableTask, ownerVariantInChildProject).toString() == "task ':main:macosDebug:compileC'"
		TaskIdentifier.of(TaskName.of('create', 'jar'), TestableTask, ownerComponentInChildProject).toString() == "task ':main:createJar'"
		TaskIdentifier.of(TaskName.of('link'), TestableTask, ownerChildProject).toString() == "task ':link'"
	}

	private static TaskIdentifier identifier(String verb, DomainObjectIdentifier owner) {
		return TaskIdentifier.of(TaskName.of(verb), TaskIdentifierTest.TestableTask, owner)
	}

	private static TaskIdentifier identifier(String verb, Class type, DomainObjectIdentifier owner) {
		return TaskIdentifier.of(TaskName.of(verb), type, owner)
	}

	private static TaskIdentifier identifier(String verb, String object, DomainObjectIdentifier owner) {
		return TaskIdentifier.of(TaskName.of(verb, object), TaskIdentifierTest.TestableTask, owner)
	}

	interface TestableTask extends Task {}
	interface AnotherDummyTask extends Task {}
}
