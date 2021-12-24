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

import dev.nokee.model.KnownDomainObject
import dev.nokee.model.internal.DefaultKnownDomainObject
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import dev.nokee.platform.base.internal.VariantIdentifier
import dev.nokee.platform.base.internal.tasks.TaskIdentifier
import dev.nokee.platform.base.internal.tasks.TaskName
import dev.nokee.platform.base.internal.tasks.TaskRegistry
import dev.nokee.platform.nativebase.internal.tasks.ObjectsLifecycleTask
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import spock.lang.Specification
import spock.lang.Subject

import static ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS
import static dev.nokee.utils.TaskUtils.configureDependsOn

@Subject(CreateVariantObjectsLifecycleTaskRule)
class CreateVariantObjectsLifecycleTaskRuleTest extends Specification {
	KnownDomainObject newSubject(VariantIdentifier identifier) {
		return new DefaultKnownDomainObject<>(identifier, Variant.class, { Stub(Provider) }, {})
	}

	KnownDomainObject newSubject(VariantIdentifier identifier, Provider provider) {
		return new DefaultKnownDomainObject<>(identifier, Variant.class, { provider }, {})
	}

	def "creates an objects task owned by the variant"() {
		given:
		def taskRegistry = Mock(TaskRegistry)
		def subject = new CreateVariantObjectsLifecycleTaskRule(taskRegistry)

		and:
		def owner1 = VariantIdentifier.of('debug', Variant, ComponentIdentifier.ofMain(ProjectIdentifier.of('root')))

		and:
		def owner2 = VariantIdentifier.of('macos', Variant, ComponentIdentifier.of(ComponentName.of('test'), ProjectIdentifier.of('root')))

		when:
		subject.execute(newSubject(owner1))
		then:
		1 * taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of('objects'), ObjectsLifecycleTask, owner1)) >> Stub(TaskProvider)
		0 * taskRegistry._

		when:
		subject.execute(newSubject(owner2))
		then:
		1 * taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of('objects'), ObjectsLifecycleTask, owner2)) >> Stub(TaskProvider)
		0 * taskRegistry._
	}

	def "configures the task with dependency of mapping to development binaries compile tasks"() {
		given:
		def taskProvider = Mock(TaskProvider)
		def taskRegistry = Stub(TaskRegistry) {
			registerIfAbsent(_) >> taskProvider
		}
		def subject = new CreateVariantObjectsLifecycleTaskRule(taskRegistry)

		and:
		def owner = VariantIdentifier.of('debug', Variant, ComponentIdentifier.ofMain(ProjectIdentifier.of('root')))
		def valueFlatMapProvider = Stub(Provider)
		def value = Mock(Provider)

		and:
		def knownVariant = newSubject(owner, value)

		when:
		subject.execute(knownVariant)

		then:
		1 * taskProvider.configure(configureDependsOn(valueFlatMapProvider))
		1 * value.flatMap(TO_DEVELOPMENT_BINARY_COMPILE_TASKS) >> valueFlatMapProvider // because provider don't have equals
	}
}
