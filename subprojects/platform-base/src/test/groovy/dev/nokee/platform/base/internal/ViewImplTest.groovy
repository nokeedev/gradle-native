package dev.nokee.platform.base.internal

import dev.nokee.model.internal.NokeeCollection
import spock.lang.Subject

@Subject(ViewImpl)
class ViewImplTest extends AbstractViewImplTest {
	@Override
	protected newSubject(NokeeCollection<?> collection) {
		return new ViewImpl<>(collection)
	}

	@Override
	protected Class<?> getViewImplementationType() {
		return ViewImpl
	}
}
