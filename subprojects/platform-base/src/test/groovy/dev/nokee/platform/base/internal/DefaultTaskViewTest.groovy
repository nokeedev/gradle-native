package dev.nokee.platform.base.internal

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import spock.lang.Subject

import javax.inject.Inject

@Subject(DefaultTaskView)
class DefaultTaskViewTest extends AbstractViewTest<Task> {
	def backingCollection = []

	@Override
	def getBackingCollection() {
		return project.tasks.withType(TestTask)
	}

	@Override
	def createView() {
		return objects.newInstance(viewType, backingCollection, realizeTrigger)
	}

	@Override
	Class getViewType() {
		return DefaultTaskView
	}

	@Override
	Provider<Task> getA() {
		return project.tasks.register('a', TestTask, 'a')
	}

	@Override
	Provider<Task> getB() {
		return project.tasks.register('b', TestTask, 'b')
	}

	@Override
	void addToBackingCollection(Provider<Task> v) {
		backingCollection.add(v)
	}

	static class TestTask extends DefaultTask implements AbstractViewTest.Identifiable {
		private final String identification

		@Inject
		TestTask(String identification) {
			this.identification = identification
		}

		@Override
		String getIdentification() {
			return identification
		}
	}
}
