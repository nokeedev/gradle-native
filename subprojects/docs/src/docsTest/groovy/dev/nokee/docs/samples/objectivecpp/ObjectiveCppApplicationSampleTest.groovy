package dev.nokee.docs.samples.objectivecpp

import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.docs.samples.WellBehavingSampleTest

class ObjectiveCppApplicationSampleTest extends WellBehavingSampleTest {
	@Override
	protected String getSampleName() {
		return 'objective-cpp-application'
	}

	@Override
	protected ToolChainRequirement getToolChainRequirement() {
		return ToolChainRequirement.GCC_COMPATIBLE // Not technically right but good enough
	}
}
