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
package dev.nokee.platform.nativebase.internal.rules

import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import dev.nokee.platform.base.internal.VariantAwareComponentInternal
import dev.nokee.platform.base.internal.tasks.TaskIdentifier
import dev.nokee.platform.base.internal.tasks.TaskName
import dev.nokee.platform.base.internal.tasks.TaskRegistry
import dev.nokee.platform.nativebase.internal.tasks.ObjectsLifecycleTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import spock.lang.Specification
import spock.lang.Subject

import static ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory
import static dev.nokee.utils.TaskUtils.configureDependsOn

@Subject(CreateVariantAwareComponentObjectsLifecycleTaskRule)
class CreateVariantAwareComponentObjectsLifecycleTaskRuleTest extends Specification {
	def "creates an objects task owned by the variant"() {
		given:
		def taskRegistry = Mock(TaskRegistry)
		def subject = new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry)

		and:
		def owner1 = ComponentIdentifier.ofMain(ProjectIdentifier.of('root'))
		def owner2 = ComponentIdentifier.of(ComponentName.of('test'), ProjectIdentifier.of('root'))

		and:
		def component = Mock(VariantAwareComponentInternal) {
			getDevelopmentVariant() >> objectFactory().property(Variant)
		}

		when:
		subject.execute(component)
		then:
		1 * component.identifier >> owner1
		1 * taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of('objects'), ObjectsLifecycleTask, owner1)) >> Stub(TaskProvider)
		0 * taskRegistry._

		when:
		subject.execute(component)
		then:
		1 * component.identifier >> owner2
		1 * taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of('objects'), ObjectsLifecycleTask, owner2)) >> Stub(TaskProvider)
		0 * taskRegistry._
	}

	def "configures the task with dependency of mapping to development binaries compile tasks of the development variant"() {
		given:
		def taskProvider = Mock(TaskProvider)
		def taskRegistry = Stub(TaskRegistry) {
			registerIfAbsent(_) >> taskProvider
		}
		def subject = new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry)

		and:
		def developmentVariantFlatMapProvider = Stub(Provider)
		def developmentVariantProvider = Mock(Property)

		and:
		def owner = ComponentIdentifier.ofMain(ProjectIdentifier.of('root'))
		def component = Stub(VariantAwareComponentInternal) {
			getIdentifier() >> owner
			getDevelopmentVariant() >> developmentVariantProvider
		}

		when:
		subject.execute(component)

		then:
		1 * taskProvider.configure(configureDependsOn(developmentVariantFlatMapProvider))
		1 * developmentVariantProvider.flatMap(TO_DEVELOPMENT_BINARY_COMPILE_TASKS) >> developmentVariantFlatMapProvider // because provider don't have equals
	}
}
