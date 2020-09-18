package dev.nokee.platform.base.internal

import dev.nokee.model.internal.NokeeCollection

class VariantViewImplTest extends AbstractViewImplTest {
	@Override
	protected newSubject(NokeeCollection<?> collection) {
		return new VariantViewImpl<>(collection)
	}

	@Override
	protected Class<?> getViewImplementationType() {
		return VariantViewImpl
	}
}
