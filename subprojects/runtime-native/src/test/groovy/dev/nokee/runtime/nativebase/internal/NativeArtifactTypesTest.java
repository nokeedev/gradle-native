package dev.nokee.runtime.nativebase.internal;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class NativeArtifactTypesTest {
	@Test // Some tests depends on this constant
	void hasNativeHeadersDirectoryType() {
		assertThat(NativeArtifactTypes.NATIVE_HEADERS_DIRECTORY, equalTo("native-headers-directory"));
	}
}
