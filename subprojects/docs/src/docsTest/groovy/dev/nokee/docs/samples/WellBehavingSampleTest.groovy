package dev.nokee.docs.samples

import dev.gradleplugins.spock.lang.CleanupTestDirectory
import dev.gradleplugins.spock.lang.TestNameTestDirectoryProvider
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter
import org.junit.Rule
import spock.lang.Specification

@CleanupTestDirectory
abstract class WellBehavingSampleTest extends Specification {
	@Rule
	final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider()

	protected GradleExecuter configureLocalPluginResolution(GradleExecuter executer) {
		def initScriptFile = temporaryFolder.file('repo.init.gradle')
		initScriptFile << """
			settingsEvaluated { settings ->
				settings.pluginManagement {
					repositories {
						maven {
							name = 'docs'
							url = '${System.getProperty('dev.nokee.docsRepository')}'
						}
					}
				}
			}
		"""
		return executer.usingInitScript(initScriptFile)
	}
}
