package dev.nokee.model.dsl;

import dev.nokee.internal.testing.Assumptions;
import dev.nokee.model.core.*;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.model.dsl.ModelNodeTestUtils.*;
import static dev.nokee.model.dsl.ModelNodeTestUtils.projectionOf;
import static dev.nokee.model.dsl.NodePredicates.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;

class NodePredicatesTest {
	@Nested
	class DirectDescendantPredicate implements NodePredicateDirectDescendantTester {
		@Override
		public NodePredicate<Object> createSubject() {
			return directChildren();
		}

		@Override
		public <T> NodePredicate<T> createSubject(ModelSpec<T> spec) {
			return directChildren(spec);
		}

		@Test
		void checkToString() {
			assertAll(
				() -> assertThat(directChildren(),
					hasToString("NodePredicates.directChildren()")),
				() -> assertThat(directChildren(specOf(String.class)),
					hasToString("NodePredicates.directChildren(test spec for 'java.lang.String')"))
			);
		}
	}

	@Nested
	class DescendantPredicate implements NodePredicateDescendantTester {
		@Override
		public NodePredicate<Object> createSubject() {
			return descendants();
		}

		@Override
		public <T> NodePredicate<T> createSubject(ModelSpec<T> spec) {
			return descendants(spec);
		}

		@Test
		void checkToString() {
			assertAll(
				() -> assertThat(descendants(),
					hasToString("NodePredicates.descendants()")),
				() -> assertThat(descendants(specOf(String.class)),
					hasToString("NodePredicates.descendants(test spec for 'java.lang.String')"))
			);
		}
	}

	@Nested
	class OfType {
		@Nested
		class ModelSpecTest implements ModelSpecTester<Child> {
			@Override
			public ModelSpec<Child> createSubject() {
				return NodePredicates.ofType(Child.class);
			}

			@Override
			public ModelProjection trueObject() {
				val result = Mockito.mock(ModelProjection.class);
				Mockito.when(result.canBeViewedAs(Child.class)).thenReturn(true);
				return result;
			}

			@Override
			public ModelProjection falseObject() {
				val result = Mockito.mock(ModelProjection.class);
				Mockito.when(result.canBeViewedAs(Child.class)).thenReturn(false);
				return result;
			}

			@Override
			public ModelSpec<Child> alwaysTrueSpec() {
				return new ModelSpec<Child>() {
					@Override
					public boolean isSatisfiedBy(ModelProjection node) {
						return true;
					}

					@Override
					public Class<Child> getProjectionType() {
						return Child.class;
					}
				};
			}

			@Override
			public ModelSpec<Child> alwaysFalseSpec() {
				return new ModelSpec<Child>() {
					@Override
					public boolean isSatisfiedBy(ModelProjection node) {
						return false;
					}

					@Override
					public Class<Child> getProjectionType() {
						return Child.class;
					}
				};
			}
		}

		@Nested
		class NodePredicateTest implements NodePredicateAllTester {

			@Override
			public NodePredicate<Object> createSubject() {
				return NodePredicates.ofType(Object.class);
			}

			@Override
			public <T> NodePredicate<T> createSubject(ModelSpec<T> spec) {
				return Assumptions.skipCurrentTestExecution("");
			}
		}

		/** @see NodePredicateTester#hasProjectionTypeOfUnderlyingSpec() */
		@Test
		void projectionTypeFromPredicate() {
			assertThat(NodePredicates.ofType(String.class).scope(rootNode()).getProjectionType(), is(String.class));
		}

		/** @see NodePredicateAllTester#returnsSpecResultWhenProjectionIsInScope() */
		@Test
		void returnsSpecSatisfiedBy() {
			val ancestor = rootNode();
			val inScope = childNodeOf(ancestor);
			val spec = NodePredicates.ofType(String.class).scope(inScope);
			val projection = projectionOf(inScope);
			Mockito.when(projection.canBeViewedAs(any())).thenReturn(true, false);
			assertAll(
				() -> assertThat(spec.isSatisfiedBy(projection), is(true)),
				() -> assertThat(spec.isSatisfiedBy(projection), is(false))
			);
		}
	}

	interface Base {}
	interface Child extends Base {}
}
