package dev.nokee.model.core;

import dev.nokee.model.KnownDomainObjectTester;

public interface ModelObjectTester<T> extends KnownDomainObjectTester<T> {
	ModelObject<T> subject();
}
