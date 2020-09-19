package dev.nokee.model.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.utils.ProviderUtils;
import dev.nokee.utils.SpecUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static dev.nokee.utils.TransformerUtils.configureInPlace;

public class NokeeMapImpl<K extends DomainObjectIdentifier, V> implements NokeeMap<K, V> {
	private final DomainObjectSet<Entry<K, V>> store;
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

	public NokeeSet<Entry<K, V>> entrySet() {
		return new EntrySet<>(store, () -> disallowChanges);
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

	private static abstract class AbstractCollection<K extends DomainObjectIdentifier, U, T> implements NokeeCollection<T> {
		protected final DomainObjectSet<? extends Entry<K, U>> store;
		protected final Supplier<Boolean> disallowChangesSupplier;

		public AbstractCollection(DomainObjectSet<? extends Entry<K, U>> store, Supplier<Boolean> disallowChangesSupplier) {
			this.store = store;
			this.disallowChangesSupplier = disallowChangesSupplier;
		}

		@Override
		public void add(Value<? extends T> value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			return store.size();
		}

		protected void assertDisallowedChanges() {
			if (!disallowChangesSupplier.get()) {
				throw new IllegalStateException("Please disallow changes before realizing this collection.");
			}
		}
	}

	private final static class EntrySet<K extends DomainObjectIdentifier, U> extends AbstractCollection<K, U, Entry<K, U>> implements NokeeSet<Entry<K, U>> {

		public EntrySet(DomainObjectSet<Entry<K, U>> store, Supplier<Boolean> disallowChangesSupplier) {
			super(store, disallowChangesSupplier);
		}

		@Override
		public void forEach(Action<? super Entry<K, U>> action) {
			store.all(action);
		}

		@Override
		public Set<Entry<K, U>> get() {
			assertDisallowedChanges();
			return ImmutableSet.copyOf(store);
		}

		@Override
		public Provider<? extends Set<Entry<K, U>>> getElements() {
			return ProviderUtils.supplied(this::get);
		}

		@Override
		@SuppressWarnings("unchecked")
		public NokeeSet<Entry<K, U>> filter(Spec<? super Entry<K, U>> spec) {
			return new EntrySet<>((DomainObjectSet<Entry<K, U>>) store.matching(spec), disallowChangesSupplier);
		}

		@Override
		public void whenElementAdded(Action<? super Value<Entry<K, U>>> action) {
			store.all(entry -> action.execute(Value.fixed(entry)));
		}
	}

	private final static class ValuesCollection<K extends DomainObjectIdentifier, U> extends AbstractCollection<K, U, U> {
		public ValuesCollection(DomainObjectSet<? extends Entry<K, U>> store, Supplier<Boolean> disallowChangesSupplier) {
			super(store, disallowChangesSupplier);
		}

		@Override
		public void forEach(Action<? super U> action) {
			store.all(entry -> entry.getValue().mapInPlace(configureInPlace(action)));
		}

		@Override
		public Collection<U> get() {
			assertDisallowedChanges();
			return store.stream().map(Entry::getValue).map(Value::get).collect(ImmutableList.toImmutableList());
		}

		@Override
		public Provider<? extends Collection<U>> getElements() {
			return ProviderUtils.supplied(this::get);
		}

		@Override
		public NokeeCollection<U> filter(Spec<? super U> spec) {
			return SpecUtils.getTypeFiltered(spec).map(type -> {
				val filteredStore = store.matching(it -> type.isAssignableFrom(it.getValue().getType()));
				return new ValuesCollection<>(filteredStore, disallowChangesSupplier);
			}).orElseThrow(UnsupportedOperationException::new);
		}

		@Override
		public void whenElementAdded(Action<? super Value<U>> action) {
			store.all(entry -> action.execute(entry.getValue()));
		}
	}
}
