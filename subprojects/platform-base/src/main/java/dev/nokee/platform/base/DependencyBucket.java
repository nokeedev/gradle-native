package dev.nokee.platform.base;

/**
 * Represent a bucket of dependencies.
 * These dependencies can either be declarable, consumable or resolvable.
 *
 * @since 0.5
 */
public interface DependencyBucket {
	/**
	 * Returns the name of the dependency bucket.
	 *
	 * @return an instance of {@link DependencyBucketName}, never null.
	 */
	DependencyBucketName getName();

	/**
	 * Extends the current bucket from the specified buckets.
	 * The dependencies declared on the specified buckets will be included in this bucket.
	 *
	 * @param buckets the dependency buckets to extends from
	 */
	void extendsFrom(DependencyBucket... buckets);
}
