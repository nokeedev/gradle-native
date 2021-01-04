package dev.nokee.platform.base.internal.dependencies;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Bucket.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasToString;

@Subject(ConfigurationDescription.Bucket.class)
class ConfigurationDescriptionBucketTest {
	@Test
	void resolveToDependenciesForDeclarableBucketOnly() {
		assertThat(ofDeclarable(), hasToString("dependencies"));
		assertThat(ofResolvable(), hasToString(emptyString()));
		assertThat(ofConsumable(), hasToString(emptyString()));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(ConfigurationDescription.Bucket.class);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(ofDeclarable(), ofDeclarable())
			.addEqualityGroup(ofResolvable(), ofConsumable())
			.testEquals();
	}
}
