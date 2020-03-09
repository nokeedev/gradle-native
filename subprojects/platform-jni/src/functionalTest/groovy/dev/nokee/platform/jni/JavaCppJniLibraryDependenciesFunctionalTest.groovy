package dev.nokee.platform.jni


import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.archive.JarTestFixture
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib

class JavaCppJniLibraryDependenciesFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def "can define api dependencies on component"() {
		given:
		makeComponentWithLibraries()
		buildFile << """
            library {
                dependencies {
                    api project(':java-library')
                    nativeImplementation project(':cpp-library')
                }
            }
        """

		when:
		run('assemble')

		then:
		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', sharedLibraryName('jni-greeter'), sharedLibraryName('cpp-library'))
	}

	def "can define implementation dependencies on component"() {
		given:
		makeComponentWithLibraries()
		buildFile << """
            library {
                dependencies {
                    jvmImplementation project(':java-library')
                    nativeImplementation project(':cpp-library')
                }
            }
        """

		when:
		run('assemble')

		then:
		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', sharedLibraryName('jni-greeter'), sharedLibraryName('cpp-library'))
	}

	def "can define an included build api dependencies on component"() {
		given:
		makeComponentWithIncludedBuildLibraries()
		buildFile << """
            library {
                dependencies {
                    api 'com.example:java-library:4.2'
                    nativeImplementation 'com.example:cpp-library:4.2'
                }
            }
        """

		when:
		run('assemble')

		then:
		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', sharedLibraryName('jni-greeter'), sharedLibraryName('cpp-library'))
	}

	def "can define an included build implementation dependencies on component"() {
		given:
		makeComponentWithIncludedBuildLibraries()
		buildFile << """
            library {
                dependencies {
                    jvmImplementation 'com.example:java-library:4.2'
                    nativeImplementation 'com.example:cpp-library:4.2'
                }
            }
        """

		when:
		run('assemble')

		then:
		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', sharedLibraryName('jni-greeter'), sharedLibraryName('cpp-library'))
	}

//	def "can define implementation dependencies on each variant"() {
//		given:
//		makeComponentWithLibrary()
//		buildFile << """
//			${componentUnderTestDsl} {
//				variants.configureEach {
//					dependencies {
//                    	jvmImplementation project(':java-library')
//                    	nativeImplementation project(':cpp-library')
//					}
//				}
//			}
//		"""
//
//		when:
//		run('assemble')
//
//		then:
//		jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class', sharedLibraryName('jni-greeter'))
//	}

    protected void makeComponentWithLibraries() {
		settingsFile << '''
            rootProject.name = 'jni-greeter'
            include 'java-library'
            include 'cpp-library'
        '''
		buildFile << '''
            plugins {
                id 'java'
                id 'dev.nokee.jni-library'
                id 'dev.nokee.cpp-language'
            }
        '''
		file('java-library/build.gradle') << '''
            plugins {
                id 'java-library'
            }
        '''
		file('cpp-library/build.gradle') << '''
            plugins {
                id 'cpp-library'
            }
        '''

		componentsUnderTest.jvmImplementation.writeToProject(file('java-library'))
		componentsUnderTest.nativeImplementation.asLib().writeToProject(file('cpp-library'))
		componentsUnderTest.nativeBindings.writeToProject(testDirectory)
		componentsUnderTest.jvmBindings.writeToProject(testDirectory)
    }

    protected void makeComponentWithIncludedBuildLibraries() {
		settingsFile << '''
            rootProject.name = 'jni-greeter'
            includeBuild 'java-library'
            includeBuild 'cpp-library'
        '''
		buildFile << '''
            plugins {
                id 'java'
                id 'dev.nokee.jni-library'
                id 'dev.nokee.cpp-language'
            }
        '''
		file('java-library/settings.gradle') << "rootProject.name = 'java-library'"
		file('java-library/build.gradle') << '''
            plugins {
                id 'java-library'
            }

            group = 'com.example'
            version = '4.2'
        '''
		file('cpp-library/settings.gradle') << "rootProject.name = 'cpp-library'"
		file('cpp-library/build.gradle') << '''
            plugins {
                id 'cpp-library'
            }

            group = 'com.example'
            version = '4.2'
        '''

		componentsUnderTest.jvmImplementation.writeToProject(file('java-library'))
		componentsUnderTest.nativeImplementation.asLib().writeToProject(file('cpp-library'))
		componentsUnderTest.nativeBindings.writeToProject(testDirectory)
		componentsUnderTest.jvmBindings.writeToProject(testDirectory)
    }

	protected JavaJniCppGreeterLib getComponentsUnderTest() {
		return new JavaJniCppGreeterLib('jni-greeter')
	}

	protected JarTestFixture jar(String path) {
		return new JarTestFixture(file(path))
	}

	def "can depends on C++ static library project"() {
		settingsFile << '''
			rootProject.name = 'jni-greeter'
			include 'cpp-greeter'
		'''
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}

			repositories {
                mavenCentral()
            }

			library {
				dependencies {
					nativeImplementation project(':cpp-greeter')
				}
			}
		'''
		file('cpp-greeter/build.gradle') << '''
			plugins {
				id 'cpp-library'
			}

			library.linkage.set([Linkage.STATIC])
		'''
		def fixture = new  JavaJniCppGreeterLib('jni-greeter')
		fixture.withoutNativeImplementation().writeToProject(testDirectory)
		fixture.nativeImplementation.asLib().writeToProject(file('cpp-greeter'))

		when:
		succeeds('assemble')

		then:
		jar('build/libs/jni-greeter.jar').hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class', sharedLibraryName('jni-greeter'))
		result.assertTaskNotExecuted(':cpp-greeter:compileReleaseCpp')
		result.assertTaskNotExecuted(':cpp-greeter:createRelease')
	}

	def "can depends on C++ shared library project"() {
		settingsFile << '''
			rootProject.name = 'jni-greeter'
			include 'cpp-greeter'
		'''
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}

			repositories {
                mavenCentral()
            }

			library {
				dependencies {
					nativeImplementation project(':cpp-greeter')
				}
			}
		'''
		file('cpp-greeter/build.gradle') << '''
			plugins {
				id 'cpp-library'
			}
		'''
		def fixture = new  JavaJniCppGreeterLib('jni-greeter')
		fixture.withoutNativeImplementation().writeToProject(testDirectory)
		fixture.nativeImplementation.asLib().writeToProject(file('cpp-greeter'))

		when:
		succeeds('assemble')

		then:
		jar('build/libs/jni-greeter.jar').hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class', sharedLibraryName('cpp-greeter'), sharedLibraryName('jni-greeter'))
		result.assertTaskNotExecuted(':cpp-greeter:compileReleaseCpp')
		result.assertTaskNotExecuted(':cpp-greeter:linkRelease')
	}
}
