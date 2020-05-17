package dev.nokee.platform.jni

import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.jni.fixtures.KotlinJniCppGreeterLib

class KotlinCppJniLibraryFunctionalTest extends AbstractJniLibraryFunctionalTest implements CppTaskNames {
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
		settingsFile << '''
			gradle.rootProject {
				repositories {
					mavenCentral()
				}
			}
		'''
	}

	private static String getKotlinVersion() {
		return '1.3.72'
	}

	@Override
	protected List<String> getExpectedClasses() {
		return ['com/example/greeter/Greeter.class', 'com/example/greeter/Greeter$Companion.class', 'com/example/greeter/NativeLoader.class', 'META-INF/jni-greeter.kotlin_module']
	}

	@Override
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

	@Override
	KotlinJniCppGreeterLib getComponentUnderTest() {
		return new KotlinJniCppGreeterLib('jni-greeter')
	}

	@Override
	protected String getJvmPluginId() {
		return 'org.jetbrains.kotlin.jvm'
	}

	@Override
	protected String getNativePluginId() {
		return 'dev.nokee.cpp-language'
	}
}
