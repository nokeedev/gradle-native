package dev.nokee.docs.samples

import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement

class JavaObjectiveCppJniLibrarySampleTest extends WellBehavingSampleTest {
	final String sampleName = 'java-objective-cpp-jni-library'
	final ToolChainRequirement toolChainRequirement = ToolChainRequirement.GCC_COMPATIBLE // Not technically right but good enough
}
