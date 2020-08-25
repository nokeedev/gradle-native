package dev.nokee.language.swift.internal

import spock.lang.Specification
import spock.lang.Subject

@Subject(UTTypeSwiftSource)
class UTTypeSwiftSourceTest extends Specification {
	def "has the expected properties"() {
		def type = new UTTypeSwiftSource()
		expect:
		type.identifier == 'public.swift-source'
		type.filenameExtensions == ['swift'] as String[]
		type.displayName == 'Swift source code'
	}
}
