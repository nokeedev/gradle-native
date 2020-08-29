package dev.nokee.platform.base;

import com.google.common.base.Preconditions;
import lombok.*;

/**
 * Represents the name of a dependency bucket.
 * It's the raw name of the bucket without any of the namespacing mixed-in to distinguish between variant.
 * For example, configurations of the following names - {@literal testImplementation} and {@literal integTestImplementation} - would have a dependency bucket name of {@literal implementation}.
 *
 * @since 0.5
 */
@Value(staticConstructor = "of")
public class DependencyBucketName {
	@Getter(AccessLevel.NONE) String name;

	private DependencyBucketName(String name) {
		Preconditions.checkArgument(name != null, "Cannot construct a dependency bucket name because the name is null.");
		Preconditions.checkArgument(!name.isEmpty(), "Cannot construct a dependency bucket name because the name is empty.");
		this.name = name;
	}

	public String get() {
		return name;
	}
}
