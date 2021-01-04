package dev.nokee.platform.base.internal.dependencies;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.platform.base.internal.dependencies.ConfigurationNamingScheme.identity;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationNamingScheme.prefixWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Subject(ConfigurationNamingScheme.class)
class ConfigurationNamingScheme_IdentityTest {
	@Test
	void returnsNameUnchanged() {
		assertThat(identity().configurationName("foo"), equalTo("foo"));
		assertThat(identity().configurationName("bar"), equalTo("bar"));
		assertThat(identity().configurationName("Far"), equalTo("Far"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicInstanceMethods(identity());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(identity(), identity())
			.addEqualityGroup(prefixWith("foo"))
			.testEquals();
	}
}
