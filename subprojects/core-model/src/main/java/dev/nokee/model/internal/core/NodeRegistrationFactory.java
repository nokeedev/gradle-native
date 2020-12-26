package dev.nokee.model.internal.core;

public interface NodeRegistrationFactory<T> {
	NodeRegistration<T> create(String name);
}
