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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
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
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.internal.metaobject.*;
import org.gradle.util.ConfigureUtil;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// DO NOT EXTEND THIS CLASS, extends BaseComponentDependencies
public abstract class DefaultComponentDependencies implements ComponentDependenciesInternal, MethodMixIn, PropertyMixIn, ExtensionAware {
	private final ContainerElementsDynamicObject elementsDynamicObject;
	@Getter private final DomainObjectIdentifierInternal ownerIdentifier;
	private final DependencyBucketFactory factory;
	@Getter(AccessLevel.PROTECTED) private final DomainObjectSet<DependencyBucket> buckets;

	@Inject
	@Deprecated // use ObjectFactory
	public DefaultComponentDependencies(DomainObjectIdentifierInternal ownerIdentifier, DependencyBucketFactory factory, ObjectFactory objects) {
		this.ownerIdentifier = ownerIdentifier;
		this.factory = factory;
		this.buckets = objects.domainObjectSet(DependencyBucket.class);
		this.elementsDynamicObject = new ContainerElementsDynamicObject();
		buckets.all(bucket -> getExtensions().add(DependencyBucket.class, bucket.getName(), bucket));
	}

	@Override
	public DependencyBucket create(String name) {
		val identifier = DependencyBucketIdentifier.of(DependencyBucketName.of(name), DeclarableDependencyBucket.class, ownerIdentifier);
		val bucket = factory.create(identifier);
		getBuckets().add(bucket);
		return bucket;
	}

	@Override
	public DependencyBucket create(String name, Action<Configuration> action) {
		val identifier = DependencyBucketIdentifier.of(DependencyBucketName.of(name), DeclarableDependencyBucket.class, ownerIdentifier);
		val bucket = factory.create(identifier);
		action.execute(bucket.getAsConfiguration());
		getBuckets().add(bucket);
		return bucket;
	}

	@Override
	public DependencyBucket getByName(String name) {
		return Optional.ofNullable((DependencyBucket) getExtensions().findByName(name))
			.orElseThrow(() -> new UnknownDomainObjectException(String.format("%s with name '%s' not found.", getTypeDisplayName(), name)));
	}

	private String getTypeDisplayName() {
		return DependencyBucket.class.getSimpleName();
	}

	@Override
	public void add(String bucketName, Object notation) {
		assertBucketExists(bucketName);
		((DependencyBucket) getExtensions().getByName(bucketName)).addDependency(notation);
	}

	@Override
	public void add(String bucketName, Object notation, Action<? super ModuleDependency> action) {
		assertBucketExists(bucketName);
		((DependencyBucket) getExtensions().getByName(bucketName)).addDependency(notation, action);
	}

	@Override
	public void configureEach(Action<? super DependencyBucket> action) {
		getBuckets().configureEach(action);
	}

	@Override
	public Optional<DependencyBucket> findByName(String name) {
		return Optional.ofNullable((DependencyBucket) getExtensions().findByName(name));
	}

	private void assertBucketExists(String bucketName) {
		if (getExtensions().findByName(bucketName) == null) {
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
		private final Map<String, DependencyBucket> bucketIndex = new HashMap<>();

		ContainerElementsDynamicObject() {
			buckets.all(bucket -> bucketIndex.put(bucket.getName(), bucket));
		}

		@Override
		public String getDisplayName() {
			return "";
		}

		@Override
		public DynamicInvokeResult tryGetProperty(String name) {
			@Nullable val bucket = (DependencyBucket) getExtensions().findByName(name);
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
