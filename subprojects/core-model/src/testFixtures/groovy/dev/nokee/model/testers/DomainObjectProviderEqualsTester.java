package dev.nokee.model.testers;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

public abstract class DomainObjectProviderEqualsTester<T> extends AbstractDomainObjectProviderTester<T> {
	@Test
	void canEquals() {
		new EqualsTester()
			.addEqualityGroup(p0(), p0())
			.addEqualityGroup(p1())
			.addEqualityGroup(p2())
			.testEquals();
	}
}
