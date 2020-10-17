package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectFactoryRegistry;
import dev.nokee.model.DomainObjectInstantiator;

import java.util.Set;

public interface PolymorphicDomainObjectInstantiator<T> extends DomainObjectInstantiator<T>, DomainObjectFactoryRegistry<T> {
	Set<? extends Class<? extends T>> getCreatableTypes();
	void assertCreatableType(Class<?> type);
}
