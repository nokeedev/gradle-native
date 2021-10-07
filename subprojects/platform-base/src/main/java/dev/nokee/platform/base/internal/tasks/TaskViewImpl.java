/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	public void configureEach(@SuppressWarnings("rawtypes") Closure closure) {
		DomainObjectView.super.configureEach(closure);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
		DomainObjectView.super.configureEach(type, closure);
	}

	@Override
	public void configureEach(Spec<? super T> spec, @SuppressWarnings("rawtypes") Closure closure) {
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
