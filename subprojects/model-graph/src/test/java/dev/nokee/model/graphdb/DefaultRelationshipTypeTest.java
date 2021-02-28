package dev.nokee.model.graphdb;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.graphdb.RelationshipType.withName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class DefaultRelationshipTypeTest {
	@Test
	void canCreateRelationshipTypeUsingFactoryMethod() {
		assertThat(withName("knows"), notNullValue(RelationshipType.class));
	}

	@Test
	void canAccessRelationshipTypeName() {
		assertThat(withName("knows").name(), equalTo("knows"));
		assertThat(withName("OWNED_BY").name(), equalTo("OWNED_BY"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(withName("knows"), withName("knows"))
			.addEqualityGroup(withName("OWNED_BY"))
			.testEquals();
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(RelationshipType.class);
	}

	@Test
	void checkToString() {
		assertThat(withName("knows"), hasToString("knows"));
	}
}
