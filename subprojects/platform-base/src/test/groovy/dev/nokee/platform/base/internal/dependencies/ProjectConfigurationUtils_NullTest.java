package dev.nokee.platform.base.internal.dependencies;

import com.google.common.testing.NullPointerTester;
import org.gradle.api.attributes.Attribute;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

@Subject(ProjectConfigurationUtils.class)
class ProjectConfigurationUtils_NullTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester()
			.setDefault(Attribute.class, Attribute.of(MyAttribute.class))
			.testAllPublicStaticMethods(ProjectConfigurationRegistry.class);
	}

	interface MyAttribute {}
}
