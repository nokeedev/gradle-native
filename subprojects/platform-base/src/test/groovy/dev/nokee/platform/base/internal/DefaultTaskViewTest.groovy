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
	void realizeBackingCollection() {
		project.tasks.withType(TestTask).iterator().next()
	}

	@Override
	def createView() {
		return objects.newInstance(DefaultTaskView, TestTask.class, backingCollection, realizeTrigger)
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
	Provider<Task> getC() {
		return project.tasks.register('c', TestChildTask, 'c')
	}

	@Override
	Class<TestTask> getType() {
		return TestTask
	}

	@Override
	Class<TestChildTask> getOtherType() {
		return TestChildTask
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

	static class TestChildTask extends TestTask {
		@Inject
		TestChildTask(String identification) {
			super(identification)
		}
	}
}
