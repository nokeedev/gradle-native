package dev.nokee.language.c.internal

import spock.lang.Specification
import spock.lang.Subject

@Subject(UTTypeCHeader)
class UTTypeCHeaderTest extends Specification {
	def "has the expected properties"() {
		def type = new UTTypeCHeader()
		expect:
		type.identifier == 'public.c-header'
		type.filenameExtensions == ['h'] as String[]
		type.displayName == 'C header file'
	}
}
