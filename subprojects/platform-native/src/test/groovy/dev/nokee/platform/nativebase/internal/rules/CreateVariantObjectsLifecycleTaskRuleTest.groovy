package dev.nokee.platform.nativebase.internal.rules

import dev.nokee.model.internal.Value
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.internal.*
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
	def "creates an objects task owned by the variant"() {
		given:
		def taskRegistry = Mock(TaskRegistry)
		def subject = new CreateVariantObjectsLifecycleTaskRule(taskRegistry)

		and:
		def owner1 = VariantIdentifier.of('debug', Variant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))
		def value1 = Value.fixed(Stub(Variant))

		and:
		def owner2 = VariantIdentifier.of('macos', Variant, ComponentIdentifier.of(ComponentName.of('test'), Component, ProjectIdentifier.of('root')))
		def value2 = Value.fixed(Stub(Variant))

		when:
		subject.execute(KnownVariant.of(owner1, value1))
		then:
		1 * taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of('objects'), ObjectsLifecycleTask, owner1)) >> Stub(TaskProvider)
		0 * taskRegistry._

		when:
		subject.execute(KnownVariant.of(owner2, value2))
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
		def owner = VariantIdentifier.of('debug', Variant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))
		def valueFlatMapProvider = Stub(Provider)
		def value = Mock(Value)

		and:
		def knownVariant = KnownVariant.of(owner, value)

		when:
		subject.execute(knownVariant)

		then:
		1 * taskProvider.configure(configureDependsOn(valueFlatMapProvider))
		1 * value.flatMap(TO_DEVELOPMENT_BINARY_COMPILE_TASKS) >> valueFlatMapProvider // because provider don't have equals
	}
}
