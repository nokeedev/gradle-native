package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelNodes;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public abstract class DomainObjectFunctorModelNodeTester<F> extends AbstractDomainObjectFunctorTester<F> {
	@Test
	void canGetBackingNodeFromInstance() {
		val node = node(new MyType());
		assertThat(ModelNodes.of(createSubject(MyType.class, node)), equalTo(node));
	}

	static final class MyType {}
}
