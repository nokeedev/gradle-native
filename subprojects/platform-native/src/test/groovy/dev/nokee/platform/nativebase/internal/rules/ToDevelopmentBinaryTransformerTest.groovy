package dev.nokee.platform.nativebase.internal.rules

import dev.nokee.platform.base.Variant
import org.gradle.api.provider.Provider
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.platform.nativebase.internal.rules.ToDevelopmentBinaryTransformer.TO_DEVELOPMENT_BINARY

@Subject(ToDevelopmentBinaryTransformer)
class ToDevelopmentBinaryTransformerTest extends Specification {
	def "returns variant development binary"() {
		given:
		def variant = Mock(Variant)
		def developmentBinaryProvider = Stub(Provider)

		when:
		def result = TO_DEVELOPMENT_BINARY.transform(variant)

		then:
		1 * variant.getDevelopmentBinary() >> developmentBinaryProvider

		and:
		result == developmentBinaryProvider
	}
}
