package dev.nokee.model.core;

import com.google.common.collect.Iterables;
import dev.nokee.model.BaseProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public abstract class ModelObjectIntegrationTester<T> implements ModelObjectTester<T> {
	public abstract ModelObject<T> subject();

	@Nested
	class CanCreateProperty implements ModelPropertyTester<BaseProjection> {
		private ModelProperty<BaseProjection> subject;

		@BeforeEach
		void setup() {
			subject = ModelObjectIntegrationTester.this.subject().newProperty("foo", BaseProjection.class);
		}

		@Override
		public ModelProperty<BaseProjection> subject() {
			return subject;
		}

		@Test
		public void hasIdentifier() {
			assertThat(Iterables.getLast(subject().getIdentifier()), equalTo("foo"));
		}

		@Test
		public void hasParent() {
			assertThat(subject().getParent(), optionalWithValue(is(ModelObjectIntegrationTester.this.subject())));
		}

		@Test
		void canReturnsSameModelObjectWhenCreatingTheSameProperty() {
			assertThat(ModelObjectIntegrationTester.this.subject().newProperty("foo", BaseProjection.class), is(subject()));
		}
	}
}
