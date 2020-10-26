package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.*;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskCollection;

import java.util.function.Predicate;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.isDescendent;

public final class TaskConfigurer implements DomainObjectConfigurer<Task> {
	private final KnownDomainObjects<Task> knownDomainObjects;
	private final TaskCollection<Task> tasks;
	private final KnownDomainObjectActions<Task> knownConfigureActions = new KnownDomainObjectActions<>();
	private final NamedDomainObjectConfigurer<Task> nameAwareConfigurer;

	public TaskConfigurer(DomainObjectEventPublisher eventPublisher, TaskCollection<Task> tasks) {
		this.knownDomainObjects = new KnownDomainObjects<>(Task.class, eventPublisher, knownConfigureActions);
		this.tasks = tasks;
		this.nameAwareConfigurer = new NamedDomainObjectConfigurer<>(Task.class, knownDomainObjects, this);
	}

	@Override
	public <S extends Task> void configureEach(DomainObjectIdentifier owner, Class<S> type, Action<? super S> action) {
		tasks.withType(type).configureEach(task -> {
			knownDomainObjects.find(identifierForTask(task.getName())).ifPresent(identifier -> {
				if (isDescendent(identifier, owner)) {
					action.execute(type.cast(task));
				}
			});
		});
	}

	@Override
	public <S extends Task> void configure(DomainObjectIdentifier owner, String name, Class<S> type, Action<? super S> action) {
		nameAwareConfigurer.configure(owner, name, type, action);
	}

	private static <S extends Task> Predicate<? super TypeAwareDomainObjectIdentifier<? extends S>> identifierForTask(String taskName) {
		return identifier -> {
			return ((TaskIdentifier<S>)identifier).getTaskName().equals(taskName);
		};
	}

	@Override
	public <S extends Task> void configure(TypeAwareDomainObjectIdentifier<S> identifier, Action<? super S> action) {
		knownDomainObjects.assertKnownObject(identifier);
		tasks.named(((TaskIdentifier<S>)identifier).getTaskName(), identifier.getType(), action);
	}

	@Override
	public <S extends Task> void whenElementKnown(DomainObjectIdentifier owner, Class<S> type, Action<? super TypeAwareDomainObjectIdentifier<S>> action) {
		val knownAction = KnownDomainObjectActions.onlyIf(owner, type, action);
		knownDomainObjects.forEach(knownAction);
		knownConfigureActions.add(knownAction);
	}
}
