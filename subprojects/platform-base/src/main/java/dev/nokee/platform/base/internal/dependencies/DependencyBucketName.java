package dev.nokee.platform.base.internal.dependencies;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents the name of a dependency bucket.
 * It's the raw name of the bucket without any of the namespacing mixed-in to distinguish between variant.
 * For example, configurations of the following names - {@literal testImplementation} and {@literal integTestImplementation} - would have a dependency bucket name of {@literal implementation}.
 *
 * @since 0.5
 */
@ToString
@EqualsAndHashCode
public final class DependencyBucketName {
	private final String name;

	private DependencyBucketName(String name) {
		checkArgument(name != null, "Cannot construct a dependency bucket name because the name is null.");
		checkArgument(!name.isEmpty(), "Cannot construct a dependency bucket name because the name is empty.");
		checkArgument(!Character.isUpperCase(name.charAt(0)), "Cannot construct a dependency bucket name because the name is capitalized.");
		this.name = name;
	}

	public static DependencyBucketName of(String name) {
		return new DependencyBucketName(name);
	}

	public String get() {
		return name;
	}
}
