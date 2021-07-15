package dev.nokee.model;

import java.util.Iterator;

public interface DomainObjectIdentifier extends Iterable<Object> {
	@Override
	default Iterator<Object> iterator() {
		throw new UnsupportedOperationException();
	}
}
