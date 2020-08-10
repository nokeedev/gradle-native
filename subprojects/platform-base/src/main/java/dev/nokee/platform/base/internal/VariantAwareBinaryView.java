package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.View;
import dev.nokee.utils.Cast;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class VariantAwareBinaryView<T extends Binary> extends BaseView<T> implements BinaryView<T> {
	@Inject
	public VariantAwareBinaryView(View<T> delegate) {
		super(delegate);
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public <S extends T> BinaryView<S> withType(Class<S> type) {
		return Cast.uncheckedCastBecauseOfTypeErasure(getObjects().newInstance(VariantAwareBinaryView.class, super.withType(type)));
	}
}
