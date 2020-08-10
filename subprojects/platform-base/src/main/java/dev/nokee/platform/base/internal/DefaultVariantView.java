package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.View;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class DefaultVariantView<T extends Variant> extends BaseView<T> implements VariantView<T> {
	private final ObjectFactory objects;

	@Inject
	public DefaultVariantView(View<T> delegate, ObjectFactory objects) {
		super(delegate);
		this.objects = objects;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends T> VariantView<S> withType(Class<S> type) {
		return objects.newInstance(DefaultVariantView.class, super.withType(type));
	}
}
