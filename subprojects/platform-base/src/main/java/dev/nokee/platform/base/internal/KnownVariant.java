package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.Value;
import dev.nokee.platform.base.Variant;
import dev.nokee.utils.TransformerUtils;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

public final class KnownVariant<T extends Variant> {
	@Getter private final VariantIdentifier<T> identifier;
	private final Value<? extends T> value;

	public KnownVariant(VariantIdentifier<T> identifier, Value<? extends T> value) {
		this.identifier = identifier;
		this.value = value;
	}

	public BuildVariantInternal getBuildVariant() {
		return (BuildVariantInternal) identifier.getBuildVariant();
	}

	public void configure(Action<? super T> action) {
		value.mapInPlace(TransformerUtils.configureInPlace(action));
	}

	public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
		return value.map(transformer);
	}

	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
		return value.flatMap(transformer);
	}
}
