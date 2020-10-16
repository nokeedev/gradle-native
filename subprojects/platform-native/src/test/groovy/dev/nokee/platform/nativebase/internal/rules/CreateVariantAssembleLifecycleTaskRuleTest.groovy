package dev.nokee.platform.nativebase.internal.rules

import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.internal.*
import dev.nokee.platform.base.internal.tasks.TaskIdentifier
import dev.nokee.platform.base.internal.tasks.TaskName
import dev.nokee.platform.base.internal.tasks.TaskRegistry
import dev.nokee.platform.base.internal.variants.KnownVariant
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.platform.nativebase.internal.rules.ToDevelopmentBinaryTransformer.TO_DEVELOPMENT_BINARY
import static dev.nokee.utils.TaskUtils.configureDependsOn
import static dev.nokee.utils.TaskUtils.configureGroup
import static org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP

@Subject(CreateVariantAssembleLifecycleTaskRule)
class CreateVariantAssembleLifecycleTaskRuleTest extends Specification {
	KnownVariant newSubject(VariantIdentifier identifier) {
		return new KnownVariant<>(identifier, Stub(Provider), null)
	}

	KnownVariant newSubject(VariantIdentifier identifier, Provider provider) {
		return new KnownVariant<>(identifier, provider, null)
	}

	def "creates an objects task owned by the variant"() {
		given:
		def taskRegistry = Mock(TaskRegistry)
		def subject = new CreateVariantAssembleLifecycleTaskRule(taskRegistry)

		and:
		def owner1 = VariantIdentifier.of('debug', Variant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))

		and:
		def owner2 = VariantIdentifier.of('macos', Variant, ComponentIdentifier.of(ComponentName.of('test'), Component, ProjectIdentifier.of('root')))

		when:
		subject.execute(newSubject(owner1))
		then:
		1 * taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of('assemble'), owner1), configureGroup(BUILD_GROUP)) >> Stub(TaskProvider)
		0 * taskRegistry._

		when:
		subject.execute(newSubject(owner2))
		then:
		1 * taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of('assemble'), owner2), configureGroup(BUILD_GROUP)) >> Stub(TaskProvider)
		0 * taskRegistry._
	}

	def "configures the task with dependency of mapping to development binary"() {
		given:
		def taskProvider = Mock(TaskProvider)
		def taskRegistry = Stub(TaskRegistry) {
			registerIfAbsent(_, _) >> taskProvider
		}
		def subject = new CreateVariantAssembleLifecycleTaskRule(taskRegistry)

		and:
		def owner = VariantIdentifier.of('debug', Variant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))
		def valueFlatMapProvider = Stub(Provider)
		def value = Mock(Provider)

		and:
		def knownVariant = newSubject(owner, value)

		when:
		subject.execute(knownVariant)

		then:
		1 * taskProvider.configure(configureDependsOn(valueFlatMapProvider))
		1 * value.flatMap(TO_DEVELOPMENT_BINARY) >> valueFlatMapProvider // because provider don't have equals
	}
}
