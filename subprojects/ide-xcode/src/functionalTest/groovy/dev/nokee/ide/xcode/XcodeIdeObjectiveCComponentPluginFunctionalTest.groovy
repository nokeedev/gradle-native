package dev.nokee.ide.xcode

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.language.objectivec.ObjectiveCTaskNames
import dev.nokee.platform.jni.fixtures.ObjectiveCGreeter
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.SharedLibraryBinary
import dev.nokee.platform.nativebase.fixtures.ObjectiveCGreeterApp
import dev.nokee.platform.nativebase.fixtures.ObjectiveCGreeterLib
import dev.nokee.platform.nativebase.fixtures.ObjectiveCGreeterTest
import dev.nokee.testing.nativebase.NativeTestSuite
import org.junit.Assume

class XcodeIdeObjectiveCApplicationFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest implements ObjectiveCTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.objective-c-application'
				id 'dev.nokee.xcode-ide'
			}

			import ${ExecutableBinary.canonicalName}
			application.variants.configureEach {
				binaries.configureEach(ExecutableBinary) {
					linkTask.configure {
						linkerArgs.add('-lobjc')
					}
				}
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new ObjectiveCGreeterApp()
	}

	@Override
	protected String getProjectName() {
		return "app"
	}
}

class XcodeIdeObjectiveCApplicationWithNativeTestSuiteFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest implements ObjectiveCTaskNames {
	@Override
	protected void makeSingleProject() {
		makeSingleProjectWithoutSources()
		new ObjectiveCGreeterApp().writeToProject(testDirectory)
	}

	@Override
	protected void makeSingleProjectWithoutSources() {
		buildFile << """
			plugins {
				id 'dev.nokee.objective-c-application'
				id 'dev.nokee.xcode-ide'
				id 'dev.nokee.native-unit-testing'
			}

			import ${ExecutableBinary.canonicalName}
			application.variants.configureEach {
				binaries.configureEach(ExecutableBinary) {
					linkTask.configure {
						linkerArgs.add('-lobjc')
					}
				}
			}

			import ${NativeTestSuite.canonicalName}

			testSuites {
				test(${NativeTestSuite.simpleName}) {
					testedComponent application
					binaries.configureEach(${ExecutableBinary.simpleName}) {
						linkTask.configure {
							linkerArgs.add('-lobjc')
						}
					}
				}
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new ObjectiveCGreeterTest()
	}

	@Override
	protected String configureCustomSourceLayout() {
		Assume.assumeTrue(false)
		return super.configureCustomSourceLayout()
	}

	@Override
	protected String getProjectName() {
		return 'app'
	}

	@Override
	protected String getGroupName() {
		return 'app-test'
	}
}

class XcodeIdeObjectiveCLibraryFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest implements ObjectiveCTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.objective-c-library'
				id 'dev.nokee.xcode-ide'
			}

			import ${SharedLibraryBinary.canonicalName}
			library.variants.configureEach {
				binaries.configureEach(SharedLibraryBinary) {
					linkTask.configure {
						linkerArgs.add('-lobjc')
					}
				}
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new ObjectiveCGreeter().asLib()
	}

	@Override
	protected String getProjectName() {
		return "lib"
	}
}

class XcodeIdeObjectiveCLibraryWithNativeTestSuiteFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest implements ObjectiveCTaskNames {
	@Override
	protected void makeSingleProject() {
		makeSingleProjectWithoutSources()
		new ObjectiveCGreeterLib().writeToProject(testDirectory)
	}

	@Override
	protected void makeSingleProjectWithoutSources() {
		buildFile << """
			plugins {
				id 'dev.nokee.objective-c-library'
				id 'dev.nokee.xcode-ide'
				id 'dev.nokee.native-unit-testing'
			}

			import ${SharedLibraryBinary.canonicalName}
			library.variants.configureEach {
				binaries.configureEach(SharedLibraryBinary) {
					linkTask.configure {
						linkerArgs.add('-lobjc')
					}
				}
			}

			import ${NativeTestSuite.canonicalName}
			import ${ExecutableBinary.canonicalName}

			testSuites {
				test(${NativeTestSuite.simpleName}) {
					testedComponent library
					binaries.configureEach(${ExecutableBinary.simpleName}) {
						linkTask.configure {
							linkerArgs.add('-lobjc')
						}
					}
				}
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new ObjectiveCGreeterTest()
	}

	@Override
	protected String configureCustomSourceLayout() {
		Assume.assumeTrue(false)
		return super.configureCustomSourceLayout()
	}

	@Override
	protected String getProjectName() {
		return 'lib'
	}

	@Override
	protected String getGroupName() {
		return 'lib-test'
	}
}

class XcodeIdeObjectiveCLibraryWithStaticLinkageFunctionalTest extends XcodeIdeObjectiveCLibraryFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << """
			library {
				targetLinkages = [linkages.static]
			}
		"""
	}
}

class XcodeIdeObjectiveCLibraryWithSharedLinkageFunctionalTest extends XcodeIdeObjectiveCLibraryFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << """
			library {
				targetLinkages = [linkages.shared]
			}
		"""
	}
}

class XcodeIdeObjectiveCLibraryWithBothLinkageFunctionalTest extends XcodeIdeObjectiveCLibraryFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << """
			library {
				targetLinkages = [linkages.static, linkages.shared]
			}
		"""
	}

	@Override
	protected String getSchemeName() {
		return "${super.getSchemeName()}Shared"
	}
}
