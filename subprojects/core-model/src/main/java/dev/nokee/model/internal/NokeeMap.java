package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;

import java.util.function.BiConsumer;

/**
 * Represents a lazy and live map of object identified by the specified key.
 * The Nokee type are the lowest logical level data container.
 * Below would be a persistent layer and above would be object repositories.
 *
 * @param <K> the identifier type associate to each object in this map.
 * @param <V> the value type of each object in this map.
 */
public interface NokeeMap<K extends DomainObjectIdentifier, V> {
	/**
	 * Puts a lazy value associate to the specified key in this map.
	 *
	 * @param key an object identifier
	 * @param value a lazy value
	 */
	void put(K key, Value<V> value);

	/**
	 * Returns the lazy value associated to the specified key in this map.
	 *
	 * @param key an object identifier
	 * @return the lazy value for the specified key.
	 */
	Value<? extends V> get(K key);

	/**
	 * Returns the number of entry in this map.
	 *
	 * @return the number of entry
	 */
	int size();

	/**
	 * Returns a lazy and live collection of all the values in this map.
	 *
	 * @return a lazy & live collection of values
	 */
	NokeeCollection<V> values();

	/**
	 * Register an action to execute for each entry (past and future) of this map.
	 *
	 * @param action The action to be performed for each entry
	 * @throws NullPointerException if the specified action is null
	 */
	void forEach(BiConsumer<? super K, ? super Value<? extends V>> action);

	/**
	 * Disallow further changes to this map.
	 *
	 * @return this map.
	 */
	NokeeMap<K, V> disallowChanges();

	/**
	 * Represent a single entry in the map.
	 *
	 * @param <K> the identifier type used by this entry.
	 * @param <V> the value type represented by this entry.
	 */
	interface Entry<K extends DomainObjectIdentifier, V> {
		K getKey();
		Value<V> getValue();
	}
}
