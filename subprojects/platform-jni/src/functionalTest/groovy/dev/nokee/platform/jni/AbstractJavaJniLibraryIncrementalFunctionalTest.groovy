package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.gradleplugins.test.fixtures.archive.JarTestFixture
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.JavaJniCGreeterLib
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib
import dev.nokee.platform.jni.fixtures.JavaJniObjectiveCGreeterLib
import dev.nokee.platform.jni.fixtures.JavaJniObjectiveCppGreeterLib
import org.gradle.nativeplatform.OperatingSystemFamily
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import spock.lang.Requires
import spock.util.environment.OperatingSystem

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

class JavaCJniLibraryIncrementalFunctionalTest extends AbstractJavaJniLibraryIncrementalFunctionalTest {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
            plugins {
                id 'java'
                id 'dev.nokee.jni-library'
                id 'dev.nokee.c-language'
            }
        '''
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniCGreeterLib('jni-greeter')
	}
}

class JavaCppJniLibraryIncrementalFunctionalTest extends AbstractJavaJniLibraryIncrementalFunctionalTest {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
            plugins {
                id 'java'
                id 'dev.nokee.jni-library'
                id 'dev.nokee.cpp-language'
            }
        '''
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniCppGreeterLib('jni-greeter')
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class JavaObjectiveCJniLibraryIncrementalFunctionalTest extends AbstractJavaJniLibraryIncrementalFunctionalTest {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.objective-c-language'
			}

			library.variants.configureEach {
				sharedLibrary.linkTask.configure {
					linkerArgs.add('-lobjc')
				}
			}
		'''
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniObjectiveCGreeterLib('jni-greeter')
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class JavaObjectiveCppJniLibraryIncrementalFunctionalTest extends AbstractJavaJniLibraryIncrementalFunctionalTest {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.objective-cpp-language'
			}

			library.variants.configureEach {
				sharedLibrary.linkTask.configure {
					linkerArgs.add('-lobjc')
				}
			}
		'''
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniObjectiveCppGreeterLib('jni-greeter')
	}
}
