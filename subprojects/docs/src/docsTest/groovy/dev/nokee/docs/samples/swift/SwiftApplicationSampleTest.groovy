package dev.nokee.docs.samples.swift

import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.docs.samples.WellBehavingSampleTest

class SwiftApplicationSampleTest extends WellBehavingSampleTest {
	@Override
	protected String getSampleName() {
		return 'swift-application'
	}

	@Override
	protected ToolChainRequirement getToolChainRequirement() {
		return ToolChainRequirement.SWIFTC
	}
}
