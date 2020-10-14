package dev.nokee.platform.base.internal.binaries


import dev.nokee.model.internal.AbstractDomainObjectConfigurerTest
import dev.nokee.model.internal.DomainObjectConfigurer
import dev.nokee.platform.base.Binary
import spock.lang.Subject

@Subject(BinaryConfigurer)
class BinaryConfigurerTest extends AbstractDomainObjectConfigurerTest<Binary> implements BinaryFixture {
	@Override
	protected DomainObjectConfigurer<Binary> newSubject() {
		return new BinaryConfigurer(eventPublisher)
	}
}
