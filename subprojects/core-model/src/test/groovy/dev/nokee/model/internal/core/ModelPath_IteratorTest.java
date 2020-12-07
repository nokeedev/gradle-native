package dev.nokee.model.internal.core;

import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.core.ModelPath.path;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class ModelPath_IteratorTest {
	@Test
	void canIterateThroughEachPathSegment() {
		assertThat(path("a.b.c"), contains("a", "b", "c"));
	}

	@Test
	void canIterateThroughEachPathSegmentOfParentPath() {
		assertThat(path("x.y.z").getParent(), optionalWithValue(contains("x", "y")));
	}

	@Test
	void canIterateThroughEachPathSegmentOfChildPath() {
		assertThat(path("q.w.e").child("r"), contains("q", "w", "e", "r"));
	}
}
