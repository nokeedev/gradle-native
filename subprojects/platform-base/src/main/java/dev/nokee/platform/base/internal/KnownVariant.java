package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.KnownDomainObject;
import dev.nokee.platform.base.Variant;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

public class KnownVariant<T extends Variant> {
	private final KnownDomainObject<T> knownObject;

	@Inject
	public KnownVariant(KnownDomainObject<T> knownObject) {
		this.knownObject = knownObject;
	}

	public BuildVariant getBuildVariant() {
		return ((BuildVariantDomainObjectIdentity)knownObject.getIdentity()).getBuildVariant();
	}

	public void configure(Action<? super T> action) {
		knownObject.configure(action);
	}

	public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
		return knownObject.map(transformer);
	}

	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
		return knownObject.flatMap(transformer);
	}
}
