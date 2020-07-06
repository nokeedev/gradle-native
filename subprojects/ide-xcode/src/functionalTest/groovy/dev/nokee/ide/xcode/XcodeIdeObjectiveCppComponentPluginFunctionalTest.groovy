package dev.nokee.ide.xcode

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.language.objectivecpp.ObjectiveCppTaskNames
import dev.nokee.platform.jni.fixtures.ObjectiveCppGreeter
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.SharedLibraryBinary
import dev.nokee.platform.nativebase.fixtures.ObjectiveCppGreeterApp

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
