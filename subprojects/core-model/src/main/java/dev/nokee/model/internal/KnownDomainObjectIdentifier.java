package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.DomainObjectIdentifier;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Namer;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class KnownDomainObjectIdentifier<I extends DomainObjectIdentifier> {
	private final Set<String> knownObjectNames = new HashSet<>();
	private final Function<I, String> objectNameMapper;

	// TODO: Use a NamingScheme?
	public KnownDomainObjectIdentifier(Function<I, String> objectNameMapper) {
		this.objectNameMapper = objectNameMapper;
	}

	public <T> T withKnownIdentifier(I identifier, DomainObjectFactory<T> factory) {
		assert identifier != null;
		assert factory != null;
		val identifierAdded = knownObjectNames.add(objectNameMapper.apply(identifier));
		assert identifierAdded : "while adding " + identifier;
		return factory.create(identifier);
	}

	public <T> Action<? super T> onlyKnownIdentifier(Action<? super T> delegate) {
		return new Action<T>() {
			@Override
			public void execute(T obj) {
				if (knownObjectNames.contains(determineName(obj))) {
					delegate.execute(obj);
				}
			}
		};
	}

	private Namer<?> namer = null;
	private <T> String determineName(T obj) {
		if (namer == null) {
			if (obj instanceof Configuration) {
				namer = new Configuration.Namer();
			} else if (obj instanceof Task) {
				namer = new Task.Namer();
			} else {
				throw new IllegalArgumentException("Unknown name.");
			}
		}

		@SuppressWarnings("unchecked")
		Namer<T> namer = (Namer<T>)this.namer;
		return namer.determineName(obj);
	}
}
