package dev.nokee.platform.nativebase.internal.rules

import dev.nokee.model.internal.Value
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.internal.*
import dev.nokee.platform.base.internal.tasks.TaskIdentifier
import dev.nokee.platform.base.internal.tasks.TaskName
import dev.nokee.platform.base.internal.tasks.TaskRegistry
import dev.nokee.platform.nativebase.internal.DefaultBinaryLinkage
import dev.nokee.platform.nativebase.internal.tasks.ExecutableLifecycleTask
import dev.nokee.platform.nativebase.internal.tasks.SharedLibraryLifecycleTask
import dev.nokee.platform.nativebase.internal.tasks.StaticLibraryLifecycleTask
import org.gradle.api.provider.Provider
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.nokee.utils.TaskUtils.configureDependsOn

@Subject(CreateNativeBinaryLifecycleTaskRule)
class CreateNativeBinaryLifecycleTaskRuleTest extends Specification {
	def "do nothing when build variant does not contain binary linkage"() {
		given:
		def taskRegistry = Mock(TaskRegistry)
		def subject = new CreateNativeBinaryLifecycleTaskRule(taskRegistry)

		and:
		def buildVariant = DefaultBuildVariant.of()
		def identifier = VariantIdentifier.of(buildVariant, Variant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))
		def value = Value.fixed(Stub(Variant))
		def knownVariant = KnownVariant.of(identifier, value)

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
		def buildVariant = DefaultBuildVariant.of(DefaultBinaryLinkage.SHARED)
		def identifier = VariantIdentifier.of(buildVariant, Variant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))
		def value = Value.fixed(Stub(Variant))
		def knownVariant = KnownVariant.of(identifier, value)

		when:
		subject.execute(knownVariant)

		then:
		1 * taskRegistry.register(TaskIdentifier.of(TaskName.of('sharedLibrary'), SharedLibraryLifecycleTask, identifier), _)
	}

	def "registers static library binary lifecycle task"() {
		given:
		def taskRegistry = Mock(TaskRegistry)
		def subject = new CreateNativeBinaryLifecycleTaskRule(taskRegistry)

		def buildVariant = DefaultBuildVariant.of(DefaultBinaryLinkage.STATIC)
		def identifier = VariantIdentifier.of(buildVariant, Variant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))
		def value = Value.fixed(Stub(Variant))
		def knownVariant = KnownVariant.of(identifier, value)

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
		def buildVariant = DefaultBuildVariant.of(DefaultBinaryLinkage.EXECUTABLE)
		def identifier = VariantIdentifier.of(buildVariant, Variant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))
		def value = Value.fixed(Stub(Variant))
		def knownVariant = KnownVariant.of(identifier, value)

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
		def buildVariant = DefaultBuildVariant.of(linkage)
		def identifier = VariantIdentifier.of(buildVariant, Variant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))
		def valueMapProvider = Stub(Provider)
		def value = Mock(Value)
		def knownVariant = KnownVariant.of(identifier, value)

		when:
		subject.execute(knownVariant)

		then:
		1 * taskRegistry.register(_, configureDependsOn(valueMapProvider))
		1 * value.map(ToDevelopmentBinaryTransformer.TO_DEVELOPMENT_BINARY) >> valueMapProvider // because provider don't have equals

		where:
		linkage << [DefaultBinaryLinkage.SHARED, DefaultBinaryLinkage.STATIC, DefaultBinaryLinkage.EXECUTABLE]
	}

	def "throws exception for unknown linkage"() {
		given:
		def taskRegistry = Mock(TaskRegistry)
		def subject = new CreateNativeBinaryLifecycleTaskRule(taskRegistry)

		and:
		def buildVariant = DefaultBuildVariant.of(new DefaultBinaryLinkage('foo'))
		def identifier = VariantIdentifier.of(buildVariant, Variant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))
		def value = Value.fixed(Stub(Variant))
		def knownVariant = KnownVariant.of(identifier, value)

		when:
		subject.execute(knownVariant)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Unknown linkage 'foo'."

		and:
		0 * taskRegistry._
	}
}
