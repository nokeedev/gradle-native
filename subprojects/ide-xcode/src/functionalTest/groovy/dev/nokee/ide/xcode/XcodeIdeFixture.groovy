package dev.nokee.ide.xcode

import dev.gradleplugins.runnerkit.GradleRunner
import dev.nokee.ide.fixtures.IdeCommandLineUtils
import dev.nokee.ide.xcode.fixtures.XcodeIdeProjectFixture
import dev.nokee.ide.xcode.fixtures.XcodeIdeWorkspaceFixture
import dev.nokee.ide.xcode.fixtures.XcodebuildExecutor

import java.util.function.UnaryOperator

trait XcodeIdeFixture {
	String getXcodeIdePluginId() {
		return 'dev.nokee.xcode-ide'
	}

	// Must be kotlin dsl compatible
	String applyXcodeIdePlugin() {
		return """
			plugins {
				id("${xcodeIdePluginId}")
			}
		"""
	}

	// Must be kotlin dsl compatible
	String configureXcodeIdeProject(String name, XcodeIdeProductType productType = XcodeIdeProductTypes.APPLICATION) {
		return """
			xcode {
				projects.register("${name}") {
					targets.register("${name.capitalize()}") {
						productReference.set("${name.capitalize()}.app")
						productType.set(productTypes.of("${productType.identifier}"))
						buildConfigurations.register("Default") {
							productLocation.set(layout.projectDirectory.dir("${name.capitalize()}.app"))
							buildSettings.put("PRODUCT_NAME", ideTarget.productName)
						}
					}
				}
			}
		"""
	}

	XcodeIdeWorkspaceFixture xcodeWorkspace(String path) {
		return XcodeIdeWorkspaceFixture.of(file(path))
	}

	XcodeIdeProjectFixture xcodeProject(String path) {
		return XcodeIdeProjectFixture.of(file(path))
	}

	XcodebuildExecutor getXcodebuild() {
		// Gradle needs to be isolated so the xcodebuild does not leave behind daemons
//		assert !executer.usesSharedDaemons()
//		assert executer.usesDaemon()
		return new XcodebuildExecutor(testDirectory)
	}

	UnaryOperator<GradleRunner> getXcodebuildTool(ideTaskName = 'xcode') {
		return { executer ->
			def initScript = file('init.gradle')
			initScript << IdeCommandLineUtils.generateGradleProbeInitFile(ideTaskName, 'xcodebuild')
			return executer
//				.requireIsolatedDaemons()
//				.requireDaemon()
				.usingInitScript(initScript)
		}
	}
}
