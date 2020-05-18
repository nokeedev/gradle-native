package dev.nokee.language.objectivecpp.internal

import spock.lang.Specification
import spock.lang.Subject

@Subject(UTTypeObjectiveCppSource)
class UTTypeObjectiveCppSourceTest extends Specification {
	def "has the expected properties"() {
		def type = new UTTypeObjectiveCppSource()
		expect:
		type.identifier == 'public.objective-c-plus-plus-source'
		type.filenameExtensions == ['mm'] as String[]
		type.displayName == 'Objective-C++ source code'
	}
}
