package dev.nokee.model.testers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class DomainObjectProviderGetIdentifierTester<T> extends AbstractDomainObjectProviderTester<T> {
	@Test
	void canGetProviderIdentifier() {
		Assertions.assertAll(() -> {
			assertEquals(p0().p().getIdentifier(), p0().id());
			assertEquals(p1().p().getIdentifier(), p1().id());
			assertEquals(p2().p().getIdentifier(), p2().id());
		});
	}
}
