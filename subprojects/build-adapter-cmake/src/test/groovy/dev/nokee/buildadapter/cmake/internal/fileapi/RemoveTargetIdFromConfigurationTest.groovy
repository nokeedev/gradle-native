package dev.nokee.buildadapter.cmake.internal.fileapi

import spock.lang.Specification
import spock.lang.Subject

@Subject(RemoveTargetIdFromConfiguration)
class RemoveTargetIdFromConfigurationTest extends Specification {
	def "can remove target id from configuration"() {
		given:
		def subject = new RemoveTargetIdFromConfiguration('foo::@c0ffeebabe')
		def configuration = new CodeModel.Configuration(
			'Debug',
			[new CodeModel.Configuration.Project('foo', [0, 1])],
			[
				new CodeModel.Configuration.TargetReference('foo-Debug-5555.json', 'foo', 'foo::@c0ffeebabe', 0),
				new CodeModel.Configuration.TargetReference('bar-Debug-5555.json', 'bar', 'bar::@deadbeef', 0)
			])

		when:
		def result = subject.apply(configuration)

		then:
		result.name == 'Debug'
		result.projects.size() == 1
		result.projects[0].name == 'foo'
		result.projects[0].targetIndexes as Set == [0] as Set
		result.targets.size() == 1
		result.targets[0].name == 'bar'
		result.targets[0].id == 'bar::@deadbeef'
		result.targets[0].jsonFile == 'bar-Debug-5555.json'
		result.targets[0].projectIndex == 0

	}
}
