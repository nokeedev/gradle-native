package dev.nokee.model.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.utils.ProviderUtils;
import dev.nokee.utils.SpecUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.internal.Cast;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static dev.nokee.utils.TransformerUtils.configureInPlace;

public class NokeeMapImpl<K extends DomainObjectIdentifier, V> implements NokeeMap<K, V> {
	private final DomainObjectSet<Entry<K, ? extends V>> store;
	private boolean disallowChanges = false;

	public NokeeMapImpl(Class<V> type, ObjectFactory objectFactory) {
		this.store = newStore(objectFactory);
	}

	@SuppressWarnings("unchecked")
	private <T> T newStore(ObjectFactory objectFactory) {
		return (T) objectFactory.domainObjectSet(Entry.class);
	}


	@Override
	public void put(K key, Value<V> value) {
		if (disallowChanges) {
			throw new IllegalStateException("The value cannot be changed any further.");
		}

		store.add(new EntryImpl<>(key, value));
	}

	@Override
	public Value<? extends V> get(K k) {
		return store.stream().filter(it -> it.getKey().equals(k)).findFirst().get().getValue();
	}

	@Override
	public void forEach(BiConsumer<? super K, ? super Value<? extends V>> action) {
		store.all(entry -> action.accept(entry.getKey(), entry.getValue()));
	}

	@Override
	public int size() {
		return store.size();
	}

	@Override
	public NokeeCollection<V> values() {
		return new ValuesCollection<>(store, () -> disallowChanges);
	}

	@Override
	public NokeeMap<K, V> disallowChanges() {
		this.disallowChanges = true;
		return this;
	}

	private static final class EntryImpl<K extends DomainObjectIdentifier, V> implements Entry<K, V> {
		private final K key;
		private final Value<V> value;

		public EntryImpl(K key, Value<V> value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public Value<V> getValue() {
			return value;
		}
	}

	private final static class ValuesCollection<K extends DomainObjectIdentifier, U> implements NokeeCollection<U> {
		private final DomainObjectSet<Entry<K, ? extends U>> store;
		private final Supplier<Boolean> disallowChangesSupplier;

		public ValuesCollection(DomainObjectSet<Entry<K, ? extends U>> store, Supplier<Boolean> disallowChangesSupplier) {
			this.store = store;
			this.disallowChangesSupplier = disallowChangesSupplier;
		}

		@Override
		public void add(Value<? extends U> value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			return store.size();
		}

		@Override
		public void forEach(Action<? super U> action) {
			store.all(entry -> entry.getValue().mapInPlace(configureInPlace(action)));
		}

		@Override
		public Collection<? extends U> get() {
			if (!disallowChangesSupplier.get()) {
				throw new IllegalStateException("Please disallow changes before realizing this collection.");
			}
			return store.stream().map(Entry::getValue).map(Value::get).collect(ImmutableList.toImmutableList());
		}

		@Override
		public Provider<Collection<? extends U>> getElements() {
			return ProviderUtils.supplied(this::get);
		}

		@Override
		public <S extends U> NokeeCollection<S> filter(Spec<? super S> spec) {
			return SpecUtils.getTypeFiltered(spec).map(type -> {
				val filteredStore = store.matching(it -> type.isAssignableFrom(it.getValue().getType()));
				DomainObjectSet<Entry<K, ? extends S>> castedFilteredStore = Cast.uncheckedCast(filteredStore);
				return new ValuesCollection<>(castedFilteredStore, disallowChangesSupplier);
			}).orElseThrow(UnsupportedOperationException::new);
		}
	}
}
