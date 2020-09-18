package dev.nokee.platform.ios.internal.rules

import dev.nokee.platform.base.Binary
import dev.nokee.platform.ios.internal.SignedIosApplicationBundle
import dev.nokee.utils.ProviderUtils
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.platform.ios.internal.rules.IosDevelopmentBinaryConvention.INSTANCE

@Subject(IosDevelopmentBinaryConvention)
class IosDevelopmentBinaryConventionTest extends Specification {
	def "returns undefined provider on empty list"() {
		expect:
		INSTANCE.transform([]) == ProviderUtils.notDefined()
	}

	def "throws exception when multiple SignedIosApplicationBundle binaries"() {
		when:
		INSTANCE.transform([Stub(SignedIosApplicationBundle), Stub(SignedIosApplicationBundle)])

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "expected one element but was: <Mock for type 'SignedIosApplicationBundle', Mock for type 'SignedIosApplicationBundle'>"
	}

	def "returns a provider of the single SignedIosApplicationBundle binary list"() {
		given:
		def binary = Stub(SignedIosApplicationBundle)

		expect:
		def result = INSTANCE.transform([binary])
		result.present
		result.get() == binary
	}

	def "returns a provider of a multi-binary list containing one SignedIosApplicationBundle binary"() {
		given:
		def binary = Stub(SignedIosApplicationBundle)

		expect:
		def result1 = INSTANCE.transform([binary, Stub(Binary)])
		result1.present
		result1.get() == binary

		and:
		def result2 = INSTANCE.transform([Stub(Binary), binary])
		result2.present
		result2.get() == binary

		and:
		def result3 = INSTANCE.transform([Stub(Binary), binary, Stub(Binary)])
		result3.present
		result3.get() == binary
	}
}
