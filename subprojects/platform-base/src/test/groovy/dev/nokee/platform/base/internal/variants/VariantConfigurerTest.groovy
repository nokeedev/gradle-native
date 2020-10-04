package dev.nokee.platform.base.internal.variants

import dev.nokee.model.internal.AbstractDomainObjectConfigurerTest
import dev.nokee.model.internal.DomainObjectConfigurer
import dev.nokee.model.internal.DomainObjectProviderFactory
import dev.nokee.platform.base.Variant
import spock.lang.Subject

@Subject(VariantConfigurer)
class VariantConfigurerTest extends AbstractDomainObjectConfigurerTest<Variant> implements VariantFixture {
	@Override
	protected DomainObjectConfigurer<Variant> newSubject() {
		return new VariantConfigurer(eventPublisher)
	}
}
