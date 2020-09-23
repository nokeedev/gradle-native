package dev.nokee.platform.base.internal.tasks;

import com.google.common.base.Preconditions;
import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.ProjectIdentifier;
import dev.nokee.platform.base.internal.VariantIdentifier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Task;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@ToString
@EqualsAndHashCode
public final class TaskIdentifier<T extends Task> implements DomainObjectIdentifierInternal {
	@Getter private final TaskName name;
	@Getter private final Class<T> type;
	@Getter private final DomainObjectIdentifierInternal ownerIdentifier;

	public TaskIdentifier(TaskName name, Class<T> type, DomainObjectIdentifierInternal ownerIdentifier) {
		Preconditions.checkArgument(name != null, "Cannot construct a task identifier because the task name is null.");
		Preconditions.checkArgument(type != null, "Cannot construct a task identifier because the task type is null.");
		Preconditions.checkArgument(ownerIdentifier != null, "Cannot construct a task identifier because the owner identifier is null.");
		Preconditions.checkArgument(isValidOwner(ownerIdentifier), "Cannot construct a task identifier because the owner identifier is invalid, only ProjectIdentifier, ComponentIdentifier and VariantIdentifier are accepted.");
		this.name = name;
		this.type = type;
		this.ownerIdentifier = ownerIdentifier;
	}

	private static boolean isValidOwner(DomainObjectIdentifierInternal ownerIdentifier) {
		return ownerIdentifier instanceof ProjectIdentifier || ownerIdentifier instanceof ComponentIdentifier || ownerIdentifier instanceof VariantIdentifier;
	}

	public static <T extends Task> TaskIdentifier<T> of(TaskName name, Class<T> type, DomainObjectIdentifierInternal ownerIdentifier) {
		return new TaskIdentifier<>(name, type, ownerIdentifier);
	}

	public static TaskIdentifier<Task> of(TaskName name, DomainObjectIdentifierInternal ownerIdentifier) {
		return new TaskIdentifier<>(name, Task.class, ownerIdentifier);
	}

	public static TaskIdentifier<Task> ofLifecycle(DomainObjectIdentifierInternal ownerIdentifier) {
		Preconditions.checkArgument(isValidLifecycleOwner(ownerIdentifier), "Cannot construct a lifecycle task identifier for specified owner as it will result into an invalid task name.");
		return new TaskIdentifier<>(TaskName.empty(), Task.class, ownerIdentifier);
	}

	private static boolean isValidLifecycleOwner(DomainObjectIdentifierInternal ownerIdentifier) {
		if (ownerIdentifier instanceof VariantIdentifier) {
			val variantIdentifier = (VariantIdentifier<?>) ownerIdentifier;
			if (variantIdentifier.getUnambiguousName().isEmpty() && variantIdentifier.getComponentIdentifier().isMainComponent()) {
				return false;
			}
		} else if (ownerIdentifier instanceof ComponentIdentifier && ((ComponentIdentifier<?>) ownerIdentifier).isMainComponent()) {
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

	private Optional<ComponentIdentifier<?>> getComponentOwnerIdentifier() {
		if (ownerIdentifier instanceof VariantIdentifier) {
			return Optional.of(((VariantIdentifier<?>) ownerIdentifier).getComponentIdentifier());
		} else if (ownerIdentifier instanceof ComponentIdentifier) {
			return Optional.of((ComponentIdentifier<?>) ownerIdentifier);
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
	public Optional<? extends DomainObjectIdentifierInternal> getParentIdentifier() {
		return Optional.of(ownerIdentifier);
	}

	@Override
	public String getDisplayName() {
		throw new UnsupportedOperationException(); // for now...
	}
}
