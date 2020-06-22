package dev.nokee.fixtures

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily
import org.gradle.nativeplatform.OperatingSystemFamily
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin

import static org.junit.Assume.assumeTrue

abstract class AbstractNativeComponentDependenciesFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	protected String getLibraryProjectName() {
		return 'library'
	}

	def "can define implementation dependencies on component"() {
		given:
		makeComponentWithLibrary()
		buildFile << """
			${componentUnderTestDsl} {
				dependencies {
					${implementationBucketNameUnderTest} ${dependencyNotation}
				}
			}
		"""

		when:
		run(tasks.assemble)

		then:
		result.assertTasksExecuted(libraryTasks, tasks.allToAssemble)
	}

	def "can define implementation dependencies on each variant"() {
		assumeTrue(canDefineDependencyOnVariants())

		given:
		makeComponentWithLibrary()
		buildFile << """
			${componentUnderTestDsl} {
				variants.configureEach { b ->
					dependencies {
						${implementationBucketNameUnderTest} ${dependencyNotation}
					}
				}
			}
		"""

		when:
		run(tasks.assemble)

		then:
		result.assertTasksExecuted(libraryTasks, tasks.allToAssemble)
	}

	// TODO: Variant-aware configuration
	def "can define implementation dependencies on single variant"() {
		assumeTrue(canDefineDependencyOnVariants())

		given:
		makeComponentWithLibrary()
		buildFile << configureToolChainSupport('foo') << """
			import ${DefaultOperatingSystemFamily.canonicalName}

			${componentUnderTestDsl} {
				targetMachines = [machines.${currentHostOperatingSystemFamilyDsl}, machines.os('foo')]
				variants.configureEach({it.buildVariant.getAxisValue(${DefaultOperatingSystemFamily.simpleName}.DIMENSION_TYPE).${currentHostOperatingSystemFamilyDsl}}) {
					dependencies {
						${implementationBucketNameUnderTest} ${dependencyNotation}
					}
				}
			}
		"""

		when:
		run(tasks.withOperatingSystemFamily(currentOsFamilyName).assemble, '--dry-run')
		then:
		// TODO: https://github.com/gradle-plugins/toolbox/issues/15
		(libraryTasks + tasks.withOperatingSystemFamily(currentOsFamilyName).allToAssemble).each { result.assertOutputContains(it) }

		when:
		run(tasks.withOperatingSystemFamily('foo').assemble, '--dry-run')
		then:
		// TODO: https://github.com/gradle-plugins/toolbox/issues/15
		tasks.withOperatingSystemFamily('foo').allToAssemble.each { result.assertOutputContains(it) }
		libraryTasks.each { result.assertNotOutput(it) }
	}

	// TODO: Add test for source dependencies

	/**
	 * Creates a build with the component under test in the root project and a library in the 'lib' project.
	 */
	protected abstract void makeComponentWithLibrary()

	protected abstract String getComponentUnderTestDsl()

	protected abstract List<String> getLibraryTasks()

	protected abstract String getDependencyNotation()

	protected String getImplementationBucketNameUnderTest() {
		return 'implementation'
	}

	protected boolean canDefineDependencyOnVariants() {
		return true
	}

	protected String configureToolChainSupport(String operatingSystem, String architecture = currentArchitecture) {
		String className = "ToolChainFor${operatingSystem.capitalize()}${architecture.capitalize()}Rules".replace('-', '')
		return """
			class ${className} extends RuleSource {
				@Finalize
				void addToolChain(NativeToolChainRegistry toolChains) {
					toolChains.create("toolChainFor${operatingSystem.capitalize()}${architecture.capitalize()}", Gcc) {
						path "/not/found"
						target("${operatingSystem}${architecture}") // It needs to be the same as NativePlatformFactory#platformNameFor
					}
				}
			}
			import ${StandardToolChainsPlugin.canonicalName}
			plugins.withType(StandardToolChainsPlugin) {
				// TODO: Applies after the Stardard tool chain plugin
				plugins.apply(${className})
			}
		"""
	}

	protected String getCurrentHostOperatingSystemFamilyDsl() {
		String osFamily = DefaultNativePlatform.getCurrentOperatingSystem().toFamilyName()
		if (osFamily == OperatingSystemFamily.MACOS) {
			return "macOS"
		} else {
			return osFamily
		}
	}
}


abstract class AbstractNativeComponentProjectDependenciesFunctionalTest extends AbstractNativeComponentDependenciesFunctionalTest {
	@Override
	protected String getDependencyNotation() {
		return "project(':${libraryProjectName}')"
	}

	protected String configureMultiProjectBuild() {
		return """
			include '${libraryProjectName}'
		"""
	}
}

abstract class AbstractNativeComponentIncludedBuildDependenciesFunctionalTest extends AbstractNativeComponentDependenciesFunctionalTest {
	@Override
	protected String getDependencyNotation() {
		return "'dev.nokee.test:${libraryProjectName}:1.0'"
	}

	protected String configureLibraryProject() {
		return """
			group = 'dev.nokee.test'
			version = '1.0'
		"""
	}

	protected String configureMultiProjectBuild() {
		return """
			includeBuild '${libraryProjectName}'
		"""
	}
}
