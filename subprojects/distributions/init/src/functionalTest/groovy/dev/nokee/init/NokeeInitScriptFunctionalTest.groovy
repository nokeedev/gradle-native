package dev.nokee.init

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification

class NokeeInitScriptFunctionalTest extends AbstractGradleSpecification {
	static File getInitScriptUnderTest() {
		return new File(System.getProperty('dev.nokee.initScriptUnderTest'))
	}

	def "does not use Nokee unless marker file present"() {
		given:
		usingInitScript(initScriptUnderTest)

		when:
		succeeds('help')
		then:
		result.assertNotOutput("Build ':' use Nokee version")

		when:
		file('.gradle/using-nokee-version.txt').text = '0.4.0'
		succeeds('help')
		then:
		result.assertOutputContains("Build ':' use Nokee version '0.4.0'.")
	}
}
