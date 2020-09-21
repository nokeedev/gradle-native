package dev.nokee.platform.base.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;

public interface ComponentBinariesInternal<I extends DomainObjectIdentifier> {
	BinaryView<Binary> getAsView();
	BinaryView<Binary> getAsViewFor(I parentIdentifier);
	void createBinaries(I identifier);
	<T extends Binary> void put(BinaryIdentifier<T> identifier, T binary);
}
