package dev.nokee.platform.objectivecpp

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.fixtures.AbstractNativeApplicationComponentFunctionalTest
import dev.nokee.platform.nativebase.fixtures.ObjectiveCppGreeterApp
import spock.lang.Requires
import spock.util.environment.OperatingSystem

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppApplicationComponentFunctionalTest extends AbstractNativeApplicationComponentFunctionalTest {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.objective-cpp-application'
			}

			tasks.withType(LinkExecutable).configureEach {
				linkerArgs.add('-lobjc')
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new ObjectiveCppGreeterApp()
	}
}
