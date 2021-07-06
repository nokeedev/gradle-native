package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectIdentifierTester;
import dev.nokee.model.core.ModelProjection;

class DefaultDomainObjectIdentifierTest implements DomainObjectIdentifierTester {
	@Override
	public DomainObjectIdentifier createSubject(ModelProjection projection) {
		return new DefaultDomainObjectIdentifier(projection);
	}
}
