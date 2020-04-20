package dev.nokee.platform.jni

import dev.gradleplugins.test.fixtures.archive.JarTestFixture
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.language.MixedLanguageTaskNames
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib
import dev.nokee.platform.nativebase.internal.DefaultMachineArchitecture
import dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily

class JniLibraryTargetMachinesFunctionalTest extends AbstractTargetMachinesFunctionalTest implements MixedLanguageTaskNames {
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
	protected String getComponentUnderTestDsl() {
		return 'library'
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniCppGreeterLib('jni-greeter')
	}

	@Override
	protected String getTaskNameToAssembleDevelopmentBinary() {
		return 'assemble'
	}

	@Override
	protected List<String> getTasksToAssembleDevelopmentBinary() {
		return taskNames.java.tasks.allToAssemble + taskNames.cpp.tasks.allToAssemble
	}

	@Override
	protected String getTaskNameToAssembleDevelopmentBinaryWithArchitecture(String architecture) {
		return "assemble${architecture.capitalize()}"
	}

	@Override
	protected String getComponentName() {
		return 'main'
	}

	@Override
	protected String getProjectName() {
		return 'jni-greeter'
	}

	@Override
	protected void assertComponentUnderTestWasBuilt(String variant) {
		if (variant.isEmpty()) {
			jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class', sharedLibraryName('jni-greeter'))
		} else {
			jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class')
			jar("build/libs/jni-greeter-${variant}.jar").hasDescendants(sharedLibraryName("${variant}/jni-greeter"))
		}
	}

	protected JarTestFixture jar(String path) {
		return new JarTestFixture(file(path))
	}

	def "uses group and visible dimensions as resource path for the shared library inside JAR"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureTargetMachines("machines.linux", "machines.macOS", "machines.windows", "machines.freeBSD")
		buildFile << configureProjectGroup('com.example.greeter')

		expect:
		succeeds 'assemble'
		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class')
		jar("build/libs/jni-greeter-${currentOsFamilyName}.jar").hasDescendants(sharedLibraryName("com/example/greeter/${currentOsFamilyName}/jni-greeter"))
	}

	def "can configure the resource path for each variant from target machine"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureTargetMachines("machines.linux", "machines.macOS", "machines.windows", "machines.freeBSD")
		buildFile << """
			library {
				variants.configureEach {
					String osName
					if (targetMachine.operatingSystemFamily.windows) {
						osName = 'windows'
					} else if (targetMachine.operatingSystemFamily.linux) {
						osName = 'linux'
					} else if (targetMachine.operatingSystemFamily.macOS) {
						osName = 'macos'
					} else if (targetMachine.operatingSystemFamily.freeBSD) {
						osName = 'freebsd'
					} else {
						throw new GradleException('Unknown operating system family')
					}

					String architectureName
					if (targetMachine.architecture.is32Bit()) {
						architectureName = 'x86'
					} else if (targetMachine.architecture.is64Bit()) {
						architectureName = 'x86-64'
					} else {
						throw new GradleException('Unknown architecture')
					}
					resourcePath = "com/example/foobar/\${architectureName}-\${osName}"
				}
			}
		"""

		expect:
		succeeds 'assemble'
		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class')
		jar("build/libs/jni-greeter-${currentOsFamilyName}.jar").hasDescendants(sharedLibraryName("com/example/foobar/${currentArchitecture}-${currentOsFamilyName}/jni-greeter"))
	}

	def "variants have the right platform type"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << """
            task verifyBinariesPlatformType {
                doLast {
                    ${componentUnderTestDsl}.variantCollection.each {
                        assert it.targetMachine.operatingSystemFamily == ${DefaultOperatingSystemFamily.canonicalName}.forName('${currentOsFamilyName}')
                        assert it.targetMachine.architecture == ${DefaultMachineArchitecture.canonicalName}.forName('${currentArchitecture}')
                    }
                }
            }
        """

		expect:
		succeeds 'verifyBinariesPlatformType'
	}

	def "can link against the right library variant from dependency"() {
		makeSingleProject()
		def fixture = new JavaJniCppGreeterLib('jni-greeter')
		fixture.withoutNativeImplementation().writeToProject(testDirectory)
		fixture.nativeImplementation.asLib().writeToProject(file('cpp-library'))
		file('cpp-library/build.gradle') << """
			plugins {
				id 'cpp-library'
			}

			library.targetMachines = [machines.${currentHostOperatingSystemFamilyDsl}.x86, machines.${currentHostOperatingSystemFamilyDsl}.x86_64]
		"""
		settingsFile << '''
			include 'cpp-library'
		'''
		buildFile << """
			library {
				targetMachines = [machines.${currentHostOperatingSystemFamilyDsl}.x86, machines.${currentHostOperatingSystemFamilyDsl}.x86_64]
				dependencies {
					nativeImplementation project(':cpp-library')
				}
			}
		"""

		when:
		succeeds(':jarX86-64')

		then:
		result.assertTasksExecutedAndNotSkipped(':cpp-library:compileDebugX86-64Cpp', ':cpp-library:linkDebugX86-64',
			":compileMain${currentOsFamilyName.capitalize()}x86-64SharedLibraryMainCpp", ":linkMain${currentOsFamilyName.capitalize()}x86-64SharedLibrary",
			':compileJava', ':jarX86-64')
	}

	protected String configureProjectGroup(String groupId) {
		return """
			group = '${groupId}'
		"""
	}
}
