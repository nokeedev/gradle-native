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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.HasName;
import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.VariantIdentifier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toGradlePath;

@EqualsAndHashCode
public final class TaskIdentifier<T extends Task> implements DomainObjectIdentifierInternal, TypeAwareDomainObjectIdentifier<T>, HasName {
	@Getter private final TaskName name;
	@Getter private final Class<T> type;
	@Getter private final DomainObjectIdentifier ownerIdentifier;

	private TaskIdentifier(TaskName name, Class<T> type, DomainObjectIdentifier ownerIdentifier) {
		Preconditions.checkArgument(name != null, "Cannot construct a task identifier because the task name is null.");
		Preconditions.checkArgument(ownerIdentifier != null, "Cannot construct a task identifier because the owner identifier is null.");
		this.name = name;
		this.type = type;
		this.ownerIdentifier = ownerIdentifier;
	}

	public static <T extends Task> TaskIdentifier<T> of(TaskName name, Class<T> type, DomainObjectIdentifier ownerIdentifier) {
		Preconditions.checkArgument(type != null, "Cannot construct a task identifier because the task type is null.");
		return new TaskIdentifier<>(name, type, ownerIdentifier);
	}

	public static <T extends Task> TaskIdentifier<T> of(String name, Class<T> type, DomainObjectIdentifier ownerIdentifier) {
		Preconditions.checkArgument(type != null, "Cannot construct a task identifier because the task type is null.");
		return new TaskIdentifier<>(TaskName.of(name), type, ownerIdentifier);
	}

	public static TaskIdentifier<Task> of(TaskName name, DomainObjectIdentifier ownerIdentifier) {
		return new TaskIdentifier<>(name, Task.class, ownerIdentifier);
	}

	public static TaskIdentifier<Task> ofLifecycle(DomainObjectIdentifier ownerIdentifier) {
		Preconditions.checkArgument(isValidLifecycleOwner(ownerIdentifier), "Cannot construct a lifecycle task identifier for specified owner as it will result into an invalid task name.");
		return new TaskIdentifier<>(TaskName.empty(), Task.class, ownerIdentifier);
	}

	public static TaskIdentifier<?> of(DomainObjectIdentifier ownerIdentifier, String name) {
		return new TaskIdentifier<>(TaskName.of(name), Task.class, ownerIdentifier);
	}

	public static TaskIdentifier<?> of(DomainObjectIdentifier ownerIdentifier, TaskName name) {
		return new TaskIdentifier<>(name, Task.class, ownerIdentifier);
	}

	private static boolean isValidLifecycleOwner(DomainObjectIdentifier ownerIdentifier) {
		if (ownerIdentifier instanceof VariantIdentifier) {
			val variantIdentifier = (VariantIdentifier<?>) ownerIdentifier;
			if (variantIdentifier.getUnambiguousName().isEmpty() && variantIdentifier.getComponentIdentifier().isMainComponent()) {
				return false;
			}
		} else if (ownerIdentifier instanceof ComponentIdentifier && ((ComponentIdentifier) ownerIdentifier).isMainComponent()) {
			return false;
		} else if (ownerIdentifier instanceof ProjectIdentifier) {
			return false;
		}
		return true;
	}

	public String getTaskName() {
		val segments = new ArrayList<String>();

		segments.add(name.getVerb());
		getComponentOwnerIdentifier()
			.filter(it -> !it.isMainComponent())
			.map(ComponentIdentifier::getName)
			.map(ComponentName::get)
			.ifPresent(segments::add);
		getVariantOwnerIdentifier()
			.map(VariantIdentifier::getUnambiguousName)
			.filter(it -> !it.isEmpty())
			.ifPresent(segments::add);
		name.getObject().ifPresent(segments::add);

		return StringUtils.uncapitalize(segments.stream().map(StringUtils::capitalize).collect(Collectors.joining()));
	}

	private Optional<ComponentIdentifier> getComponentOwnerIdentifier() {
		if (ownerIdentifier instanceof VariantIdentifier) {
			return Optional.of(((VariantIdentifier<?>) ownerIdentifier).getComponentIdentifier());
		} else if (ownerIdentifier instanceof ComponentIdentifier) {
			return Optional.of((ComponentIdentifier) ownerIdentifier);
		}
		return Optional.empty();
	}

	private Optional<VariantIdentifier<?>> getVariantOwnerIdentifier() {
		if (ownerIdentifier instanceof VariantIdentifier) {
			return Optional.of((VariantIdentifier<?>) ownerIdentifier);
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		return "task '" + toGradlePath(this) + "' (" + type.getSimpleName() + ")";
	}

	@Override
	public Iterator<Object> iterator() {
		return ImmutableList.builder().addAll(ownerIdentifier).add(this).build().iterator();
	}
}
