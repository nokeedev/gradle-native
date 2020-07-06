package dev.nokee.ide.xcode

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.language.objectivec.ObjectiveCTaskNames
import dev.nokee.platform.jni.fixtures.ObjectiveCGreeter
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.SharedLibraryBinary
import dev.nokee.platform.nativebase.fixtures.ObjectiveCGreeterApp

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

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.allToLink
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

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.allToLink
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

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.forStaticLibrary.allToCreate
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
	protected List<String> getAllTasksForBuildAction() {
		return tasks.withLinkage('shared').allToLink
	}

	@Override
	protected String getSchemeName() {
		return "${super.getSchemeName()}Shared"
	}
}
