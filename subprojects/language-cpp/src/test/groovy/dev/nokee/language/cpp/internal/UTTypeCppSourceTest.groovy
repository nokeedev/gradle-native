package dev.nokee.language.cpp.internal

import dev.nokee.language.cpp.internal.UTTypeCppSource
import spock.lang.Specification
import spock.lang.Subject

@Subject(UTTypeCppSource)
class UTTypeCppSourceTest extends Specification {
	def "has the expected properties"() {
		def type = new UTTypeCppSource()
		expect:
		type.identifier == 'public.c-plus-plus-source'
		type.filenameExtensions == ['cp', 'cpp', 'c++', 'cc', 'cxx'] as String[]
		type.displayName == 'C++ source code'
	}
}
