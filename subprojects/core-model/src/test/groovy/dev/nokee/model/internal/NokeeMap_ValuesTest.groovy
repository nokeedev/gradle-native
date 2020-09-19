package dev.nokee.model.internal

import org.gradle.api.Action
import spock.lang.Subject

import static dev.nokee.utils.SpecUtils.byType

@Subject(NokeeMapImpl)
class NokeeMap_ValuesTest extends NokeeMap_AbstractCollectionTest {
	@Override
	protected def collectionUnderTest(NokeeMap subject) {
		return subject.values()
	}

	@Override
	protected def byTypeFilter(Class type) {
		return byType(type)
	}

	@Override
	protected configureValue(Action action) {
		return action
	}

	@Override
	protected asValues(Collection c) {
		return c
	}

	@Override
	protected callbackWithValue(Action action) {
		return action
	}
}
