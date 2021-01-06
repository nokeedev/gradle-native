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
class ConfigurationNamingScheme_PrefixWithTest {
	@Test
	void capitalizeNameBeforePrefixing() {
		assertThat(prefixWith("foo").configurationName("bar"), equalTo("fooBar"));
		assertThat(prefixWith("foo").configurationName("far"), equalTo("fooFar"));
		assertThat(prefixWith("bar").configurationName("far"), equalTo("barFar"));
	}

	@Test
	void appendOnlyWhenConfigurationNameAlreadyCapitalized() {
		assertThat(prefixWith("foo").configurationName("Bar"), equalTo("fooBar"));
		assertThat(prefixWith("foo").configurationName("Far"), equalTo("fooFar"));
		assertThat(prefixWith("bar").configurationName("Far"), equalTo("barFar"));
	}

	@Test
	void returnsIdentityNamingSchemeForEmptyPrefix() {
		assertThat(prefixWith(""), equalTo(identity()));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicInstanceMethods(prefixWith("foo"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(prefixWith("foo"), prefixWith("foo"))
			.addEqualityGroup(prefixWith("bar"))
			.addEqualityGroup(identity())
			.testEquals();
	}
}
