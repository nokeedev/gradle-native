package dev.nokee.ide.xcode

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.language.objectivecpp.ObjectiveCppTaskNames
import dev.nokee.platform.jni.fixtures.ObjectiveCppGreeter
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.SharedLibraryBinary
import dev.nokee.platform.nativebase.fixtures.ObjectiveCppGreeterApp
import dev.nokee.platform.nativebase.fixtures.ObjectiveCppGreeterLib
import dev.nokee.platform.nativebase.fixtures.ObjectiveCppGreeterTest
import dev.nokee.testing.nativebase.NativeTestSuite
import org.junit.Assume

class XcodeIdeObjectiveCppApplicationFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest implements ObjectiveCppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.objective-cpp-application'
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
		return new ObjectiveCppGreeterApp()
	}

	@Override
	protected String getProjectName() {
		return "app"
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.allToLink
	}
}

class XcodeIdeObjectiveCppApplicationWithNativeTestSuiteFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest implements ObjectiveCppTaskNames {
	@Override
	protected void makeSingleProject() {
		makeSingleProjectWithoutSources()
		new ObjectiveCppGreeterApp().writeToProject(testDirectory)
	}

	@Override
	protected void makeSingleProjectWithoutSources() {
		buildFile << """
			plugins {
				id 'dev.nokee.objective-cpp-application'
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
		return new ObjectiveCppGreeterTest()
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

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return [tasks.compile] + tasks.withComponentName('test').allToLink + [tasks.withComponentName('test').relocateMainSymbol]
	}
}

class XcodeIdeObjectiveCppLibraryFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest implements ObjectiveCppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.objective-cpp-library'
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
		return new ObjectiveCppGreeter().asLib()
	}

	@Override
	protected String getProjectName() {
		return "lib"
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.allToLink
	}
}

class XcodeIdeObjectiveCppLibraryWithNativeTestSuiteFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest implements ObjectiveCppTaskNames {
	@Override
	protected void makeSingleProject() {
		makeSingleProjectWithoutSources()
		new ObjectiveCppGreeterLib().writeToProject(testDirectory)
	}

	@Override
	protected void makeSingleProjectWithoutSources() {
		buildFile << """
			plugins {
				id 'dev.nokee.objective-cpp-library'
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
		return new ObjectiveCppGreeterTest()
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

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return [tasks.compile] + tasks.withComponentName('test').allToLink
	}
}

class XcodeIdeObjectiveCppLibraryWithStaticLinkageFunctionalTest extends XcodeIdeObjectiveCppLibraryFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << """
			library {
				targetLinkages = [linkages.static]
			}
		"""
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.forStaticLibrary.allToCreate
	}
}

class XcodeIdeObjectiveCppLibraryWithSharedLinkageFunctionalTest extends XcodeIdeObjectiveCppLibraryFunctionalTest {
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

class XcodeIdeObjectiveCppLibraryWithBothLinkageFunctionalTest extends XcodeIdeObjectiveCppLibraryFunctionalTest {
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
	protected List<String> getAllTasksForBuildAction() {
		return tasks.withLinkage('shared').allToLink
	}

	@Override
	protected String getSchemeName() {
		return "${super.getSchemeName()}Shared"
	}
}
