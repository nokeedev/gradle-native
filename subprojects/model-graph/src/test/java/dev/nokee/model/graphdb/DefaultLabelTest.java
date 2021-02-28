package dev.nokee.model.graphdb;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.graphdb.Label.label;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class DefaultLabelTest {
	@Test
	void canCreateLabelUsingFactoryMethod() {
		assertThat(label("component"), notNullValue(Label.class));
	}

	@Test
	void canAccessLabelName() {
		assertThat(label("sourceSet").name(), equalTo("sourceSet"));
		assertThat(label("variant").name(), equalTo("variant"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(label("component"), label("component"))
			.addEqualityGroup(label("variant"))
			.testEquals();
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(Label.class);
	}

	@Test
	void checkToString() {
		assertThat(label("component"), hasToString("component"));
	}
}
