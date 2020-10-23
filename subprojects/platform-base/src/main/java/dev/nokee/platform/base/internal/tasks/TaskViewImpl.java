package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectView;
import dev.nokee.model.internal.AbstractDomainObjectView;
import dev.nokee.platform.base.TaskView;
import groovy.lang.Closure;
import org.gradle.api.Buildable;
import org.gradle.api.Task;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskDependency;

public final class TaskViewImpl<T extends Task> extends AbstractDomainObjectView<Task, T> implements TaskView<T>, DomainObjectView<T>, Buildable {

	TaskViewImpl(DomainObjectIdentifier viewOwner, Class<T> viewElementType, TaskRepository repository, TaskConfigurer configurer, TaskViewFactory viewFactory) {
		super(viewOwner, viewElementType, repository, configurer, viewFactory);
	}

	@Override
	public void configureEach(Closure<Void> closure) {
		DomainObjectView.super.configureEach(closure);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Closure<Void> closure) {
		DomainObjectView.super.configureEach(type, closure);
	}

	@Override
	public void configureEach(Spec<? super T> spec, Closure<Void> closure) {
		DomainObjectView.super.configureEach(spec, closure);
	}

	@Override
	public <S extends T> TaskViewImpl<S> withType(Class<S> type) {
		return (TaskViewImpl<S>) super.withType(type);
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return task -> get();
	}
}
