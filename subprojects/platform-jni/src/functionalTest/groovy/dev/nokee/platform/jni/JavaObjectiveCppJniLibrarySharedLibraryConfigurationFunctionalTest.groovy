package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.JavaJniObjectiveCppGreeterLib
import spock.lang.Requires
import spock.util.environment.OperatingSystem

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class JavaObjectiveCppJniLibrarySharedLibraryConfigurationFunctionalTest extends AbstractSharedLibraryConfigurationFunctionalTest {
	@Override
	protected String getCompileTaskName() {
		return "compileMainSharedLibraryMainObjcpp"
	}

	@Override
	protected void makeSingleProject() {
		settingsFile << "rootProject.name = 'jni-greeter'"
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.objective-cpp-language'
			}

			library.variants.configureEach {
				sharedLibrary.linkTask.configure {
					linkerArgs.add('-lobjc')
				}
			}
		'''
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniObjectiveCppGreeterLib('jni-greeter').withOptionalFeature()
	}
}
