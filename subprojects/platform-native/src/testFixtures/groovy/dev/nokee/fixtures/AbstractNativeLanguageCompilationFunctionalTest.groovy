package dev.nokee.fixtures

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.sources.SourceElement
import org.hamcrest.Matchers
import spock.lang.Unroll

abstract class AbstractNativeLanguageCompilationFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	@Unroll
	def "build fails when compilation fails"(taskUnderTest) {
		given:
		makeSingleProject()

		and:
		file("src/main/c/broken.c") << "broken!"
		file("src/main/cpp/broken.cpp") << "broken!"
		file("src/main/objc/broken.m") << "broken!"
		file("src/main/objcpp/broken.mm") << "broken!"
		file("src/main/swift/broken.swift") << "broken!"

		expect:
		fails taskUnderTest
		failure.assertHasDescription("Execution failed for task '${tasks.compile}'.")
		failure.assertHasCause("A build operation failed.")
		failure.assertThatCause(Matchers.containsString(expectedCompilationFailureCause))

		where:
		taskUnderTest << ['assemble', 'objects', binaryLifecycleTaskName]
	}

	def "can assemble component"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('assemble')

		then:
		assertTasksExecutedAndNotSkipped(*tasks.allToAssemble)
	}

	protected abstract void makeSingleProject()

	protected abstract SourceElement getComponentUnderTest()

	protected abstract String getBinaryLifecycleTaskName()

	protected abstract String getExpectedCompilationFailureCause()
}
