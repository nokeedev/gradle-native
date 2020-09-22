package dev.nokee.platform.nativebase.internal.rules

import dev.nokee.platform.base.Binary
import dev.nokee.platform.base.TaskView
import dev.nokee.platform.base.Variant
import dev.nokee.platform.nativebase.NativeBinary
import dev.nokee.utils.ProviderUtils
import org.gradle.api.provider.Provider
import spock.lang.Specification
import spock.lang.Subject

import static ToDevelopmentBinaryCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS

@Subject(ToDevelopmentBinaryCompileTasksTransformer)
class ToDevelopmentBinaryCompileTasksTransformerTest extends Specification {

	def "returns provider to empty list when development binary is not a native binary"() {
		given:
		def variant = Stub(Variant) {
			getDevelopmentBinary() >> ProviderUtils.fixed(Stub(Binary))
		}

		when:
		def provider = TO_DEVELOPMENT_BINARY_COMPILE_TASKS.transform(variant)

		then:
		provider != null
		provider.present
		provider.get().isEmpty()
	}

	def "returns provider to binary's compile tasks when development binary is a native binary"() {
		given:
		def taskViewElements = Stub(Provider)
		def taskView = Mock(TaskView)
		def binary = Mock(NativeBinary)
		def variant = Stub(Variant) {
			getDevelopmentBinary() >> ProviderUtils.fixed(binary)
		}

		when:
		def provider = TO_DEVELOPMENT_BINARY_COMPILE_TASKS.transform(variant)

		then:
		provider == taskViewElements
		1 * binary.getCompileTasks() >> taskView
		1 * taskView.getElements() >> taskViewElements
	}
}
