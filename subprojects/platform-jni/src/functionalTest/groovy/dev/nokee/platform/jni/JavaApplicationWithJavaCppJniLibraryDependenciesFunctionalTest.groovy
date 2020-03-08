package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
import dev.nokee.platform.jni.fixtures.GreeterAppWithJniLibrary

class JavaApplicationWithJavaCppJniLibraryDependenciesFunctionalTest extends AbstractFunctionalSpec  {
    private void makeComponentWithLibrary() {
		settingsFile << '''
            rootProject.name = 'app'
            include 'jni-library'
        '''
		buildFile << '''
            plugins {
                id 'java'
                id 'application'
            }

            application {
                mainClassName = 'com.example.app.Main'
            }
        '''
		file('jni-library/build.gradle') << '''
            plugins {
                id 'java'
                id 'dev.nokee.jni-library'
                id 'dev.nokee.cpp-language'
            }
        '''

		componentsUnderTest.library.writeToProject(testDirectory.file('jni-library'))
		componentsUnderTest.application.writeToProject(testDirectory)
	}

	private void makeComponentWithLibraries() {
		settingsFile << '''
            rootProject.name = 'app'
            include 'jni-library'
            include 'java-library'
            include 'cpp-library'
        '''
		buildFile << '''
            plugins {
                id 'java'
                id 'application'
            }

            application {
                mainClassName = 'com.example.app.Main'
            }
        '''
		file('jni-library/build.gradle') << '''
            plugins {
                id 'java'
                id 'dev.nokee.jni-library'
                id 'dev.nokee.cpp-language'
            }

            library {
            	dependencies {
            		jvmImplementation project(':java-library')
            		nativeImplementation project(':cpp-library')
				}
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

		[componentsUnderTest.library.jvmBindings, componentsUnderTest.library.nativeBindings]*.writeToProject(file('jni-library'))
		componentsUnderTest.library.jvmImplementation.writeToProject(file('java-library'))
		componentsUnderTest.library.nativeImplementation.asLib().writeToProject(file('cpp-library'))
		componentsUnderTest.application.writeToProject(testDirectory)
	}

    private void makeComponentWithIncludedBuildLibrary() {
        settingsFile << '''
            rootProject.name = 'application'
            includeBuild 'jni-library'
        '''
        buildFile << '''
            plugins {
                id 'java'
                id 'application'
            }

            application {
                mainClassName = 'com.example.app.Main'
            }
        '''

        file('jni-library/settings.gradle') << "rootProject.name = 'jni-library'"
        file('jni-library/build.gradle') << '''
            plugins {
                id 'java'
                id 'dev.nokee.jni-library'
                id 'dev.nokee.cpp-language'
            }

            group = 'com.example'
            version = '4.2'
        '''

        componentsUnderTest.library.writeToProject(testDirectory.file('jni-library'))
        componentsUnderTest.application.writeToProject(testDirectory)
    }

	private void makeComponentWithIncludedBuildLibraries() {
		settingsFile << '''
            rootProject.name = 'app'
            includeBuild 'jni-library'
            includeBuild 'java-library'
            includeBuild 'cpp-library'
        '''
		buildFile << '''
            plugins {
                id 'java'
                id 'application'
            }

            application {
                mainClassName = 'com.example.app.Main'
            }
        '''
		file('jni-library/settings.gradle') << "rootProject.name = 'jni-library'"
		file('jni-library/build.gradle') << '''
            plugins {
                id 'java'
                id 'dev.nokee.jni-library'
                id 'dev.nokee.cpp-language'
            }

            group = 'com.example'
            version = '4.2'

            library {
            	dependencies {
            		jvmImplementation 'com.example:java-library:4.2'
            		nativeImplementation 'com.example:cpp-library:4.2'
				}
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

		[componentsUnderTest.library.jvmBindings, componentsUnderTest.library.nativeBindings]*.writeToProject(file('jni-library'))
		componentsUnderTest.library.jvmImplementation.writeToProject(file('java-library'))
		componentsUnderTest.library.nativeImplementation.asLib().writeToProject(file('cpp-library'))
		componentsUnderTest.application.writeToProject(testDirectory)
	}

    private GreeterAppWithJniLibrary getComponentsUnderTest() {
        return new GreeterAppWithJniLibrary('jni-library')
    }

    def "can define implementation dependencies on component"() {
        makeComponentWithLibrary()
		buildFile << '''
			dependencies {
                implementation project(':jni-library')
            }
		'''

        when:
        run('run')

        then:
        result.assertOutputContains(componentsUnderTest.expectedOutput)
    }

    def "can define an included build implementation dependencies on component"() {
        makeComponentWithIncludedBuildLibrary()
		buildFile << '''
			dependencies {
                implementation 'com.example:jni-library:4.2'
            }
		'''

        when:
        run('run')

        then:
        result.assertOutputContains(componentsUnderTest.expectedOutput)
    }

	def "can consume transitive dependencies on component"() {
		given:
		makeComponentWithLibraries()
		buildFile << '''
			dependencies {
                implementation project(':jni-library')
            }
		'''

		when:
		run('run')

		then:
		result.assertOutputContains(componentsUnderTest.expectedOutput)
	}

	def "can consume an included build transitive dependencies on component"() {
		makeComponentWithIncludedBuildLibraries()
		buildFile << '''
			dependencies {
                implementation 'com.example:jni-library:4.2'
            }
		'''

		when:
		run('run')

		then:
		result.assertOutputContains(componentsUnderTest.expectedOutput)
	}
}
