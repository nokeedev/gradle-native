package dev.nokee.platform.base.internal.dependencies;

import com.google.common.testing.NullPointerTester;
import dev.nokee.platform.base.internal.ComponentName;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

@Subject(ConfigurationNamingScheme.class)
class ConfigurationNamingSchemeTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester()
			.setDefault(ComponentName.class, ComponentName.ofMain())
			.testAllPublicStaticMethods(ConfigurationNamingScheme.class);
	}
}
