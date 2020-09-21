package dev.nokee.platform.nativebase.internal.rules

import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import dev.nokee.platform.base.internal.ProjectIdentifier
import dev.nokee.platform.base.internal.VariantAwareComponentInternal
import dev.nokee.platform.base.internal.tasks.TaskIdentifier
import dev.nokee.platform.base.internal.tasks.TaskName
import dev.nokee.platform.base.internal.tasks.TaskRegistry
import dev.nokee.utils.ProviderUtils
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.platform.nativebase.internal.rules.ToDevelopmentBinaryTransformer.TO_DEVELOPMENT_BINARY
import static dev.nokee.utils.TaskUtils.configureDependsOn
import static dev.nokee.utils.TaskUtils.configureGroup
import static org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP

@Subject(CreateVariantAwareComponentAssembleLifecycleTaskRule)
class CreateVariantAwareComponentAssembleLifecycleTaskRuleTest extends Specification {
	def "creates an assemble task owned by the variant"() {
		given:
		def taskRegistry = Mock(TaskRegistry)
		def subject = new CreateVariantAwareComponentAssembleLifecycleTaskRule(taskRegistry)

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
		1 * taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of('assemble'), owner1), configureGroup(BUILD_GROUP)) >> Stub(TaskProvider)
		0 * taskRegistry._

		when:
		subject.execute(component)
		then:
		1 * component.identifier >> owner2
		1 * taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of('assemble'), owner2), configureGroup(BUILD_GROUP)) >> Stub(TaskProvider)
		0 * taskRegistry._
	}

	def "configures the task with dependency of mapping to development binaries compile tasks of the development variant"() {
		given:
		def taskProvider = Mock(TaskProvider)
		def taskRegistry = Stub(TaskRegistry) {
			registerIfAbsent(_, _) >> taskProvider
		}
		def subject = new CreateVariantAwareComponentAssembleLifecycleTaskRule(taskRegistry)

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
		1 * developmentVariantProvider.flatMap(TO_DEVELOPMENT_BINARY) >> developmentVariantFlatMapProvider // because provider don't have equals
	}
}
