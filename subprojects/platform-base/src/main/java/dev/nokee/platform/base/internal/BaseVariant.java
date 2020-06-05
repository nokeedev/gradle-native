package dev.nokee.platform.base.internal;

import dev.nokee.internal.Cast;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Named;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

// CAUTION: Never rely on the name of the variant, it isn't exposed on the public type!
@RequiredArgsConstructor
public abstract class BaseVariant implements Named {
	@Getter private final DomainObjectSet<Binary> binaryCollection = getObjects().domainObjectSet(Binary.class);
	@Getter private final String name;
	@Getter private final BuildVariant buildVariant;

	@Inject
	protected abstract ObjectFactory getObjects();

	public BinaryView<Binary> getBinaries() {
		return Cast.uncheckedCast("of type erasure", getObjects().newInstance(DefaultBinaryView.class, Binary.class, binaryCollection, Realizable.IDENTITY));
	}

	public abstract Property<Binary> getDevelopmentBinary();
}
