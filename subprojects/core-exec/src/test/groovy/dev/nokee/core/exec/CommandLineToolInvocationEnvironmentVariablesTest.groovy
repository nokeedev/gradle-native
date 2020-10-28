package dev.nokee.core.exec

import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.empty
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.from
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.inherit

@Subject(CommandLineToolInvocationEnvironmentVariables)
class CommandLineToolInvocationEnvironmentVariablesTest extends Specification {
	def "can create empty environment variables"() {
		expect:
		empty() == from([:])
		empty() == from([])

		and:
		empty().asMap == [:]
		empty().asList == []
		empty().plus(from([A: 'a'])) == from([A: 'a'])
	}

	def "can create environment variables from current process"() {
		expect:
		inherit() == from(System.getenv())

		and:
		inherit().asMap == System.getenv()
		inherit().asList == System.getenv().collect { k, v -> "$k=$v" }
		inherit().plus(from([A: 'a'])) == from(System.getenv() + [A: 'a'])
	}

	def "can create environment variables from list"() {
		expect:
		from(['A=a', 'B=b']) == from([A: 'a', B: 'b'])

		and:
		from(['A=a', 'B=b']).asMap == [A: 'a', B: 'b']
		from(['A=a', 'B=b']).asList == ['A=a', 'B=b']
		from(['A=a', 'B=b']).plus(from([C: 'c'])) == from([A: 'a', B: 'b', C: 'c'])
	}

	def "can create environment variables from properties file"() {
		expect:
		from(propertiesFile([A: 'a', B: 'b'])) == from([A: 'a', B: 'b'])

		and:
		from(propertiesFile([A: 'a', B: 'b'])).asMap == [A: 'a', B: 'b']
		from(propertiesFile([A: 'a', B: 'b'])).asList == ['A=a', 'B=b']
		from(propertiesFile([A: 'a', B: 'b'])).plus(from([C: 'c'])) == from([A: 'a', B: 'b', C: 'c'])
	}

	static File propertiesFile(Map<String, String> values) {
		def result = File.createTempFile('test', 'properties')
		result.text = values.collect { k, v -> "$k=$v" }.join('\n')
		return result
	}

	def "can create environment variables from properties"() {
		expect:
		from(properties([A: 'a', B: 'b'])) == from([A: 'a', B: 'b'])

		and:
		from(properties([A: 'a', B: 'b'])).asMap == [A: 'a', B: 'b']
		from(properties([A: 'a', B: 'b'])).asList == ['A=a', 'B=b']
		from(properties([A: 'a', B: 'b'])).plus(from([C: 'c'])) == from([A: 'a', B: 'b', C: 'c'])
	}

	static Properties properties(Map<String, String> values) {
		def result = new Properties()
		result.putAll(values)
		return result
	}
}
