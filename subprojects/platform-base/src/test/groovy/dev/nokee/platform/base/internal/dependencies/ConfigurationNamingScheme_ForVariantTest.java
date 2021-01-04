package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.internal.ComponentName;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.platform.base.internal.dependencies.ConfigurationNamingScheme.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Subject(ConfigurationNamingScheme.class)
class ConfigurationNamingScheme_ForVariantTest {
	@Test
	void isAliasForIdentityWhenComponentNameIsMainAndVariantNameIsEmpty() {
		assertThat(forVariant(ComponentName.ofMain(), ""), equalTo(identity()));
	}

	@Test
	void isAliasForPrefixWithComponentNameWhenComponentIsNotMainAndVariantNameIsEmpty() {
		assertThat(forVariant(ComponentName.of("test"), ""), equalTo(prefixWith("test")));
	}

	@Test
	void isAliasForPrefixWithComponentNameAndVariantNameWhenComponentIsNotMainAndVariantNameIsNotEmpty() {
		assertThat(forVariant(ComponentName.of("test"), "macos"), equalTo(prefixWith("testMacos")));
	}

	@Test
	void isAliasForPrefixWithVariantNameWhenComponentIsMainAndVariantNameIsNotEmpty() {
		assertThat(forVariant(ComponentName.ofMain(), "macos"), equalTo(prefixWith("macos")));
	}
}
