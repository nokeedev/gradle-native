package dev.nokee.fixtures

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.language.NativeProjectTasks
import org.hamcrest.Matchers
import spock.lang.Unroll

import static org.junit.Assume.assumeTrue

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
		failure.assertHasDescription("Execution failed for task '${taskNamesUnderTest.compile}'.")
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
		assertTasksExecutedAndNotSkipped(*taskNamesUnderTest.allToLifecycleAssemble)
	}

	// TODO: Variant-aware configuration
	def "can assemble variant component"() {
		assumeTrue(isTargetMachineAwareConfiguration())

		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << """
			${componentUnderTestDsl} {
				targetMachines = [machines.macOS, machines.linux, machines.windows]
			}
		"""

		when:
		succeeds(taskNamesUnderTest.withOperatingSystemFamily(currentOsFamilyName).assemble)

		then:
		assertTasksExecutedAndNotSkipped(*taskNamesUnderTest.withOperatingSystemFamily(currentOsFamilyName).allToAssemble)
	}

	protected abstract void makeSingleProject()

	protected abstract SourceElement getComponentUnderTest()

	protected abstract String getBinaryLifecycleTaskName()

	protected String getExpectedCompilationFailureCause() {
		String className = this.getClass().simpleName
		if (className.contains('ObjectiveCppLanguage') || className.startsWith('ObjectiveCpp')) {
			return 'Objective-C++ compiler failed while compiling broken.mm'
		} else if (className.contains('ObjectiveCLanguage') || className.startsWith('ObjectiveC')) {
			return 'Objective-C compiler failed while compiling broken.m'
		} else if (className.contains('CppLanguage') || className.startsWith('Cpp')) {
			return 'C++ compiler failed while compiling broken.cpp'
		} else if (className.contains('CLanguage') || className.startsWith('C')) {
			return 'C compiler failed while compiling broken.c'
		} else if (className.contains('SwiftLanguage') || className.startsWith('Swift')) {
			return 'Swift compiler failed while compiling swift file(s)'
		}
		throw new IllegalArgumentException('Unable to figure out expected compilation failure cause')
	}

	protected String getComponentUnderTestDsl() {
		if (this.getClass().simpleName.contains("Library")) {
			return 'library'
		} else if (this.getClass().simpleName.contains("Application")) {
			return 'application'
		}
		throw new IllegalArgumentException('Unable to figure out component under test DSL name')
	}

	protected boolean isTargetMachineAwareConfiguration() {
		return true
	}

	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks
	}
}
