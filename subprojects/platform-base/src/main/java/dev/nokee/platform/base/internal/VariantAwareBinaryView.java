package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.View;
import dev.nokee.utils.Cast;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class VariantAwareBinaryView<T extends Binary> extends BaseView<T> implements BinaryView<T> {
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public VariantAwareBinaryView(View<T> delegate, ObjectFactory objects) {
		super(delegate);
		this.objects = objects;
	}

	@Override
	public <S extends T> BinaryView<S> withType(Class<S> type) {
		return Cast.uncheckedCastBecauseOfTypeErasure(getObjects().newInstance(VariantAwareBinaryView.class, super.withType(type)));
	}
}
