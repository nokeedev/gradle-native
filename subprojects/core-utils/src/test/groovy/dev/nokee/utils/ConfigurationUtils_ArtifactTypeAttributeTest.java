package dev.nokee.utils;

import org.junit.jupiter.api.Test;

import static dev.nokee.utils.ConfigurationUtils.ARTIFACT_TYPE_ATTRIBUTE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class ConfigurationUtils_ArtifactTypeAttributeTest {
	@Test
	void checkArtifactTypeAttributeName() {
		assertThat(ARTIFACT_TYPE_ATTRIBUTE.getName(), equalTo("artifactType"));
	}

	@Test
	void checkArtifactTypeAttributeType() {
		assertThat(ARTIFACT_TYPE_ATTRIBUTE.getType(), is(String.class));
	}
}
