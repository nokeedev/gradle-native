/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.platform.jni.dependencies

import spock.lang.Ignore

abstract class AbstractNokeeConfigurationFunctionalTest extends AbstractConfigurationFunctionalTest {

	protected abstract void reportDependencies();

	protected abstract void assertStaticVariantSelectedMatchingBuildTypes(BuildType... producer)
	protected abstract void assertSharedVariantSelectedMatchingBuildTypes(BuildType... producer)
	protected abstract void assertFrameworkVariantSelectedMatchingBuildTypes(BuildType... producer)

	protected abstract void assertFrameworkVariantSelected()

	protected void makeStaticLibraryProducerProject(BuildType... buildTypes = [DEFAULT]) {
		producerBuildFile << """
			configurations {
				createIfAbsent('api', configure.asBucket())
				createIfAbsent('implementation', configure.asBucket(api))
				createIfAbsent('headerSearchPathElements', configure.asOutgoingHeaderSearchPathFrom(implementation).headerDirectoryArtifact(file('includes')))
			}
		"""
		file("producer/includes").mkdirs()

		buildTypes.each { buildType ->
			producerBuildFile << """
				configurations {
					create('static${buildType.name.capitalize()}LinkLibraryElements', configure.asOutgoingLinkLibrariesFrom(implementation)${buildType.builderMethod}.withStaticLinkage().staticLibraryArtifact(file('${buildType.path}${staticLibraryPath}')))
					create('static${buildType.name.capitalize()}RuntimeLibraryElements', configure.asOutgoingRuntimeLibrariesFrom(implementation)${buildType.builderMethod}.withStaticLinkage())
				}
			"""
			file("producer/${buildType.path}${staticLibraryPath}").createFile()
		}
	}

	protected void makeSharedLibraryProducerProject(BuildType... buildTypes = [DEFAULT]) {
		producerBuildFile << """
			configurations {
				createIfAbsent('api', configure.asBucket())
				createIfAbsent('implementation', configure.asBucket(api))
				createIfAbsent('headerSearchPathElements', configure.asOutgoingHeaderSearchPathFrom(implementation).headerDirectoryArtifact(file('includes')))
			}
		"""
		file("producer/includes").mkdirs()

		buildTypes.each { buildType ->
			producerBuildFile << """
				configurations {
					create('shared${buildType.name.capitalize()}LinkLibraryElements', configure.asOutgoingLinkLibrariesFrom(implementation)${buildType.builderMethod}.withSharedLinkage().sharedLibraryArtifact(file('${buildType.path}${importLibraryPath}')))
					create('shared${buildType.name.capitalize()}RuntimeLibraryElements', configure.asOutgoingRuntimeLibrariesFrom(implementation)${buildType.builderMethod}.withSharedLinkage().sharedLibraryArtifact(file('${buildType.path}${sharedLibraryPath}')))
				}
			"""
			file("producer/${buildType.path}${importLibraryPath}").createFile()
			file("producer/${buildType.path}${sharedLibraryPath}").createFile()
		}
	}

	protected void makeFrameworkProducerProject(BuildType... buildTypes = [DEFAULT]) {
		producerBuildFile << """
			configurations {
				createIfAbsent('api', configure.asBucket())
				createIfAbsent('implementation', configure.asBucket(api))
			}
		"""

		buildTypes.each { buildType ->
			producerBuildFile << """
				configurations {
					create('framework${buildType.name.capitalize()}HeaderSearchPathElements', configure.asOutgoingHeaderSearchPathFrom(implementation)${buildType.builderMethod}.frameworkArtifact(file('${frameworkPath}')))
					create('framework${buildType.name.capitalize()}LinkLibraryElements', configure.asOutgoingLinkLibrariesFrom(implementation)${buildType.builderMethod}.frameworkArtifact(file('${frameworkPath}')))
					create('framework${buildType.name.capitalize()}RuntimeLibraryElements', configure.asOutgoingRuntimeLibrariesFrom(implementation)${buildType.builderMethod}.frameworkArtifact(file('${frameworkPath}')))
				}
			"""
			file("producer/${frameworkPath}").createFile()
		}
	}

	def "selects framework variant from framework producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject()

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelected()
	}

	@Ignore("TODO: Come back to these rules, for now, they aren't required")
	def "selects framework variant from framework and static linkage producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject()
		makeStaticLibraryProducerProject()

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelected()
	}

	@Ignore("TODO: Come back to these rules, for now, they aren't required")
	def "selects framework variant from framework and shared linkage producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject()
		makeSharedLibraryProducerProject()

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelected()
	}

	@Ignore("TODO: Come back to these rules, for now, they aren't required")
	def "selects framework variant from framework, static linkage and shared linkage producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject()
		makeSharedLibraryProducerProject()
		makeStaticLibraryProducerProject()

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelected()
	}

	def "selects framework variant from framework (wBT) producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject(debug, release)

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelectedMatchingBuildTypes()
	}

	def "selects framework variant from framework (wBT) and static linkage producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject(debug, release)
		makeStaticLibraryProducerProject()

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelectedMatchingBuildTypes()
	}

	@Ignore
	def "selects framework variant from framework and static linkage (wBT) producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject()
		makeStaticLibraryProducerProject(debug, release)

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelected()
	}

	@Ignore("TODO: Come back to these rules, for now, they aren't required")
	def "selects framework variant from framework and static linkage (all wBT) producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject(debug, release)
		makeStaticLibraryProducerProject(debug, release)

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelectedMatchingBuildTypes()
	}

	def "selects framework variant from framework (wBT) and shared linkage producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject(debug, release)
		makeSharedLibraryProducerProject()

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelectedMatchingBuildTypes()
	}

	@Ignore
	def "selects framework variant from framework and shared linkage (wBT) producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject()
		makeSharedLibraryProducerProject(debug, release)

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelected()
	}

	def "selects framework variant from framework and shared linkage (all wBT) producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject(debug, release)
		makeSharedLibraryProducerProject(debug, release)

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelectedMatchingBuildTypes()
	}

	@Ignore
	def "selects framework variant from framework (wBT), static and shared linkage producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject(debug, release)
		makeSharedLibraryProducerProject()
		makeStaticLibraryProducerProject()

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelectedMatchingBuildTypes()
	}

	@Ignore
	def "selects framework variant from framework, static linkage (wBT) and shared linkage producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject()
		makeStaticLibraryProducerProject(debug, release)
		makeSharedLibraryProducerProject()

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelected()
	}

	@Ignore
	def "selects framework variant from framework, static linkage and shared linkage (wBT) producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject()
		makeStaticLibraryProducerProject()
		makeSharedLibraryProducerProject(debug, release)

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelected()
	}

	@Ignore
	def "selects framework variant from framework (wBT), static linkage and shared linkage (wBT) producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject(debug, release)
		makeStaticLibraryProducerProject()
		makeSharedLibraryProducerProject(debug, release)

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelectedMatchingBuildTypes()
	}

	@Ignore
	def "selects framework variant from framework, static linkage (wBT) and shared linkage (wBT) producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject()
		makeStaticLibraryProducerProject(debug, release)
		makeSharedLibraryProducerProject(debug, release)

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelected()
	}

	@Ignore
	def "selects framework variant from framework (wBT), static linkage (wBT) and shared linkage producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject(debug, release)
		makeStaticLibraryProducerProject(debug, release)
		makeSharedLibraryProducerProject()

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelectedMatchingBuildTypes()
	}

	@Ignore
	def "selects framework variant from framework, static linkage and shared linkage (all wBT) producer"() {
		given:
		makeSingleProject()
		makeFrameworkProducerProject(debug, release)
		makeStaticLibraryProducerProject(debug, release)
		makeSharedLibraryProducerProject(debug, release)

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertFrameworkVariantSelectedMatchingBuildTypes()
	}

	def "selects static variant from static linkage (wBT) producer"() {
		given:
		makeSingleProject()
		makeStaticLibraryProducerProject(debug, release)

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertStaticVariantSelectedMatchingBuildTypes(debug, release)
	}

	def "selects shared variant from shared linkage with build type producer"() {
		given:
		makeSingleProject()
		makeSharedLibraryProducerProject(debug, release)

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertSharedVariantSelectedMatchingBuildTypes(debug, release)
	}

	@Ignore
	def "selects shared variant from static linkage with build type and shared linkage producer"() {
		given:
		makeSingleProject()
		makeStaticLibraryProducerProject(debug, release)
		makeSharedLibraryProducerProject()

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertSharedVariantSelected()
	}

	def "selects shared variant from static and shared linkage with build type producer"() {
		given:
		makeSingleProject()
		makeStaticLibraryProducerProject()
		makeSharedLibraryProducerProject(debug, release)

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertSharedVariantSelectedMatchingBuildTypes(debug, release)
	}

	def "selects shared variant from static and shared linkage each with build type producer"() {
		given:
		makeSingleProject()
		makeStaticLibraryProducerProject(debug, release)
		makeSharedLibraryProducerProject(debug, release)

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertSharedVariantSelectedMatchingBuildTypes(debug, release)
	}
}
