package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.Variant;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

public abstract class KnownVariant<T extends Variant> {
	@Getter private final BuildVariant buildVariant;

	@Inject
	public KnownVariant(BuildVariant buildVariant) {
		this.buildVariant = buildVariant;
	}

	public void configure(Action<? super T> action) {

	}

	public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
		return null;
	}

	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
		return null;
	}
}
