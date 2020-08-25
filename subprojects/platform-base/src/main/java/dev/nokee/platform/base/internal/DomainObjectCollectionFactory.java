package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.DomainObjectCollection;

public interface DomainObjectCollectionFactory {
	<T> DomainObjectCollection<T> create(Class<T> type);
}
