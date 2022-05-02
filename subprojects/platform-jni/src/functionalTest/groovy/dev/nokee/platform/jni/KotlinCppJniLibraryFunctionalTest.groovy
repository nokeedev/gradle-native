/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
		return '1.6.21'
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
