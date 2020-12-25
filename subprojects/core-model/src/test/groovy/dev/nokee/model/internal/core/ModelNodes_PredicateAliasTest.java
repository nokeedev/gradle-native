package dev.nokee.model.internal.core;

import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelNode.State.Realized;
import static dev.nokee.model.internal.core.ModelNode.State.Registered;
import static dev.nokee.model.internal.core.ModelNodes.*;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ModelNodes_PredicateAliasTest {
	@Test
	void checkDiscoverAlias() {
		assertThat(discover(), equalTo(stateOf(Registered)));
	}

	@Test
	void checkDiscoverOfTypeAlias() {
		assertThat(discover(of(MyType.class)), equalTo(stateOf(Registered).and(withType(of(MyType.class)))));
	}

	@Test
	void checkMutateOfTypeAlias() {
		assertThat(mutate(of(MyType.class)), equalTo(stateOf(Realized).and(withType(of(MyType.class)))));
	}

	interface MyType {}
}
