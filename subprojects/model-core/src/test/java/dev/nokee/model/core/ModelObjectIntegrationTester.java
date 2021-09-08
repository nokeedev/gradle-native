package dev.nokee.model.core;

import com.google.common.collect.Iterables;
import dev.nokee.internal.testing.SequentialTestNames;
import dev.nokee.model.BaseProjection;
import dev.nokee.model.streams.ModelStream;
import dev.nokee.model.streams.ModelStreamTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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

		@Test
		void hasEmptyPropertiesStream() {
			assertThat(subject().getProperties().collect(Collectors.toList()), providerOf(emptyIterable()));
		}

		@Nested
		class PropertiesStreamTest implements ModelStreamTester<ModelProperty<?>> {
			private final SequentialTestNames names = new SequentialTestNames();
			@Override
			public ModelStream<ModelProperty<?>> createSubject() {
				return ModelObjectIntegrationTester.this.subject().property("foo", BaseProjection.class).getProperties();
			}

			@Override
			public ModelProperty<?> createElement() {
				return subject().newProperty(names.next(), BaseProjection.class);
			}
		}
	}

	@Nested
	class PropertiesStreamTest implements ModelStreamTester<ModelProperty<?>> {
		private final SequentialTestNames names = new SequentialTestNames();
		@Override
		public ModelStream<ModelProperty<?>> createSubject() {
			return subject().getProperties();
		}

		@Override
		public ModelProperty<?> createElement() {
			return subject().newProperty(names.next(), BaseProjection.class);
		}
	}
}
