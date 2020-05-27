package dev.nokee.runtime.darwin.internal.locators

import spock.lang.Specification
import spock.lang.Subject

@Subject(XcrunLocator)
class XcrunLocatorTest extends Specification {
	def "can parse output from Xcode 11.3.1"() {
		when:
		def version = XcrunLocator.asXcodeRunVersion().parse('xcrun version 48.\n')

		then:
		version.major == 48
		version.minor == 0
		version.micro == 0
		version.patch == 0
		version.qualifier == null
	}

	def "can parse output from Xcode 10.3"() {
		when:
		def version = XcrunLocator.asXcodeRunVersion().parse('xcrun version 43.1.\n')

		then:
		version.major == 43
		version.minor == 1
		version.micro == 0
		version.patch == 0
		version.qualifier == null
	}
}
