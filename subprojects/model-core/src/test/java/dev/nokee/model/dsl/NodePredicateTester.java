package dev.nokee.model.dsl;

import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.dsl.ModelNodeTestUtils.*;
import static dev.nokee.utils.FunctionalInterfaceMatchers.neverCalled;
import static dev.nokee.utils.SpecTestUtils.mockSpec;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Deprecated
public interface NodePredicateTester {
	NodePredicate<Object> createSubject();
	<T> NodePredicate<T> createSubject(ModelSpec<T> spec);

	@Test
	default void hasProjectionTypeOfUnderlyingSpec() {
		assertThat(createSubject(specOf(String.class)).scope(rootNode()).getProjectionType(), is(String.class));
	}

	@Test
	default void doesNotQuerySpecWhenProjectionNotInScope() {
		val spec = mockSpec();
		val ancestor = rootNode();
		val inScope = childNodeOf(ancestor);
		createSubject(specOf(String.class, spec)).scope(inScope).test(projectionOf(ancestor));
		assertThat(spec, neverCalled());
	}

	@Test
	default void defaultProjectionTypeIsObject() {
		assertThat(createSubject().scope(rootNode()).getProjectionType(), is(Object.class));
	}

	@Test
	default void doesNotMatchProjectionOfAncestorNode() {
		val ancestor = rootNode();
		val directAncestor = childNodeOf(ancestor);
		val inScope = childNodeOf(directAncestor);
		assertThat(createSubject().scope(inScope).test(projectionOf(ancestor)), is(false));
	}

	@Test
	default void doesNotMatchProjectionOfDirectAncestor() {
		val directAncestor = rootNode();
		val inScope = childNodeOf(directAncestor);
		assertThat(createSubject().scope(inScope).test(projectionOf(directAncestor)), is(false));
	}
}
