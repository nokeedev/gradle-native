package dev.nokee.platform.base.internal

import dev.nokee.model.internal.NokeeCollection
import dev.nokee.utils.SpecUtils
import org.gradle.api.Action
import spock.lang.Subject

@Subject(ViewImpl)
class ViewImplTest extends AbstractViewImplTest {
	@Override
	protected newSubject(NokeeCollection collection) {
		return new ViewImpl<>(collection)
	}

	@Override
	protected Class<?> getViewImplementationType() {
		return ViewImpl
	}

	@Override
	protected def valueOf(String value) {
		return value
	}

	@Override
	protected values(Action action) {
		return action
	}

	@Override
	protected byType(Class type) {
		return SpecUtils.byType(type)
	}
}
