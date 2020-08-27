package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ProjectIdentifier;
import dev.nokee.platform.base.internal.VariantIdentifier;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Task;

interface TaskIdentifierFactory {
	<T extends Task> TaskIdentifier<T> create(TaskName taskName, Class<T> type);

	static TaskIdentifierFactory childOf(ProjectIdentifier parentIdentifier) {
		return new TaskIdentifierFactory() {
			@Override
			public <T extends Task> TaskIdentifier<T> create(TaskName taskName, Class<T> type) {
				return new TaskIdentifier<>(taskName.getVerb() + taskName.getObject().map(StringUtils::capitalize).orElse(""), type, parentIdentifier);
			}
		};
	}

	static TaskIdentifierFactory childOf(ComponentIdentifier parentIdentifier) {
		return new TaskIdentifierFactory() {
			@Override
			public <T extends Task> TaskIdentifier<T> create(TaskName taskName, Class<T> type) {
				if (parentIdentifier.getName().equals("main")) {
					return new TaskIdentifier<>(taskName.getVerb() + taskName.getObject().map(StringUtils::capitalize).orElse(""), type, parentIdentifier);
				}
				return new TaskIdentifier<>(taskName.getVerb() + StringUtils.capitalize(parentIdentifier.getName()) + taskName.getObject().map(StringUtils::capitalize).orElse(""), type, parentIdentifier);
			}
		};
	}

	static TaskIdentifierFactory childOf(VariantIdentifier parentIdentifier) {
		return new TaskIdentifierFactory() {
			@Override
			public <T extends Task> TaskIdentifier<T> create(TaskName taskName, Class<T> type) {
				if (parentIdentifier.getComponentIdentifier().getName().equals("main")) {
					return new TaskIdentifier<>(taskName.getVerb() + StringUtils.capitalize(parentIdentifier.getUnambiguousName()) + taskName.getObject().map(StringUtils::capitalize).orElse(""), type, parentIdentifier);
				}
				return new TaskIdentifier<>(taskName.getVerb() + StringUtils.capitalize(parentIdentifier.getComponentIdentifier().getName()) + StringUtils.capitalize(parentIdentifier.getUnambiguousName()) + taskName.getObject().map(StringUtils::capitalize).orElse(""), type, parentIdentifier);
			}
		};
	}

	static TaskIdentifierFactory childOf(DomainObjectIdentifierInternal parentIdentifier) {
		if (parentIdentifier instanceof ProjectIdentifier) {
			return childOf((ProjectIdentifier) parentIdentifier);
		} else if (parentIdentifier instanceof ComponentIdentifier) {
			return childOf((ComponentIdentifier) parentIdentifier);
		} else if (parentIdentifier instanceof VariantIdentifier) {
			return childOf((VariantIdentifier) parentIdentifier);
		}
		throw new IllegalArgumentException();
	}
}
