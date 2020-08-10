package dev.nokee.runtime.base.internal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Named;
import org.gradle.api.model.ObjectFactory;

import java.io.Serializable;
import java.util.Objects;

/**
 * Base implementation of the Named interface with a stable hash and a generic equals implementation for plain Named types.
 * It's an alternative to using {@link ObjectFactory#named(Class, String)} hence avoiding to weave an ObjectFactory instance around.
 * The implementation is a bit dumb in the sense that will serialize using Java serialization.
 * A better implementation could try to use the Gradle custom serialization.
 */
@RequiredArgsConstructor
public class BaseNamed implements Named, Serializable {
	@Getter private final String name;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Named)) return false;
		Named that = (Named) o;
		return getName().equals(that.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName());
	}

	@Override
	public String toString() {
		return name;
	}
}
