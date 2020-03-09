package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.WellBehavedPluginTest

class JniLibraryWellBehavedPluginTest extends WellBehavedPluginTest {
	   @Override
	   String getQualifiedPluginId() {
			   return 'dev.nokee.jni-library'
	   }
}
