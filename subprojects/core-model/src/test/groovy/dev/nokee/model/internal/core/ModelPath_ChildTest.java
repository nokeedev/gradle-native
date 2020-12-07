package dev.nokee.model.internal.core;

import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelPath.root;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelPath_ChildTest {
	@Test
	void canCreateChildPath() {
		assertEquals(path("a.b.c"), path("a.b").child("c"), "child path should be the same as an absolute path");
	}

	@Test
	void canGetParentOfChildPath() {
		assertThat(path("a.b").child("c").getParent(), optionalWithValue(equalTo(path("a.b"))));
	}

	@Test
	void canCreateChildPathFromRootPath() {
		assertEquals(path("a"), root().child("a"), "child path from root should be the same as an absolute path");
	}
}
