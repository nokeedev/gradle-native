package dev.nokee.model.testers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class DomainObjectProviderGetTypeTester<T> extends AbstractDomainObjectProviderTester<T> {
	@Test
	void canGetProviderType() {
		Assertions.assertAll(() -> {
			assertEquals(p0().p().getType(), p0().type());
			assertEquals(p1().p().getType(), p1().type());
			assertEquals(p2().p().getType(), p2().type());
		});
	}
}
