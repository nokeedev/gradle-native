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

import dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.platform.nativebase.internal.ConfigurationUtils
import dev.nokee.runtime.darwin.internal.plugins.CompatibilityRules

import static org.apache.commons.io.FilenameUtils.separatorsToSystem

abstract class AbstractConfigurationFunctionalTest extends AbstractFunctionalSpec {

	protected TestFile getProducerBuildFile() {
		return file('producer/build.gradle')
	}

	protected String getFrameworkPath() {
		return 'Frameworks/Producer.framework'
	}

	protected String getImportLibraryPath() {
		return 'libs/producer.lib'
	}

	protected String getSharedLibraryPath() {
		return 'libs/producer.dll'
	}

	protected String getStaticLibraryPath() {
		return 'libs/libproducer.a'
	}

	protected void assertSharedVariantSelected(BuildType consumer, BuildType producer) {
		result.assertOutputContains("headerSearchPath${consumer.name.capitalize()}: [${separatorsToSystem('producer/includes')}]")
		result.assertOutputContains("linkLibraries${consumer.name.capitalize()}: [${separatorsToSystem("producer/${producer.path}${importLibraryPath}")}]")
		result.assertOutputContains("runtimeLibraries${consumer.name.capitalize()}: [${separatorsToSystem("producer/${producer.path}${sharedLibraryPath}")}]")
	}

	protected void assertFrameworkVariantSelected(BuildType consumer, BuildType producer) {
		result.assertOutputContains("headerSearchPath${consumer.name.capitalize()}: [${separatorsToSystem("producer/${frameworkPath}")}]")
		result.assertOutputContains("linkLibraries${consumer.name.capitalize()}: [${separatorsToSystem("producer/${frameworkPath}")}]")
		result.assertOutputContains("runtimeLibraries${consumer.name.capitalize()}: [${separatorsToSystem("producer/${frameworkPath}")}]")
	}

	protected void assertStaticVariantSelected(BuildType consumer, BuildType producer) {
		result.assertOutputContains("headerSearchPath${consumer.name.capitalize()}: [${separatorsToSystem("producer/includes")}]")
		result.assertOutputContains("linkLibraries${consumer.name.capitalize()}: [${separatorsToSystem("producer/${producer.path}${staticLibraryPath}")}]")
		result.assertOutputContains("runtimeLibraries${consumer.name.capitalize()}: []")
	}

	protected void makeSingleProject(BuildType... buildTypes) {
		settingsFile << """
			include 'producer'
		"""
		buildFile << configurePluginClasspathAsBuildScriptDependencies() << """
			apply plugin: ${CompatibilityRules.canonicalName}
			def configure = objects.newInstance(${ConfigurationUtils.canonicalName})

			configurations {
				create('implementation', configure.asBucket())

				${buildTypes.collect { buildType -> """
				create('headerSearchPath${buildType.name.capitalize()}', configure.asIncomingHeaderSearchPathFrom(implementation)${buildType.builderMethod})
				create('linkLibraries${buildType.name.capitalize()}', configure.asIncomingLinkLibrariesFrom(implementation)${buildType.builderMethod})
				create('runtimeLibraries${buildType.name.capitalize()}', configure.asIncomingRuntimeLibrariesFrom(implementation)${buildType.builderMethod})
				"""}.join('\n')}
			}

			dependencies {
				implementation project(':producer')
			}

			void displayResolved(Configuration c) {
				def files = c.files.collect { (it.absolutePath - project.projectDir.absolutePath).substring(1) }
				println("\${c.name}: \${files}")
			}

			tasks.register('resolve') {
				doLast {
					${buildTypes.collect { buildType -> """
					displayResolved(configurations.headerSearchPath${buildType.name.capitalize()})
					displayResolved(configurations.linkLibraries${buildType.name.capitalize()})
					displayResolved(configurations.runtimeLibraries${buildType.name.capitalize()})
					"""}.join('\n')}
				}
			}
		"""

		producerBuildFile << """
			def configure = objects.newInstance(${ConfigurationUtils.canonicalName})

			void createIfAbsent(String name, Action<Configuration> action) {
				if (configurations.findByName(name) == null) {
					configurations.create(name, action)
				}
			}
		"""
	}

	protected void reportDependencies(BuildType... buildTypes) {
		run(':producer:outgoingVariant')
		buildTypes.each { buildType ->
			run('dependencyInsight', '--configuration', "headerSearchPath${buildType.name.capitalize()}", '--dependency', ':producer')
			run('dependencyInsight', '--configuration', "linkLibraries${buildType.name.capitalize()}", '--dependency', ':producer')
			run('dependencyInsight', '--configuration', "runtimeLibraries${buildType.name.capitalize()}", '--dependency', ':producer')
		}
	}

	protected abstract void assertSharedVariantSelected()
	protected abstract void assertStaticVariantSelected()

	protected BuildType getDebug() {
		return new BuildType() {
			final String name = 'debug'
			final String builderMethod = '.asDebug()'
			final String path = 'debug/'
		}
	}

	protected BuildType getRelease() {
		return new BuildType() {
			final String name = 'release'
			final String builderMethod = '.asRelease()'
			final String path = 'release/'
		}
	}

	protected abstract void makeSingleProject()

	protected abstract void makeStaticLibraryProducerProject()

	protected abstract void makeSharedLibraryProducerProject()

	protected abstract void reportDependencies();

	def "selects static variant from static linkage producer"() {
		given:
		makeSingleProject()
		makeStaticLibraryProducerProject()

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertStaticVariantSelected()
	}

	def "selects shared variant from shared linkage producer"() {
		given:
		makeSingleProject()
		makeSharedLibraryProducerProject()

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertSharedVariantSelected()
	}

	def "selects shared variant from static and shared linkage producer"() {
		given:
		makeSingleProject()
		makeStaticLibraryProducerProject()
		makeSharedLibraryProducerProject()

		and:
		reportDependencies()

		expect:
		succeeds('resolve')
		assertSharedVariantSelected()
	}

	protected static final BuildType DEFAULT = new BuildType() {
		final String name = ''
		final String builderMethod = ''
		final String path = ''
	}
	protected static interface BuildType {
		String getName()

		String getBuilderMethod()

		String getPath()
	}
}
