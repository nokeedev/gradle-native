package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.isDescendent;

public final class KnownDomainObjectActions<T> implements Consumer<TypeAwareDomainObjectIdentifier<? extends T>> {
	private final List<Consumer<? super TypeAwareDomainObjectIdentifier<? extends T>>> knownConfigureActions = new ArrayList<>();

	@Override
	public void accept(TypeAwareDomainObjectIdentifier<? extends T> knownDomainObjectIdentifier) {
		knownConfigureActions.forEach(action -> action.accept(knownDomainObjectIdentifier));
	}

	public void add(Consumer<? super TypeAwareDomainObjectIdentifier<? extends T>> action) {
		knownConfigureActions.add(action);
	}

	public static <T> Consumer<? super TypeAwareDomainObjectIdentifier<?>> onlyIf(DomainObjectIdentifier owner, Class<T> type, Action<? super TypeAwareDomainObjectIdentifier<T>> action) {
		return new Consumer<TypeAwareDomainObjectIdentifier<?>>() {
			@Override
			public void accept(TypeAwareDomainObjectIdentifier<?> knownDomainObjectIdentifier) {
				if (isDescendent(knownDomainObjectIdentifier, owner) && type.isAssignableFrom(knownDomainObjectIdentifier.getType())) {
					action.execute((TypeAwareDomainObjectIdentifier<T>)knownDomainObjectIdentifier);
				}
			}
		};
	}
}
