package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import groovy.lang.Closure;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.model.ObjectFactory;
import org.gradle.internal.metaobject.*;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// DO NOT EXTEND THIS CLASS, extends BaseComponentDependencies
public class DefaultComponentDependencies implements ComponentDependenciesInternal, MethodMixIn, PropertyMixIn {
	private final Map<String, DependencyBucket> bucketIndex = new HashMap<>();
	private final ContainerElementsDynamicObject elementsDynamicObject = new ContainerElementsDynamicObject();
	@Getter private final String componentDisplayName;
	private final DependencyBucketFactory factory;
	@Getter(AccessLevel.PROTECTED) private final DomainObjectSet<DependencyBucket> buckets;

	@Inject
	@Deprecated // use ObjectFactory
	public DefaultComponentDependencies(String componentDisplayName, DependencyBucketFactory factory, ObjectFactory objects) {
		this.componentDisplayName = componentDisplayName;
		this.factory = factory;
		this.buckets = objects.domainObjectSet(DependencyBucket.class);
	}

	@Override
	public DependencyBucket create(String name) {
		val bucket = factory.create(name);
		bucketIndex.put(name, bucket);
		getBuckets().add(bucket);
		return bucket;
	}

	@Override
	public DependencyBucket create(String name, Action<Configuration> action) {
		val bucket = factory.create(name);
		action.execute(bucket.getAsConfiguration());
		bucketIndex.put(name, bucket);
		getBuckets().add(bucket);
		return bucket;
	}

	@Override
	public DependencyBucket getByName(String name) {
		return bucketIndex.computeIfAbsent(name, it -> {
			throw new UnknownDomainObjectException(String.format("%s with name '%s' not found.", getTypeDisplayName(), name));
		});
	}

	private String getTypeDisplayName() {
		return DependencyBucket.class.getSimpleName();
	}

	@Override
	public void add(String bucketName, Object notation) {
		assertBucketExists(bucketName);
		bucketIndex.get(bucketName).addDependency(notation);
	}

	@Override
	public void add(String bucketName, Object notation, Action<? super ModuleDependency> action) {
		assertBucketExists(bucketName);
		bucketIndex.get(bucketName).addDependency(notation, action);
	}

	@Override
	public void configureEach(Action<? super DependencyBucket> action) {
		getBuckets().configureEach(action);
	}

	@Override
	public Optional<DependencyBucket> findByName(String name) {
		return Optional.ofNullable(bucketIndex.get(name));
	}

	private void assertBucketExists(String bucketName) {
		if (!bucketIndex.containsKey(bucketName)) {
			throw new IllegalArgumentException(String.format("Dependency bucket named '%s' couldn't be found.", bucketName));
		}
	}

	@Override
	public MethodAccess getAdditionalMethods() {
		return elementsDynamicObject;
	}

	@Override
	public PropertyAccess getAdditionalProperties() {
		return elementsDynamicObject;
	}

	private class ContainerElementsDynamicObject extends AbstractDynamicObject {
		@Override
		public String getDisplayName() {
			return "";
		}

		@Override
		public DynamicInvokeResult tryGetProperty(String name) {
			val bucket = bucketIndex.get(name);
			if (bucket == null) {
				return DynamicInvokeResult.notFound();
			}
			return DynamicInvokeResult.found(bucket);
		}

		@Override
		public Map<String, DependencyBucket> getProperties() {
			return Collections.unmodifiableMap(bucketIndex);
		}

		@Override
		public boolean hasProperty(String name) {
			return bucketIndex.containsKey(name);
		}

		@Override
		public boolean hasMethod(String name, Object... arguments) {
			return isConfigureMethod(name, arguments);
		}

		@Override
		public DynamicInvokeResult tryInvokeMethod(String name, Object... arguments) {
			if (isConfigureMethod(name, arguments)) {
				if (arguments.length == 1) {
					add(name, arguments[0]);
					return DynamicInvokeResult.found();
				} else if (arguments.length == 2) {
					add(name, arguments[0], ConfigureUtil.configureUsing((Closure) arguments[1]));
					return DynamicInvokeResult.found();
				}
			}
			return DynamicInvokeResult.notFound();
		}

		private boolean isConfigureMethod(String name, Object... arguments) {
			if (name.equals("project")) { // Let project(...) methods be handled by parent objects
				return false;
			}
			return arguments.length == 1 || (arguments.length == 2 && arguments[1] instanceof Closure);
		}
	}
}
