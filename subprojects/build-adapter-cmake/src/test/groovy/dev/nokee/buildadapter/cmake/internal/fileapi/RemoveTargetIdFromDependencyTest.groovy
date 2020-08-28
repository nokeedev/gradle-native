package dev.nokee.buildadapter.cmake.internal.fileapi

import spock.lang.Specification
import spock.lang.Subject

@Subject(RemoveTargetIdFromDependency)
class RemoveTargetIdFromDependencyTest extends Specification {
	def "can remove target id from target's dependency"() {
		given:
		def subject = new RemoveTargetIdFromDependency('foo::@c0ffeebabe')
		def target = CodeModelTarget.builder()
			.id('bar::@deadbeef')
			.name('bar')
			.type('STATIC_LIBRARY')
			.dependencies([new CodeModelTarget.Dependency('foo::@c0ffeebabe'), new CodeModelTarget.Dependency('far::@facade')])
			.build()

		when:
		def result = subject.apply(target)

		then:
		result.dependencies.size() == 1
		result.dependencies[0].id == 'far::@facade'

		and:
		result.id == 'bar::@deadbeef'
		result.name == 'bar'
		result.type == 'STATIC_LIBRARY'
		!result.generatorProvided
		result.artifacts == []
		result.compileGroups == []
		result.sources == []
	}

	def "does not modify target if no dependencies to remove"() {
		given:
		def subject = new RemoveTargetIdFromDependency('foo::@c0ffeebabe')
		def target = CodeModelTarget.builder()
			.id('bar::@deadbeef')
			.name('bar')
			.type('STATIC_LIBRARY')
			.dependencies([new CodeModelTarget.Dependency('far::@facade')])
			.build()

		when:
		def result = subject.apply(target)

		then:
		result == target
	}

	def "can remove last dependency from target"() {
		given:
		def subject = new RemoveTargetIdFromDependency('foo::@c0ffeebabe')
		def target = CodeModelTarget.builder()
			.id('bar::@deadbeef')
			.name('bar')
			.type('STATIC_LIBRARY')
			.dependencies([new CodeModelTarget.Dependency('foo::@c0ffeebabe')])
			.build()

		when:
		def result = subject.apply(target)

		then:
		result.dependencies.size() == 0

		and:
		result.id == 'bar::@deadbeef'
		result.name == 'bar'
		result.type == 'STATIC_LIBRARY'
		!result.generatorProvided
		result.artifacts == []
		result.compileGroups == []
		result.sources == []
	}
}
