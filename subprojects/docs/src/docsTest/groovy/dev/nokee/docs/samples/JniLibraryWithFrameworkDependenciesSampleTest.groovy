package dev.nokee.docs.samples

import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import org.apache.commons.lang3.SystemUtils
import org.junit.Assume

class JniLibraryWithFrameworkDependenciesSampleTest extends WellBehavingSampleTest {
	@Override
	protected String getSampleName() {
		return 'jni-library-with-framework-dependencies'
	}

	@Override
	protected ToolChainRequirement getToolChainRequirement() {
		Assume.assumeTrue(SystemUtils.IS_OS_MAC)
		return ToolChainRequirement.GCC_COMPATIBLE // Not technically right but good enough
	}
}
