package dev.nokee.language.objectivec.internal

import spock.lang.Specification
import spock.lang.Subject

@Subject(UTTypeObjectiveCSource)
class UTTypeObjectiveCSourceTest extends Specification{
	def "has the expected properties"() {
		def type = new UTTypeObjectiveCSource()
		expect:
		type.identifier == 'public.objective-c-source'
		type.filenameExtensions == ['m'] as String[]
		type.displayName == 'Objective-C source code'
	}
}
