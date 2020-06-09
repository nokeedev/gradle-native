package dev.nokee.docs.samples.objectivec

import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.docs.samples.WellBehavingSampleTest

class ObjectiveCApplicationSampleTest extends WellBehavingSampleTest {
	@Override
	protected String getSampleName() {
		return 'objective-c-application'
	}

	@Override
	protected ToolChainRequirement getToolChainRequirement() {
		return ToolChainRequirement.GCC_COMPATIBLE // Not technically right but good enough
	}
}
