package dev.nokee.platform.nativebase.internal.rules


import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import dev.nokee.platform.base.internal.ProjectIdentifier
import dev.nokee.platform.base.internal.VariantAwareComponentInternal
import dev.nokee.platform.base.internal.tasks.TaskIdentifier
import dev.nokee.platform.base.internal.tasks.TaskName
import dev.nokee.platform.base.internal.tasks.TaskRegistry
import dev.nokee.platform.nativebase.internal.tasks.ObjectsLifecycleTask
import dev.nokee.utils.ProviderUtils
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import spock.lang.Specification
import spock.lang.Subject

import static ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS
import static dev.nokee.utils.TaskUtils.configureDependsOn

@Subject(CreateVariantAwareComponentObjectsLifecycleTaskRule)
class CreateVariantAwareComponentObjectsLifecycleTaskRuleTest extends Specification {
	def "creates an objects task owned by the variant"() {
		given:
		def taskRegistry = Mock(TaskRegistry)
		def subject = new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry)

		and:
		def owner1 = ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root'))
		def owner2 = ComponentIdentifier.of(ComponentName.of('test'), Component, ProjectIdentifier.of('root'))

		and:
		def component = Mock(VariantAwareComponentInternal) {
			getDevelopmentVariant() >> ProviderUtils.notDefined()
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
		def developmentVariantProvider = Mock(Provider)

		and:
		def owner = ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root'))
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
