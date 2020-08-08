package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.base.internal.BaseNamed;
import lombok.Value;

@Value
public class DefaultLibraryElements extends BaseNamed implements LibraryElements {
	public DefaultLibraryElements(String name) {
		super(name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LibraryElements)) return false;
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
