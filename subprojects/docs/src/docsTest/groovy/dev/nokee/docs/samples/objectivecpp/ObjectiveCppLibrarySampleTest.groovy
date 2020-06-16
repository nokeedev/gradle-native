package dev.nokee.docs.samples.objectivecpp

import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.docs.samples.WellBehavingSampleTest
import org.apache.commons.lang3.SystemUtils

import static org.junit.Assume.assumeFalse

class ObjectiveCppLibrarySampleTest extends WellBehavingSampleTest {
	@Override
	protected String getSampleName() {
		return 'objective-cpp-library'
	}

	@Override
	protected ToolChainRequirement getToolChainRequirement() {
		assumeFalse(SystemUtils.IS_OS_WINDOWS)
		return ToolChainRequirement.GCC_COMPATIBLE // Not technically right but good enough
	}
}
