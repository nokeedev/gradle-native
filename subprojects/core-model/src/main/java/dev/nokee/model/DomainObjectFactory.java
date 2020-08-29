package dev.nokee.model;

public interface DomainObjectFactory<T> {
    /**
     * Creates a new object with the given identifier.
     *
     * @param identifier the global identifier of the object
     * @return The object.
     */
    T create(DomainObjectIdentifier identifier);
}
