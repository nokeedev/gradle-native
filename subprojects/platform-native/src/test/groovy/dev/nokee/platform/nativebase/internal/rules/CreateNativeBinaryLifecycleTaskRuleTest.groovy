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

import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.DefaultBuildVariant
import dev.nokee.platform.base.internal.VariantIdentifier
import dev.nokee.platform.base.internal.tasks.TaskIdentifier
import dev.nokee.platform.base.internal.tasks.TaskName
import dev.nokee.platform.base.internal.tasks.TaskRegistry
import dev.nokee.platform.base.internal.variants.KnownVariant
import dev.nokee.platform.nativebase.internal.tasks.ExecutableLifecycleTask
import dev.nokee.platform.nativebase.internal.tasks.SharedLibraryLifecycleTask
import dev.nokee.platform.nativebase.internal.tasks.StaticLibraryLifecycleTask
import dev.nokee.runtime.core.Coordinates
import dev.nokee.runtime.nativebase.internal.TargetLinkages
import dev.nokee.runtime.nativebase.internal.TargetMachines
import org.gradle.api.provider.Provider
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.nokee.utils.TaskUtils.configureDependsOn

@Subject(CreateNativeBinaryLifecycleTaskRule)
class CreateNativeBinaryLifecycleTaskRuleTest extends Specification {
	KnownVariant newSubject(VariantIdentifier identifier) {
		return new KnownVariant<>(identifier, Stub(Provider), null)
	}

	KnownVariant newSubject(VariantIdentifier identifier, Provider provider) {
		return new KnownVariant<>(identifier, provider, null)
	}

	VariantIdentifier<Variant> newIdentifier() {
		return newIdentifier(DefaultBuildVariant.of(Coordinates.of(TargetMachines.host())))
	}

	VariantIdentifier<Variant> newIdentifier(DefaultBuildVariant buildVariant) {
		return VariantIdentifier.of(buildVariant, Variant, ComponentIdentifier.ofMain(ProjectIdentifier.of('root')))
	}

	def "do nothing when build variant does not contain binary linkage"() {
		given:
		def taskRegistry = Mock(TaskRegistry)
		def subject = new CreateNativeBinaryLifecycleTaskRule(taskRegistry)

		and:
		def knownVariant = newSubject(newIdentifier())

		when:
		subject.execute(knownVariant)

		then:
		0 * taskRegistry._
	}

	def "registers shared library binary lifecycle task"() {
		given:
		def taskRegistry = Mock(TaskRegistry)
		def subject = new CreateNativeBinaryLifecycleTaskRule(taskRegistry)

		and:
		def identifier = newIdentifier(DefaultBuildVariant.of(Coordinates.of(TargetLinkages.SHARED)))
		def knownVariant = newSubject(identifier)

		when:
		subject.execute(knownVariant)

		then:
		1 * taskRegistry.register(TaskIdentifier.of(TaskName.of('sharedLibrary'), SharedLibraryLifecycleTask, identifier), _)
	}

	def "registers static library binary lifecycle task"() {
		given:
		def taskRegistry = Mock(TaskRegistry)
		def subject = new CreateNativeBinaryLifecycleTaskRule(taskRegistry)

		def identifier = newIdentifier(DefaultBuildVariant.of(Coordinates.of(TargetLinkages.STATIC)))
		def knownVariant = newSubject(identifier)

		when:
		subject.execute(knownVariant)

		then:
		1 * taskRegistry.register(TaskIdentifier.of(TaskName.of('staticLibrary'), StaticLibraryLifecycleTask, identifier), _)
	}

	def "registers executable binary lifecycle task"() {
		given:
		def taskRegistry = Mock(TaskRegistry)
		def subject = new CreateNativeBinaryLifecycleTaskRule(taskRegistry)

		and:
		def identifier = newIdentifier(DefaultBuildVariant.of(Coordinates.of(TargetLinkages.EXECUTABLE)))
		def knownVariant = newSubject(identifier)

		when:
		subject.execute(knownVariant)

		then:
		1 * taskRegistry.register(TaskIdentifier.of(TaskName.of('executable'), ExecutableLifecycleTask, identifier), _)
	}

	@Unroll
	def "configures lifecycle task dependency to variant's development binary"(linkage) {
		given:
		def taskRegistry = Mock(TaskRegistry)
		def subject = new CreateNativeBinaryLifecycleTaskRule(taskRegistry)

		and:
		def identifier = newIdentifier(DefaultBuildVariant.of(linkage))
		def valueMapProvider = Stub(Provider)
		def value = Mock(Provider)
		def knownVariant = newSubject(identifier, value)

		when:
		subject.execute(knownVariant)

		then:
		1 * taskRegistry.register(_, configureDependsOn(valueMapProvider))
		1 * value.flatMap(ToDevelopmentBinaryTransformer.TO_DEVELOPMENT_BINARY) >> valueMapProvider // because provider don't have equals

		where:
		linkage << [TargetLinkages.SHARED, TargetLinkages.STATIC, TargetLinkages.EXECUTABLE]
	}

	def "throws exception for unknown linkage"() {
		given:
		def taskRegistry = Mock(TaskRegistry)
		def subject = new CreateNativeBinaryLifecycleTaskRule(taskRegistry)

		and:
		def identifier = newIdentifier(DefaultBuildVariant.of(Coordinates.of(TargetLinkages.BUNDLE)))
		def value = Mock(Provider)
		def knownVariant = newSubject(identifier, value)

		when:
		subject.execute(knownVariant)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Unknown linkage 'bundle'."

		and:
		0 * taskRegistry._
	}
}
