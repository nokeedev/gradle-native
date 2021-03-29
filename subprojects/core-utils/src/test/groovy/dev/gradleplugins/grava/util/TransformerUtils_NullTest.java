package dev.gradleplugins.grava.util;

import com.google.common.testing.NullPointerTester;
import dev.gradleplugins.grava.util.TransformerUtils;
import org.junit.jupiter.api.Test;

class TransformerUtils_NullTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(TransformerUtils.class);
	}
}
