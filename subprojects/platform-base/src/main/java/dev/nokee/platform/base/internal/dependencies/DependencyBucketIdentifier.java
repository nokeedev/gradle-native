package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.ProjectIdentifier;
import dev.nokee.platform.base.internal.VariantIdentifier;
import lombok.Value;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.util.GUtil;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@Value
public class DependencyBucketIdentifier<T extends DependencyBucket> implements DomainObjectIdentifierInternal {
	DependencyBucketName name;
	Class<T> type;
	DomainObjectIdentifierInternal ownerIdentifier;

	private DependencyBucketIdentifier(DependencyBucketName name, Class<T> type, DomainObjectIdentifierInternal ownerIdentifier) {
		checkArgument(name != null, "Cannot construct a dependency identifier because the bucket name is null.");
		checkArgument(type != null, "Cannot construct a dependency identifier because the bucket type is null.");
		checkArgument(ownerIdentifier != null, "Cannot construct a dependency identifier because the owner identifier is null.");
		checkArgument(isValidOwner(ownerIdentifier), "Cannot construct a dependency identifier because the owner identifier is invalid, only ProjectIdentifier, ComponentIdentifier and VariantIdentifier are accepted.");
		this.name = name;
		this.type = type;
		this.ownerIdentifier = ownerIdentifier;
	}

	private static boolean isValidOwner(DomainObjectIdentifierInternal ownerIdentifier) {
		return ownerIdentifier instanceof ProjectIdentifier || ownerIdentifier instanceof ComponentIdentifier || ownerIdentifier instanceof VariantIdentifier;
	}

	@Override
	public Optional<? extends DomainObjectIdentifierInternal> getParentIdentifier() {
		return Optional.of(ownerIdentifier);
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
	public String getDisplayName() {
		val builder = new StringBuilder();
		builder.append(StringUtils.capitalize(GUtil.toWords(name.get()).replace("api", "API")));
		if (!ConsumableDependencyBucket.class.isAssignableFrom(type) && !ResolvableDependencyBucket.class.isAssignableFrom(type)) {
			builder.append(" dependencies");
		}
		builder.append(" for ").append(ownerIdentifier.getDisplayName()).append(".");
		return builder.toString();
	}

	public String getConfigurationName() {
		val segments = new ArrayList<String>();

		getComponentOwnerIdentifier()
			.filter(it -> !it.isMainComponent())
			.map(ComponentIdentifier::getName)
			.map(ComponentName::get)
			.ifPresent(segments::add);
		getVariantOwnerIdentifier()
			.map(VariantIdentifier::getUnambiguousName)
			.filter(it -> !it.isEmpty())
			.ifPresent(segments::add);
		segments.add(name.get());

		return StringUtils.uncapitalize(segments.stream().map(StringUtils::capitalize).collect(Collectors.joining()));
	}

	public static <T extends DependencyBucket> DependencyBucketIdentifier<T> of(DependencyBucketName name, Class<T> type, DomainObjectIdentifierInternal ownerIdentifier) {
		return new DependencyBucketIdentifier<>(name, type, ownerIdentifier);
	}
}
