package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.internal.ComponentName;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.platform.base.internal.dependencies.ConfigurationNamingScheme.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Subject(ConfigurationNamingScheme.class)
class ConfigurationNamingScheme_ForComponentTest {
	@Test
	void isAliasForIdentityWhenComponentNameIsMain() {
		assertThat(forComponent(ComponentName.ofMain()), equalTo(identity()));
	}

	@Test
	void isAliasForPrefixWithComponentNameWhenComponentIsNotMain() {
		assertThat(forComponent(ComponentName.of("test")), equalTo(prefixWith("test")));
	}
}
