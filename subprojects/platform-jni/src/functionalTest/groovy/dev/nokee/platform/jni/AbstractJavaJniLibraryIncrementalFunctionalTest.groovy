package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.archive.JarTestFixture
import dev.gradleplugins.test.fixtures.sources.SourceElement
import org.gradle.nativeplatform.OperatingSystemFamily
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

abstract class AbstractJavaJniLibraryIncrementalFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def "recreate JAR when group change"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('assemble')
		then:
		result.assertTaskNotSkipped(':jar')
		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class', sharedLibraryName('jni-greeter'))

		when:
		succeeds('assemble')
		then:
		result.assertTaskSkipped(':jar')

		when:
		buildFile << configureProjectGroup('com.example.greeter')
		succeeds('assemble')
		then:
		result.assertTaskNotSkipped(':jar')
		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class', sharedLibraryName('com/example/greeter/jni-greeter'))
	}

	def "recreate JAR when dimension change (e.g. target machine)"() {
		makeSingleProject()
		buildFile << configureProjectGroup('com.example.greeter')
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('assemble')
		then:
		result.assertTaskNotSkipped(':jar')
		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class', sharedLibraryName('com/example/greeter/jni-greeter'))

		when:
		succeeds('assemble')
		then:
		result.assertTaskSkipped(':jar')

		when:
		buildFile << configureTargetMachines('machines.linux', 'machines.macOS', 'machines.windows')
		succeeds('assemble')
		then:
		result.assertTaskNotSkipped(':jar')
		result.assertTaskNotSkipped(":jar${currentOsFamilyName.capitalize()}")
		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class')
		jar("build/libs/jni-greeter-${currentOsFamilyName}.jar").hasDescendants(sharedLibraryName("com/example/greeter/${currentOsFamilyName}/jni-greeter"))
	}

	def "skips JAR when dimension does not change (e.g. target machine)"() {
		makeSingleProject()
		buildFile << configureProjectGroup('com.example.greeter')
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('assemble')
		then:
		result.assertTaskNotSkipped(':jar')
		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class', sharedLibraryName('com/example/greeter/jni-greeter'))

		when:
		succeeds('assemble')
		then:
		result.assertTaskSkipped(':jar')

		when:
		buildFile << configureTargetMachines("machines.${currentHostOperatingSystemFamilyDsl}.x86_64")
		succeeds('assemble')
		then:
		result.assertTaskSkipped(':jar')
		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class', sharedLibraryName('com/example/greeter/jni-greeter'))
	}

	protected JarTestFixture jar(String path) {
		return new JarTestFixture(file(path))
	}

	protected String configureProjectGroup(String groupId) {
		return """
			group = '${groupId}'
		"""
	}

	protected configureTargetMachines(String... targetMachines) {
		return """
            ${componentUnderTestDsl} {
                targetMachines = [${targetMachines.join(",")}]
            }
        """
	}

	protected String getComponentUnderTestDsl() {
		return 'library'
	}

	protected String getCurrentHostOperatingSystemFamilyDsl() {
		String osFamily = DefaultNativePlatform.getCurrentOperatingSystem().toFamilyName()
		if (osFamily == OperatingSystemFamily.MACOS) {
			return "macOS"
		} else {
			return osFamily
		}
	}

	protected abstract void makeSingleProject()

	protected abstract SourceElement getComponentUnderTest()
}
