package dev.nokee.docs.samples.jni

import dev.nokee.docs.samples.WellBehavingSampleTest

class JniLibraryComposingFromPrebuiltBinariesSampleTest extends WellBehavingSampleTest {
	@Override
	protected String getSampleName() {
		return 'jni-library-composing-from-pre-built-binaries'
	}

	List<String> getExpectedAdditionalExtensions() {
		return ['cpp'] // We include the source that produce the pre-built binaries
	}
}
