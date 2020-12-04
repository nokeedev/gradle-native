package dev.nokee.platform.nativebase.internal.rules

import dev.nokee.internal.testing.utils.TestUtils
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.VariantAwareComponentInternal
import org.gradle.api.Project
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.nokee.utils.TaskUtils.configureGroup

@Subject(CreateVariantAwareComponentAssembleLifecycleTaskRule)
class CreateVariantAwareComponentAssembleLifecycleTaskRuleTest extends Specification implements TaskEntityFixture, ComponentEntityFixture {
	Project project = TestUtils.rootProject()
	def subject = new CreateVariantAwareComponentAssembleLifecycleTaskRule(taskRegistry)

	VariantAwareComponentInternal<?> aComponent(ComponentIdentifier<?> identifier) {
		return Stub(VariantAwareComponentInternal) {
			getIdentifier() >> identifier
		}
	}

	@Unroll
	def "creates the assemble task if absent"(component) {
		given:
		discovered(component)

		when:
		subject.execute(aComponent(component))

		then:
		def assembleTask = taskRepository.get(aTaskOfComponent('assemble', component))
		assembleTask.group == 'build'

		where:
		component << [mainComponentIdentifier(), aComponentIdentifier('test'), aComponentIdentifier('integTest')]
	}

	@Unroll
	def "does not configure the assemble task group if already present"(component) {
		given:
		discovered(component)
		def assembleTask = taskRegistry.register(aTaskOfComponent('assemble', component), configureGroup('some group')).get()

		when:
		subject.execute(aComponent(component))

		then:
		assembleTask.group == 'some group'

		where:
		component << [mainComponentIdentifier(), aComponentIdentifier('test')]
	}

	@Unroll
	def "configures assemble task with a dependency on the buildable variant or warns"(component) {
		given:
		discovered(component)

		when:
		subject.execute(aComponent(component))

		then:
		def assembleTask = taskRepository.get(aTaskOfComponent('assemble', component))
		assembleTask.dependsOn.size() == 1 // We assume the dependency is buildable variant or warning logger

		where:
		component << [mainComponentIdentifier(), aComponentIdentifier('test')]
	}
}
