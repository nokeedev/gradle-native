package dev.nokee.platform.base;

import dev.nokee.platform.base.internal.DomainObjectFactory;
import dev.nokee.platform.base.internal.DomainObjectIdentity;
import org.gradle.api.Action;

public interface DomainObjectContainer<T> extends DomainObjectElementObserver<T>, DomainObjectElementConfigurer<T> {
	<U extends T> void registerFactory(Class<U> type, DomainObjectFactory<U> factory);
	<U extends T> DomainObjectProvider<U> register(DomainObjectIdentity identity, Class<U> type);
	<U extends T> DomainObjectProvider<U> register(DomainObjectIdentity identity, Class<U> type, Action<? super U> action);
}
