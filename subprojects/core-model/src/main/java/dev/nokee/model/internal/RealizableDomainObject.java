package dev.nokee.model.internal;

/**
 * Represent an lazy object that can be forced into existence.
 * In theory, once an object is realized, it should no longer change.
 */
public interface RealizableDomainObject {
	void realize();
}
