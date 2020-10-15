package dev.nokee.language.cpp.internal

import spock.lang.Specification
import spock.lang.Subject

@Subject(UTTypeCppHeader)
class UTTypeCppHeaderTest extends Specification {
	def "has the expected properties"() {
		def type = new UTTypeCppHeader()
		expect:
		type.identifier == 'public.c-plus-plus-header'
		type.filenameExtensions == ['hpp', 'h++', 'hxx'] as String[]
		type.displayName == 'C++ header file'
	}
}
