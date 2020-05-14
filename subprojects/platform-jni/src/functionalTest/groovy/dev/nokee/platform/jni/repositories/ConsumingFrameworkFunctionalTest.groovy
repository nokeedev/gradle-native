package dev.nokee.platform.jni.repositories

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.archive.JarTestFixture
import dev.nokee.platform.jni.fixtures.JavaJniObjectiveCGreeterLib
import dev.nokee.platform.jni.fixtures.JavaJniObjectiveCNSSavePanelLib
import dev.nokee.platform.nativebase.internal.ArtifactSerializationTypes
import dev.nokee.platform.nativebase.internal.ConfigurationUtils
import dev.nokee.platform.nativebase.internal.DefaultTargetMachineFactory
import dev.nokee.platform.nativebase.internal.LibraryElements
import dev.nokee.platform.nativebase.internal.plugins.FakeMavenRepositoryPlugin
import spock.lang.Requires
import spock.lang.Unroll
import spock.util.environment.OperatingSystem

import java.nio.file.Files

import static java.util.regex.Pattern.*
import static org.hamcrest.text.MatchesPattern.matchesPattern

@Requires({OperatingSystem.current.macOs})
class ConsumingFrameworkFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def "can depend on local framework"() {
		settingsFile << "rootProject.name = 'greeter'"
		buildFile << """
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.objective-c-language'
			}

			library {
				dependencies {
					nativeImplementation 'dev.nokee.framework:Foundation:10.15'
					nativeImplementation 'dev.nokee.framework:CoreFoundation:10.15' // dependency of Foundation
				}
			}

			library.variants.configureEach {
				sharedLibrary {
					linkTask.configure {
						linkerArgs.add('-nostdinc')
						linkerArgs.add('-lobjc')
					}
					compileTasks.configureEach {
						compilerArgs.add('-nostdinc')
					}
				}
			}
		"""
		new JavaJniObjectiveCGreeterLib('greeter').withFoundationFrameworkDependency().writeToProject(testDirectory)

		expect:
		succeeds('assemble')
		jar('build/libs/greeter.jar').hasDescendants('com/example/greeter/NativeLoader.class', 'com/example/greeter/Greeter.class', sharedLibraryName('greeter'))
	}

	def "stops the embedded server after each build"() {
		settingsFile << "rootProject.name = 'greeter'"
		buildFile << """
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.objective-c-language'
			}

			library {
				dependencies {
					nativeImplementation 'dev.nokee.framework:Foundation:10.15'
					nativeImplementation 'dev.nokee.framework:CoreFoundation:10.15' // dependency of Foundation
				}
			}

			library.variants.configureEach {
				sharedLibrary {
					linkTask.configure {
						linkerArgs.add('-nostdinc')
						linkerArgs.add('-lobjc')
					}
					compileTasks.configureEach {
						compilerArgs.add('-nostdinc')
					}
				}
			}
		"""
		new JavaJniObjectiveCGreeterLib('greeter').withFoundationFrameworkDependency().writeToProject(testDirectory)

		when:
		succeeds('assemble', '-i')
		then:
		result.assertOutputContains("Nokee server stopped")

		when:
		succeeds('assemble', '-i')
		then:
		result.assertOutputContains("Nokee server stopped")
	}

	def "can depends on subframeworks via capabilities (ie JavaNativeFoundation)"() {
		settingsFile << "rootProject.name = 'save-panel'"
		buildFile << """
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.objective-c-language'
			}

			library {
				dependencies {
					nativeImplementation('dev.nokee.framework:JavaVM:10.15') {
						capabilities {
							requireCapability 'JavaVM:JavaNativeFoundation:10.15'
						}
					}
					nativeImplementation 'dev.nokee.framework:Cocoa:10.15'
				}
			}

			library.variants.configureEach {
				sharedLibrary {
					linkTask.configure {
						linkerArgs.add('-lobjc')
					}
				}
			}
		"""

		new JavaJniObjectiveCNSSavePanelLib('save-panel').writeToProject(testDirectory)

		expect:
		succeeds('assemble')
		jar('build/libs/save-panel.jar').hasDescendants('com/example/cocoa/NativeLoader.class', 'com/example/cocoa/NSSavePanel.class', sharedLibraryName('save-panel'))
	}

	// Test multiple capabilities
	// Test mutliple projects (only one web server should be started)
	// Test invalid capabilities (subframework)
	// Test multiple projects with resolution
	// Test depending on core cpp-library on macOS with frameworks...

	// Open question, framework on non macOS
	// Can you say with dependency engine, if you don't find attribute x assume no artifacts?

	def "fails to resolve for non-magic group"() {
		buildFile << configurePluginClasspathAsBuildScriptDependencies() << """
			import ${DarwinFrameworkResolutionSupportPlugin.canonicalName}
			import ${ConfigurationUtils.canonicalName}
			import ${DefaultTargetMachineFactory.canonicalName}
			import ${LibraryElements.canonicalName}
			import ${ArtifactSerializationTypes.canonicalName}

			apply plugin: ${DarwinFrameworkResolutionSupportPlugin.name}

			configurations {
				create('framework', objects.newInstance(${ConfigurationUtils.name}).asIncomingHeaderSearchPath())
			}

			dependencies {
				framework('non.magic.group:JavaVM:10.15') {
					attributes {
						attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.FRAMEWORK_BUNDLE))
						attribute(ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, ArtifactSerializationTypes.DESERIALIZED)
					}
				}
			}

			tasks.register('resolveConfiguration') {
				doLast {
					configurations.framework.each {
						println it
					}
				}
			}
		"""

		expect:
		fails('resolveConfiguration', '-i')
		failure.assertHasDescription("Execution failed for task ':resolveConfiguration'.")
		failure.assertHasCause("Could not resolve all files for configuration ':framework'.")
		failure.assertHasCause("""Could not find non.magic.group:JavaVM:10.15.
Required by:
    project :""")

		failure.assertNotOutput("The requested path doesn't match the magic value, make sure you are requesting group 'dev.nokee.framework'.")
	}

	def "fails to resolve for non-existant framework"() {
		buildFile << configurePluginClasspathAsBuildScriptDependencies() << """
			import ${DarwinFrameworkResolutionSupportPlugin.canonicalName}
			import ${ConfigurationUtils.canonicalName}
			import ${DefaultTargetMachineFactory.canonicalName}
			import ${LibraryElements.canonicalName}
			import ${ArtifactSerializationTypes.canonicalName}

			apply plugin: ${DarwinFrameworkResolutionSupportPlugin.name}

			configurations {
				create('framework', objects.newInstance(${ConfigurationUtils.name}).asIncomingHeaderSearchPath())
			}

			dependencies {
				framework('dev.nokee.framework:NonExistantFramework:10.15') {
					attributes {
						attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.FRAMEWORK_BUNDLE))
						attribute(ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, ArtifactSerializationTypes.DESERIALIZED)
					}
				}
			}

			tasks.register('resolveConfiguration') {
				doLast {
					configurations.framework.each {
						println it
					}
				}
			}
		"""

		expect:
		fails('resolveConfiguration', '-i')
		failure.assertHasDescription("Execution failed for task ':resolveConfiguration'.")
		failure.assertHasCause("Could not resolve all files for configuration ':framework'.")
		failure.assertThatCause(matchesPattern(compile("""Could not find dev.nokee.framework:NonExistantFramework:10.15.
Searched in the following locations:
  - http://localhost:\\d+/dev/nokee/framework/NonExistantFramework/10.15/NonExistantFramework-10.15.module
.+""", MULTILINE | DOTALL)))

		failure.assertOutputContains("The requested framework 'NonExistantFramework' wasn't found at in '/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/System/Library/Frameworks/'.")
	}

	def "fails to resolve framework for bad SDK version"() {
		buildFile << configurePluginClasspathAsBuildScriptDependencies() << """
			import ${DarwinFrameworkResolutionSupportPlugin.canonicalName}
			import ${ConfigurationUtils.canonicalName}
			import ${DefaultTargetMachineFactory.canonicalName}
			import ${LibraryElements.canonicalName}
			import ${ArtifactSerializationTypes.canonicalName}

			apply plugin: ${DarwinFrameworkResolutionSupportPlugin.name}

			configurations {
				create('framework', objects.newInstance(${ConfigurationUtils.name}).asIncomingHeaderSearchPath())
			}

			dependencies {
				framework('dev.nokee.framework:Foundation:4.2') {
					attributes {
						attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.FRAMEWORK_BUNDLE))
						attribute(ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, ArtifactSerializationTypes.DESERIALIZED)
					}
				}
			}

			tasks.register('resolveConfiguration') {
				doLast {
					configurations.framework.each {
						println it
					}
				}
			}
		"""

		expect:
		fails('resolveConfiguration', '-i')
		failure.assertHasDescription("Execution failed for task ':resolveConfiguration'.")
		failure.assertHasCause("Could not resolve all files for configuration ':framework'.")
		failure.assertThatCause(matchesPattern(compile("""Could not find dev.nokee.framework:Foundation:4.2.
Searched in the following locations:
  - http://localhost:\\d+/dev/nokee/framework/Foundation/4.2/Foundation-4.2.module
.+""", MULTILINE | DOTALL)))

		failure.assertOutputContains("The requested framework 'Foundation' version '4.2' doesn't match current SDK version '10.15'.")
	}

	def "can resolve framework search path"() {
		buildFile << configurePluginClasspathAsBuildScriptDependencies() << """
			import ${DarwinFrameworkResolutionSupportPlugin.canonicalName}
			import ${ConfigurationUtils.canonicalName}
			import ${DefaultTargetMachineFactory.canonicalName}
			import ${Files.canonicalName}
			import ${LibraryElements.canonicalName}
			import ${ArtifactSerializationTypes.canonicalName}

			apply plugin: ${DarwinFrameworkResolutionSupportPlugin.name}

			configurations {
				create('framework', objects.newInstance(${ConfigurationUtils.name}).asIncomingHeaderSearchPath())
			}

			dependencies {
				framework('dev.nokee.framework:Foundation:10.15') {
					attributes {
						attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.FRAMEWORK_BUNDLE))
						attribute(ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, ArtifactSerializationTypes.DESERIALIZED)
					}
				}
			}

			tasks.register('verify') {
				doLast {
					def f = configurations.framework.singleFile
					assert Files.exists(f.toPath())
					assert Files.isSymbolicLink(f.toPath())
					assert Files.readSymbolicLink(f.toPath()).toString() == '/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/System/Library/Frameworks/Foundation.framework'
				}
			}
		"""

		expect:
		succeeds('verify')
	}

	def "can resolve framework bundle"() {
		buildFile << configurePluginClasspathAsBuildScriptDependencies() << """
			import ${DarwinFrameworkResolutionSupportPlugin.canonicalName}
			import ${ConfigurationUtils.canonicalName}
			import ${DefaultTargetMachineFactory.canonicalName}
			import ${Files.canonicalName}
			import ${LibraryElements.canonicalName}
			import ${ArtifactSerializationTypes.canonicalName}

			apply plugin: ${DarwinFrameworkResolutionSupportPlugin.name}

			configurations {
				create('framework', objects.newInstance(${ConfigurationUtils.name}).asIncomingHeaderSearchPath())
			}

			dependencies {
				framework('dev.nokee.framework:Foundation:10.15') {
					attributes {
						attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.FRAMEWORK_BUNDLE))
						attribute(ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, ArtifactSerializationTypes.DESERIALIZED)
					}
				}
			}

			tasks.register('verify') {
				doLast {
					def f = configurations.framework.singleFile
					assert f.name == 'Foundation.framework'
					assert Files.exists(f.toPath())
					assert Files.isSymbolicLink(f.toPath())
					assert Files.readSymbolicLink(f.toPath()).toString() == '/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/System/Library/Frameworks/Foundation.framework'
				}
			}
		"""

		expect:
		succeeds('verify')
	}

	@Unroll
	def "can resolve dynamic #displayName of framework bundle"(String versionNotation, String displayName) {
		buildFile << configurePluginClasspathAsBuildScriptDependencies() << """
			import ${DarwinFrameworkResolutionSupportPlugin.canonicalName}
			import ${ConfigurationUtils.canonicalName}
			import ${DefaultTargetMachineFactory.canonicalName}
			import ${Files.canonicalName}
			import ${LibraryElements.canonicalName}
			import ${ArtifactSerializationTypes.canonicalName}

			apply plugin: ${DarwinFrameworkResolutionSupportPlugin.name}

			configurations {
				create('framework', objects.newInstance(${ConfigurationUtils.name}).asIncomingHeaderSearchPath())
			}

			dependencies {
				framework('dev.nokee.framework:Foundation:$versionNotation') {
					attributes {
						attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.FRAMEWORK_BUNDLE))
						attribute(ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, ArtifactSerializationTypes.DESERIALIZED)
					}
				}
			}

			tasks.register('verify') {
				doLast {
					def f = configurations.framework.singleFile
					assert f.name == 'Foundation.framework'
					assert Files.exists(f.toPath())
					assert Files.isSymbolicLink(f.toPath())
					assert Files.readSymbolicLink(f.toPath()).toString() == '/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/System/Library/Frameworks/Foundation.framework'
				}
			}
		"""

		expect:
		succeeds('verify', '-i')

		where:
		versionNotation 		| displayName
		'latest.integration'	| 'latest status for integration'
		'latest.release'		| 'latest status for release'
		'10.+'					| 'prefix version range'
		'[10.0,10.17]'			| 'version range'
		'[10.0,10.17]!!10.15'	| 'version range with preference'
	}

	def "handles xcrun errors without logging beyond info level"() {
		buildFile << configurePluginClasspathAsBuildScriptDependencies() << """
			import ${DarwinFrameworkResolutionSupportPlugin.canonicalName}
			import ${ConfigurationUtils.canonicalName}
			import ${DefaultTargetMachineFactory.canonicalName}
			import ${Files.canonicalName}
			import ${LibraryElements.canonicalName}
			import ${ArtifactSerializationTypes.canonicalName}

			apply plugin: ${DarwinFrameworkResolutionSupportPlugin.name}

			configurations {
				create('framework', objects.newInstance(${ConfigurationUtils.name}).asIncomingHeaderSearchPath())
			}

			dependencies {
				framework('dev.nokee.framework:Foundation:10.15') {
					attributes {
						attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.FRAMEWORK_BUNDLE))
						attribute(ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, ArtifactSerializationTypes.DESERIALIZED)
					}
				}
			}

			tasks.register('resolveConfiguration') {
				doLast {
					configurations.framework.each {
						println it
					}
				}
			}
		"""

		when:
		def failure = executer.withEnvironmentVars([DEVELOPER_DIR: '/opt/xcode']).withTasks('resolveConfiguration', '-i').runWithFailure()
		then:
		failure.assertOutputContains('''An exception occurred during the dispatch of the request: Process 'xcrun --show-sdk-version' finished with non-zero exit value 1''')
		and:
		failure.assertHasDescription("Execution failed for task ':resolveConfiguration'.")
		failure.assertHasCause("Could not resolve all files for configuration ':framework'.")
		failure.assertThatCause(matchesPattern(compile("""Could not find dev.nokee.framework:Foundation:10.15.
Searched in the following locations:
  - http://localhost:\\d+/dev/nokee/framework/Foundation/10.15/Foundation-10.15.module
.+""", MULTILINE | DOTALL)))

		when:
		failure = executer.withEnvironmentVars([DEVELOPER_DIR: '/opt/xcode']).withTasks('resolveConfiguration').runWithFailure()
		then:
		failure.assertNotOutput('''An exception occurred during the dispatch of the request: Fail to execute xcrun:
xcrun: error: missing DEVELOPER_DIR path: /opt/xcode''')
		and:
		failure.assertHasDescription("Execution failed for task ':resolveConfiguration'.")
		failure.assertHasCause("Could not resolve all files for configuration ':framework'.")
		failure.assertThatCause(matchesPattern(compile("""Could not find dev.nokee.framework:Foundation:10.15.
Searched in the following locations:
  - http://localhost:\\d+/dev/nokee/framework/Foundation/10.15/Foundation-10.15.module
.+""", MULTILINE | DOTALL)))
	}

	protected JarTestFixture jar(String path) {
		return new JarTestFixture(file(path))
	}
}
