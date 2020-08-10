package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.DomainObjectElementObserver;
import dev.nokee.platform.base.KnownDomainObject;
import dev.nokee.utils.Cast;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;

public final class DefaultDomainObjectElementObserver<T> implements DomainObjectElementObserver<T> {
	private final DomainObjectSet<KnownDomainObject<T>> knownElements;

	public DefaultDomainObjectElementObserver(DomainObjectSet<KnownDomainObject<T>> knownElements) {
		this.knownElements = knownElements;
	}

	@Override
	public void whenElementKnown(Action<KnownDomainObject<? extends T>> action) {
		knownElements.all(action);
	}

	@Override
	public <U> void whenElementKnown(Class<U> type, Action<KnownDomainObject<? extends U>> action) {
		knownElements.all(knownElement -> {
			if (type.isAssignableFrom(knownElement.getType())) {
				action.execute(Cast.uncheckedCastBecauseOfTypeErasure(knownElement));
			}
		});
	}
}
