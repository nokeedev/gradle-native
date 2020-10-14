package dev.nokee.platform.base.internal.binaries;

import dev.nokee.model.internal.AbstractKnownDomainObject;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import lombok.ToString;
import org.gradle.api.provider.Provider;

@ToString
public final class KnownBinary<T extends Binary> extends AbstractKnownDomainObject<Binary, T> {
	KnownBinary(BinaryIdentifier<T> identifier, Provider<T> provider, BinaryConfigurer configurer) {
		super(identifier, provider, configurer);
	}
}
