package dev.nokee.docs.samples

import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement

class JavaObjectiveCJniLibrarySampleTest extends WellBehavingSampleTest {
	final String sampleName = 'java-objective-c-jni-library'
	final ToolChainRequirement toolChainRequirement = ToolChainRequirement.GCC_COMPATIBLE // Not technically right but good enough
}
