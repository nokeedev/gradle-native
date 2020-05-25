package dev.nokee.platform.jni

import dev.gradleplugins.test.fixtures.archive.JarTestFixture
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.language.MixedLanguageTaskNames
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib
import dev.nokee.platform.nativebase.SharedLibraryBinary
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
                    ${componentUnderTestDsl}.variants.elements.get().each {
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
			":compileX86-64Cpp", ":linkX86-64",
			':compileJava', ':jarX86-64')
		jar('build/libs/jni-greeter-x86-64.jar').hasDescendants(sharedLibraryName('x86-64/cpp-library'), sharedLibraryName('x86-64/jni-greeter'))

		when:
		succeeds(':jar')
		then:
		result.assertTasksExecuted(':classes', ':processResources', ':compileJava', ':jar')
		jar('build/libs/jni-greeter.jar').hasDescendants('com/example/greeter/NativeLoader.class', 'com/example/greeter/Greeter.class')
	}

	def "can query the binaries for multi-variant library"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << configureTargetMachines('machines.macOS', 'machines.windows', 'machines.linux')
		buildFile << """
			import ${JarBinary.canonicalName}
			import ${JniJarBinary.canonicalName}
			import ${JvmJarBinary.canonicalName}
			import ${SharedLibraryBinary.canonicalName}

			tasks.register('verify') {
				doLast {
					// All variants are created
					def variants = library.variants.elements
					assert variants.get().size() == 3

					// Each variants has the expected binaries
					variants.get().each { variant ->
						def binaries = variant.binaries.elements
						assert binaries.get().size() == 3
						assert binaries.get().count { it instanceof ${JarBinary.simpleName} } == 2
						assert binaries.get().count { it instanceof ${JniJarBinary.simpleName} } == 1
						assert binaries.get().count { it instanceof ${JvmJarBinary.simpleName} } == 1
						assert binaries.get().count { it instanceof ${SharedLibraryBinary.simpleName} } == 1
					}

					// All the binaries count, a shared library and JNI JAR for each variant and a single JVM JAR shared between all variants
					def allBinaries = library.binaries.elements
					assert allBinaries.get().size() == 7
					assert allBinaries.get().count { it instanceof ${JarBinary.simpleName} } == 4
					assert allBinaries.get().count { it instanceof ${JniJarBinary.simpleName} } == 3
					assert allBinaries.get().count { it instanceof ${JvmJarBinary.simpleName} } == 1
					assert allBinaries.get().count { it instanceof ${SharedLibraryBinary.simpleName} } == 3
				}
			}
		"""

		expect:
		succeeds('verify')
	}

	def "can build the JVM JAR but not the JNI JAR when all variants are unbuildable"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << configureToolChainSupport('foo', currentArchitecture)
		buildFile << configureToolChainSupport('bar', currentArchitecture)
		buildFile << configureTargetMachines('machines.os("foo")', 'machines.os("bar")')

		when:
		fails(':jarFoo')
		then:
		result.assertTasksExecutedAndNotSkipped(':compileJava', ':compileFooCpp')

		when:
		succeeds(':jar')
		then:
		result.assertTasksExecuted(':classes', ':processResources', ':compileJava', ':jar')
		jar('build/libs/jni-greeter.jar').hasDescendants('com/example/greeter/NativeLoader.class', 'com/example/greeter/Greeter.class')
	}

	def "resolve variants when all the binaries are queried"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << configureTargetMachines('machines.macOS', 'machines.windows', 'machines.linux')
		buildFile << """
			import ${JarBinary.canonicalName}
			import ${JniJarBinary.canonicalName}
			import ${JvmJarBinary.canonicalName}
			import ${SharedLibraryBinary.canonicalName}

			def configuredVariants = []
			library {
				variants.configureEach { variant ->
					configuredVariants << variant
				}
			}

			tasks.register('verify') {
				doLast {
					def allBinaries = library.binaries.elements
					assert allBinaries.get().size() == 7
					assert allBinaries.get().count { it instanceof ${JarBinary.simpleName} } == 4
					assert allBinaries.get().count { it instanceof ${JniJarBinary.simpleName} } == 3
					assert allBinaries.get().count { it instanceof ${JvmJarBinary.simpleName} } == 1
					assert allBinaries.get().count { it instanceof ${SharedLibraryBinary.simpleName} } == 3

					assert configuredVariants.size() == 3
				}
			}
		"""

		expect:
		succeeds('verify')
	}

	def "resolve variants when a subset of binaries are queried"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << configureTargetMachines('machines.macOS', 'machines.windows', 'machines.linux')
		buildFile << """
			import ${JarBinary.canonicalName}
			import ${JniJarBinary.canonicalName}
			import ${JvmJarBinary.canonicalName}
			import ${SharedLibraryBinary.canonicalName}

			def configuredVariants = []
			library {
				variants.configureEach { variant ->
					configuredVariants << variant
				}
			}

			tasks.register('verify') {
				doLast {
					def allBinaries = library.binaries.withType(${JarBinary.simpleName}).elements
					assert allBinaries.get().size() == 4
					assert allBinaries.get().count { it instanceof ${JarBinary.simpleName} } == 4
					assert allBinaries.get().count { it instanceof ${JniJarBinary.simpleName} } == 3
					assert allBinaries.get().count { it instanceof ${JvmJarBinary.simpleName} } == 1
					assert allBinaries.get().count { it instanceof ${SharedLibraryBinary.simpleName} } == 0

					assert configuredVariants.size() == 3
				}
			}
		"""

		expect:
		succeeds('verify')
	}

	def "can check if a binary is buildable"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureTargetMachines("machines.linux", "machines.macOS", "machines.windows", "machines.freeBSD")
		buildFile << """
			tasks.register('verify') {
				doLast {
					def variants = library.variants.elements.get()
					def buildable = variants*.sharedLibrary*.buildable
					assert buildable.count { it == true } == 1
					assert buildable.count { it == false } == 3
				}
			}
		"""

		expect:
		succeeds 'verify'
	}

	protected String configureProjectGroup(String groupId) {
		return """
			group = '${groupId}'
		"""
	}
}
