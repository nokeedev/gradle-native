package dev.nokee.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelNode.State.Realized;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class AbstractDomainObjectViewElementQueryTester<T> extends AbstractDomainObjectViewTester<T> {
	private final DomainObjectView<T> subject = createSubject();

	protected abstract Iterable<? extends T> query(DomainObjectView<T> subject);

	@BeforeEach
	void addElements() {
		elements("e1", "e2");
	}

	// provider returns assertions
	@Test
	void returnsCurrentElementsWhenQueried() {
		assertThat("all registered elements should be returned",
			query(subject), contains(e("e1"), e("e2")));
	}

	@Test
	void doesNotReturnsWrongElementTypesWhenProviderIsQueried() {
		element("foo", WrongType.class);
		assertThat("all registered elements should be returned",
			(Iterable<Object>)query(subject), not(hasItem(e("foo"))));
		// TODO: Remove cast, maybe map (or whatever generic provider returning method) should return Provider<Iterable<Object>>
	}

	// model node assertions
	@Test
	void modelNodesAreRealizedWhenProviderIsQueried() {
		query(subject);
		assertThat("model nodes should realize when provider is queried",
			elementNodes(stateAtLeast(Realized)), contains(node("e1"), node("e2")));
	}

	@Test
	void futureElementsAreNotRealizedAfterProviderIsQueried() {
		query(subject);
		elements("e3", "e4");
		assertThat("future elements should not be realized",
			elementNodes(stateAtLeast(Realized).negate()), contains(node("e3"), node("e4")));
	}

	@Test
	void wrongElementTypesAreNotRealized() {
		element("foo", WrongType.class);
		query(subject);
		assertThat("unscoped elements should not be realized",
			elementNodes(stateAtLeast(Realized).negate()), not(hasItem(node("foo"))));
	}

	interface WrongType {}
}
