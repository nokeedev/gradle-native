package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.nokee.platform.jni.fixtures.KotlinJniCppGreeterLib
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement

class KotlinCppJniLibraryWithJUnitTestingFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def setup() {
		settingsFile << """
			pluginManagement {
				repositories {
					gradlePluginPortal()
					mavenCentral()
				}

				resolutionStrategy {
					eachPlugin {
						if (requested.id.id == 'org.jetbrains.kotlin.jvm') {
							useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
						}
					}
				}
			}
		"""
	}

	def "can test library using JUnit"() {
		makeSingleProject()
		buildFile << '''
			repositories {
				mavenCentral()
			}

			dependencies {
				testImplementation 'org.jetbrains.kotlin:kotlin-test'
				testImplementation 'org.jetbrains.kotlin:kotlin-test-junit'
			}
		'''
		componentUnderTest.writeToProject(testDirectory)

		expect:
		succeeds('test')
		// TODO: assert JUnit result
	}

	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'org.jetbrains.kotlin.jvm'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}

			repositories {
				mavenCentral()
			}

			dependencies {
				implementation platform('org.jetbrains.kotlin:kotlin-bom:${kotlinVersion}')
				implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk8'
			}
		"""
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	JniLibraryElement getComponentUnderTest() {
		return new KotlinJniCppGreeterLib('jni-greeter').withJUnitTest()
	}

	private static String getKotlinVersion() {
		return '1.3.72'
	}
}
