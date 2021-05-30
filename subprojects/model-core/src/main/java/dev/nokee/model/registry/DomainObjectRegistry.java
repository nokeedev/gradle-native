package dev.nokee.model.registry;

import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.NamedDomainObjectProvider;

public interface DomainObjectRegistry {
	<T> NamedDomainObjectProvider<T> register(DomainObjectIdentifier identifier, Class<T> type);
}
