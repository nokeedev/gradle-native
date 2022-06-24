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
package dev.nokee.platform.jni.repositories

import com.google.common.collect.Iterables
import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.archive.JarTestFixture
import dev.nokee.core.exec.CachingProcessBuilderEngine
import dev.nokee.core.exec.CommandLine
import dev.nokee.core.exec.ProcessBuilderEngine
import dev.nokee.platform.jni.fixtures.JavaJniObjectiveCGreeterLib
import dev.nokee.platform.jni.fixtures.JavaJniObjectiveCNSSavePanelLib
import dev.nokee.platform.nativebase.internal.ConfigurationUtils
import dev.nokee.runtime.darwin.internal.DarwinLibraryElements
import dev.nokee.runtime.darwin.internal.locators.XcodebuildLocator
import dev.nokee.runtime.darwin.internal.plugins.DarwinFrameworkResolutionSupportPlugin
import dev.nokee.runtime.nativebase.MachineArchitecture
import dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes
import spock.lang.Ignore
import spock.lang.Requires
import spock.lang.Unroll
import spock.util.environment.OperatingSystem

import java.nio.file.Files

import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.hasFailureCause
import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.hasFailureDescription
import static dev.nokee.runtime.darwin.internal.FrameworkRouteHandler.findMacOsSdks
import static java.util.regex.Pattern.*
import static org.hamcrest.text.MatchesPattern.matchesPattern
import static spock.util.matcher.HamcrestSupport.expect

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
					nativeImplementation 'dev.nokee.framework:Foundation:${sdkVersion}'
					nativeImplementation 'dev.nokee.framework:CoreFoundation:${sdkVersion}' // dependency of Foundation
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
		succeeds('assemble', '-i')
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
					nativeImplementation 'dev.nokee.framework:Foundation:${sdkVersion}'
					nativeImplementation 'dev.nokee.framework:CoreFoundation:${sdkVersion}' // dependency of Foundation
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

	@Ignore // For now, JavaVM is only available on Xcode 12.1 and lower
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
					nativeImplementation('dev.nokee.framework:JavaVM:${sdkVersion}') {
						capabilities {
							requireCapability 'JavaVM:JavaNativeFoundation:${sdkVersion}'
						}
					}
					nativeImplementation 'dev.nokee.framework:Cocoa:${sdkVersion}'
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
			import ${DarwinLibraryElements.canonicalName}
			import ${ArtifactSerializationTypes.canonicalName}
			import ${MachineArchitecture.canonicalName}

			apply plugin: ${DarwinFrameworkResolutionSupportPlugin.name}

			configurations {
				create('framework', objects.newInstance(${ConfigurationUtils.name}).asIncomingHeaderSearchPath())
				framework {
					attributes {
						attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named(MachineArchitecture, MachineArchitecture.X86_64))
					}
				}
			}

			dependencies {
				framework('non.magic.group:JavaVM:${sdkVersion}') {
					attributes {
						attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, DarwinLibraryElements.FRAMEWORK_BUNDLE))
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
		failure.assertHasCause("""Could not find non.magic.group:JavaVM:${sdkVersion}.
Required by:
    project :""")

		failure.assertNotOutput("The requested path doesn't match the magic value, make sure you are requesting group 'dev.nokee.framework'.")
	}

	def "fails to resolve for non-existant framework"() {
		buildFile << configurePluginClasspathAsBuildScriptDependencies() << """
			import ${DarwinFrameworkResolutionSupportPlugin.canonicalName}
			import ${ConfigurationUtils.canonicalName}
			import ${DarwinLibraryElements.canonicalName}
			import ${ArtifactSerializationTypes.canonicalName}
			import ${MachineArchitecture.canonicalName}

			apply plugin: ${DarwinFrameworkResolutionSupportPlugin.name}

			configurations {
				create('framework', objects.newInstance(${ConfigurationUtils.name}).asIncomingHeaderSearchPath())
				framework {
					attributes {
						attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named(MachineArchitecture, MachineArchitecture.X86_64))
					}
				}
			}

			dependencies {
				framework('dev.nokee.framework:NonExistantFramework:${sdkVersion}') {
					attributes {
						attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, DarwinLibraryElements.FRAMEWORK_BUNDLE))
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
		failure.assertThatCause(matchesPattern(compile("""Could not find dev.nokee.framework:NonExistantFramework:${sdkVersion}.
Searched in the following locations:
  - http://127.0.0.1:\\d+/dev/nokee/framework/NonExistantFramework/${sdkVersion}/NonExistantFramework-${sdkVersion}.module
.+""", MULTILINE | DOTALL)))

		failure.assertThatOutput(matchesPattern(compile(".+The requested framework 'NonExistantFramework' wasn't found at in '/Applications/Xcode[_.0-9]*.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX${sdkFolderVersion}.sdk/System/Library/Frameworks/'..+", DOTALL)))
	}

	def "fails to resolve framework for bad SDK version"() {
		buildFile << configurePluginClasspathAsBuildScriptDependencies() << """
			import ${DarwinFrameworkResolutionSupportPlugin.canonicalName}
			import ${ConfigurationUtils.canonicalName}
			import ${DarwinLibraryElements.canonicalName}
			import ${ArtifactSerializationTypes.canonicalName}
			import ${MachineArchitecture.canonicalName}

			apply plugin: ${DarwinFrameworkResolutionSupportPlugin.name}

			configurations {
				create('framework', objects.newInstance(${ConfigurationUtils.name}).asIncomingHeaderSearchPath())
				framework {
					attributes {
						attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named(MachineArchitecture, MachineArchitecture.X86_64))
					}
				}
			}

			dependencies {
				framework('dev.nokee.framework:Foundation:4.2') {
					attributes {
						attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, DarwinLibraryElements.FRAMEWORK_BUNDLE))
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
  - http://127.0.0.1:\\d+/dev/nokee/framework/Foundation/4.2/Foundation-4.2.module
.+""", MULTILINE | DOTALL)))

		failure.assertOutputContains("The requested module 'Foundation' version '4.2' doesn't match current available versions '${sdkVersion}'.")
	}

	def "can resolve framework search path"() {
		buildFile << configurePluginClasspathAsBuildScriptDependencies() << """
			import ${DarwinFrameworkResolutionSupportPlugin.canonicalName}
			import ${ConfigurationUtils.canonicalName}
			import ${Files.canonicalName}
			import ${DarwinLibraryElements.canonicalName}
			import ${ArtifactSerializationTypes.canonicalName}
			import ${MachineArchitecture.canonicalName}

			apply plugin: ${DarwinFrameworkResolutionSupportPlugin.name}

			configurations {
				create('framework', objects.newInstance(${ConfigurationUtils.name}).asIncomingHeaderSearchPath())
				framework {
					attributes {
						attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named(MachineArchitecture, MachineArchitecture.X86_64))
					}
				}
			}

			dependencies {
				framework('dev.nokee.framework:Foundation:${sdkVersion}') {
					attributes {
						attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, DarwinLibraryElements.FRAMEWORK_BUNDLE))
						attribute(ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, ArtifactSerializationTypes.DESERIALIZED)
					}
				}
			}

			tasks.register('verify') {
				doLast {
					def f = configurations.framework.singleFile
					assert Files.exists(f.toPath())
					assert Files.isSymbolicLink(f.toPath())
					assert Files.readSymbolicLink(f.toPath()).toString().endsWith('/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX${sdkFolderVersion}.sdk/System/Library/Frameworks/Foundation.framework')
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
			import ${Files.canonicalName}
			import ${DarwinLibraryElements.canonicalName}
			import ${ArtifactSerializationTypes.canonicalName}
			import ${MachineArchitecture.canonicalName}

			apply plugin: ${DarwinFrameworkResolutionSupportPlugin.name}

			configurations {
				create('framework', objects.newInstance(${ConfigurationUtils.name}).asIncomingHeaderSearchPath())
				framework {
					attributes {
						attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named(MachineArchitecture, MachineArchitecture.X86_64))
					}
				}
			}

			dependencies {
				framework('dev.nokee.framework:Foundation:${sdkVersion}') {
					attributes {
						attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, DarwinLibraryElements.FRAMEWORK_BUNDLE))
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
					assert Files.readSymbolicLink(f.toPath()).toString().endsWith('/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX${sdkFolderVersion}.sdk/System/Library/Frameworks/Foundation.framework')
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
			import ${Files.canonicalName}
			import ${DarwinLibraryElements.canonicalName}
			import ${ArtifactSerializationTypes.canonicalName}
			import ${MachineArchitecture.canonicalName}

			apply plugin: ${DarwinFrameworkResolutionSupportPlugin.name}

			configurations {
				create('framework', objects.newInstance(${ConfigurationUtils.name}).asIncomingHeaderSearchPath())
				framework {
					attributes {
						attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named(MachineArchitecture, MachineArchitecture.X86_64))
					}
				}
			}

			dependencies {
				framework('dev.nokee.framework:Foundation:$versionNotation') {
					attributes {
						attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, DarwinLibraryElements.FRAMEWORK_BUNDLE))
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
					assert Files.readSymbolicLink(f.toPath()).toString().endsWith('/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX${sdkFolderVersion}.sdk/System/Library/Frameworks/Foundation.framework')
				}
			}
		"""

		expect:
		succeeds('verify', '-i')

		where:
		versionNotation       			| displayName
		'latest.integration'  			| 'latest status for integration'
		'latest.release'      			| 'latest status for release'
		'12.+'                			| 'prefix version range'
		'[10.0,12.3]'        			| 'version range'
		"[10.0,12.3]!!${sdkVersion}"	| 'version range with preference'
	}

	def "handles xcrun errors without logging beyond info level"() {
		buildFile << configurePluginClasspathAsBuildScriptDependencies() << """
			import ${DarwinFrameworkResolutionSupportPlugin.canonicalName}
			import ${ConfigurationUtils.canonicalName}
			import ${Files.canonicalName}
			import ${DarwinLibraryElements.canonicalName}
			import ${ArtifactSerializationTypes.canonicalName}
			import ${MachineArchitecture.canonicalName}

			apply plugin: ${DarwinFrameworkResolutionSupportPlugin.name}

			configurations {
				create('framework', objects.newInstance(${ConfigurationUtils.name}).asIncomingHeaderSearchPath())
				framework {
					attributes {
						attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named(MachineArchitecture, MachineArchitecture.X86_64))
					}
				}
			}

			dependencies {
				framework('dev.nokee.framework:Foundation:${sdkVersion}') {
					attributes {
						attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, DarwinLibraryElements.FRAMEWORK_BUNDLE))
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
		def failure = executer.withEnvironmentVariable('DEVELOPER_DIR', '/opt/xcode').withTasks('resolveConfiguration', '-i').buildAndFail()
		then:
		failure.output.contains('''An exception occurred during the dispatch of the request: Process '/usr/bin/xcrun --find xcodebuild' finished with non-zero exit value 1''')
		and:
		expect failure, hasFailureDescription("Execution failed for task ':resolveConfiguration'.")
		expect failure, hasFailureCause("Could not resolve all files for configuration ':framework'.")
		expect failure, hasFailureCause(matchesPattern(compile("""Could not find dev.nokee.framework:Foundation:${sdkVersion}.
Searched in the following locations:
  - http://127.0.0.1:\\d+/dev/nokee/framework/Foundation/${sdkVersion}/Foundation-${sdkVersion}.module
.+""", MULTILINE | DOTALL)))

		when:
		failure = executer.withEnvironmentVariable('DEVELOPER_DIR', '/opt/xcode').withTasks('resolveConfiguration').buildAndFail()
		then:
		!failure.output.contains('''An exception occurred during the dispatch of the request: Process '/usr/bin/xcrun --find xcodebuild' finished with non-zero exit value 1''')
		and:
		expect failure, hasFailureDescription("Execution failed for task ':resolveConfiguration'.")
		expect failure, hasFailureCause("Could not resolve all files for configuration ':framework'.")
		expect failure, hasFailureCause(matchesPattern(compile("""Could not find dev.nokee.framework:Foundation:${sdkVersion}.
Searched in the following locations:
  - http://127.0.0.1:\\d+/dev/nokee/framework/Foundation/${sdkVersion}/Foundation-${sdkVersion}.module
.+""", MULTILINE | DOTALL)))
	}

	protected JarTestFixture jar(String path) {
		return new JarTestFixture(file(path))
	}

	private static final CachingProcessBuilderEngine ENGINE = new CachingProcessBuilderEngine(new ProcessBuilderEngine())
	private String getSdkVersion() {
		def xcodeSdk = findMacOsSdks(Iterables.getFirst(new XcodebuildLocator().findAll("xcodebuild"), null), ENGINE)
		return CommandLine.of("xcrun", "-sdk", xcodeSdk.identifier, "--show-sdk-version")
			.execute(ENGINE)
			.result
			.assertNormalExitValue()
			.standardOutput.asString.trim()
	}

	// SDK version inside the path doesn't include the patch version, we simply drop it.
	private String getSdkFolderVersion() {
		String result = sdkVersion
		int versionSeparator = result.count('.')
		if (versionSeparator > 1) {
			// input	| versionSeparator	| drop 	| result
			// 10		| 0					| 0		| 10
			// 10.15	| 1					| 0		| 10.15
			// 10.15.4	| 2					| 1		| 10.15
			return result.split('\\.').dropRight(versionSeparator - 1).join('.')
		}
		return result
	}
}
