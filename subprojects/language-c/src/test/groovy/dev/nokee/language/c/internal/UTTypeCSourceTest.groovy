package dev.nokee.language.c.internal

import spock.lang.Specification
import spock.lang.Subject

@Subject(UTTypeCSource)
class UTTypeCSourceTest extends Specification {
	def "has the expected properties"() {
		def type = new UTTypeCSource()
		expect:
		type.identifier == 'public.c-source'
		type.filenameExtensions == ['c'] as String[]
		type.displayName == 'C source code'
	}
}
