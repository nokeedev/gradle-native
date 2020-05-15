package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib

class VariantAwareNativeComponentFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def "do not realize variants for unrelated tasks"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << """
			def configuredVariants = []
			library {
				variants.configureEach {
					configuredVariants << it
				}
			}

            gradle.buildFinished {
                assert configuredVariants == []
            }
		"""

		expect:
		succeeds('help')
	}

	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}
		'''
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	protected SourceElement getComponentUnderTest() {
		return new JavaJniCppGreeterLib('jni-greeter')
	}
}
