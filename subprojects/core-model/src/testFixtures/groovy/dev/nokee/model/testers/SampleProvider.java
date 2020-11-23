package dev.nokee.model.testers;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectProvider;

public final class SampleProvider<T> {
	private final DomainObjectProvider<T> p;
	private final Class<T> type;
	private final DomainObjectIdentifier id;

	public SampleProvider(DomainObjectProvider<T> p, Class<T> type, DomainObjectIdentifier id) {
		this.p = p;
		this.type = type;
		this.id = id;
	}

	public DomainObjectProvider<T> p() {
		return p;
	}

	public Class<T> type() {
		return type;
	}

	public DomainObjectIdentifier id() {
		return id;
	}
}
