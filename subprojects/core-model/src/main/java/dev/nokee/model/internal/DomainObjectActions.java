package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.isDescendent;

public final class DomainObjectActions<T> implements Consumer<T> {
	private final List<Consumer<? super T>> configureActions = new ArrayList<>();

	@Override
	public void accept(T t) {
		configureActions.forEach(action -> action.accept(t));
	}

	public void add(Consumer<? super T> action) {
		configureActions.add(action);
	}

	public static <T, S extends T> Consumer<T> onlyIf(Class<S> type, Consumer<? super S> action) {
		return new Consumer<T>() {
			@Override
			public void accept(T object) {
				if (type.isInstance(object)) {
					action.accept(type.cast(object));
				}
			}
		};
	}

	public static <T> BiConsumer<? super DomainObjectIdentifier, ? super T> onlyIf(DomainObjectIdentifier owner, Action<? super T> action) {
		return new BiConsumer<DomainObjectIdentifier, T>() {
			@Override
			public void accept(DomainObjectIdentifier identifier, T object) {
				if (isDescendent(identifier, owner)) {
					action.execute(object);
				}
			}
		};
	}
}
