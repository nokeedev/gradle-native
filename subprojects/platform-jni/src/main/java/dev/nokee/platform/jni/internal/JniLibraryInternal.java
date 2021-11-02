/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.jni.internal;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.nativebase.internal.ObjectSourceSet;
import dev.nokee.model.internal.DomainObjectCreated;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ResolvableComponentDependencies;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.jni.JavaNativeInterfaceNativeComponentDependencies;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.TargetMachine;
import groovy.lang.Closure;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;

import static dev.nokee.runtime.nativebase.TargetMachine.TARGET_MACHINE_COORDINATE_AXIS;

public class JniLibraryInternal extends BaseVariant implements JniLibrary, VariantInternal {
	private final DefaultJavaNativeInterfaceNativeComponentDependencies dependencies;
	@Getter(AccessLevel.PROTECTED) private final ConfigurationContainer configurations;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	private final FunctionalSourceSet sources;
	private final DomainObjectEventPublisher eventPublisher;
	private final TaskViewFactory taskViewFactory;
	private final TargetMachine targetMachine;
	private final GroupId groupId;
	private final TaskRegistry taskRegistry;
	private AbstractJarBinary jarBinary;
	private SharedLibraryBinaryInternal sharedLibraryBinary;
	@Getter private final Property<String> resourcePath;
	@Getter private final ConfigurableFileCollection nativeRuntimeFiles;
	private final ResolvableComponentDependencies resolvableDependencies;

	@Inject
	public JniLibraryInternal(VariantIdentifier<JniLibraryInternal> identifier, FunctionalSourceSet parentSources, GroupId groupId, VariantComponentDependencies dependencies, ObjectFactory objects, ConfigurationContainer configurations, ProviderFactory providers, TaskRegistry taskRegistry, DomainObjectEventPublisher eventPublisher, BinaryViewFactory binaryViewFactory, TaskViewFactory taskViewFactory) {
		super(identifier, objects, binaryViewFactory);
		this.dependencies = dependencies.getDependencies();
		this.configurations = configurations;
		this.providers = providers;
		this.sources = parentSources;
		this.eventPublisher = eventPublisher;
		this.taskViewFactory = taskViewFactory;
		this.targetMachine = getBuildVariant().getAxisValue(TARGET_MACHINE_COORDINATE_AXIS);
		this.groupId = groupId;
		this.resourcePath = objects.property(String.class);
		this.nativeRuntimeFiles = objects.fileCollection();
		this.resolvableDependencies = dependencies.getIncoming();
		this.taskRegistry = taskRegistry;

		getResourcePath().convention(getProviders().provider(() -> getResourcePath(groupId)));
		getDevelopmentBinary().convention(providers.provider(this::getJar));
	}

	@Override
	public String getName() {
		return VariantNamer.INSTANCE.determineName(getIdentifier());
	}

	public ResolvableComponentDependencies getResolvableDependencies() {
		return resolvableDependencies;
	}

	private String getResourcePath(GroupId groupId) {
		return groupId.get().map(it -> it.replace('.', '/') + '/').orElse("") + getIdentifier().getAmbiguousDimensions().getAsKebabCase().orElse("");
	}

	public FunctionalSourceSet getSources() {
		return sources;
	}

	public void registerSharedLibraryBinary(DomainObjectSet<ObjectSourceSet> objectSourceSets, TaskProvider<LinkSharedLibraryTask> linkTask, NativeIncomingDependencies dependencies) {
		val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("sharedLibrary"), SharedLibraryBinaryInternal.class, getIdentifier());

		val sharedLibraryBinary = getObjects().newInstance(SharedLibraryBinaryInternal.class, binaryIdentifier, targetMachine, objectSourceSets, linkTask, dependencies, taskViewFactory);
		eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifier, sharedLibraryBinary));

		getNativeRuntimeFiles().from(linkTask.flatMap(AbstractLinkTask::getLinkedFile));
		getNativeRuntimeFiles().from(sharedLibraryBinary.getRuntimeLibrariesDependencies());
		this.sharedLibraryBinary = sharedLibraryBinary;
		sharedLibraryBinary.getBaseName().convention(BaseNameUtils.from(getIdentifier()).getAsString());
	}

	public void registerJniJarBinary() {
		TaskProvider<Jar> jarTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of("jar"), Jar.class, getIdentifier()));
		addJniJarBinary(getObjects().newInstance(DefaultJniJarBinary.class, jarTask));
	}

	public AbstractJarBinary getJar() {
		return jarBinary;
	}

	public SharedLibraryBinaryInternal getSharedLibrary() {
		return sharedLibraryBinary;
	}

	@Override
	public void sharedLibrary(Action<? super SharedLibraryBinary> action) {
		action.execute(sharedLibraryBinary);
	}

	public void addJniJarBinary(AbstractJarBinary jniJarBinary) {
		jarBinary = jniJarBinary;
		Class<? extends AbstractJarBinary> type = DefaultJniJarBinary.class;
		if (jniJarBinary instanceof DefaultJvmJarBinary) {
			type = DefaultJvmJarBinary.class;
		}
		val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("jniJar"), type, getIdentifier());
		eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifier, jniJarBinary));
	}

	public void addJvmJarBinary(DefaultJvmJarBinary jvmJarBinary) {
		val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("jvmJar"), DefaultJvmJarBinary.class, getIdentifier());
		eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifier, jvmJarBinary));
	}

	public TargetMachine getTargetMachine() {
		return targetMachine;
	}

	public TaskProvider<Task> getAssembleTask() {
		return ModelProperties.getProperty(this, "assembleTask").as(TaskProvider.class).get();
	}

	@Override
	public DefaultJavaNativeInterfaceNativeComponentDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public void dependencies(Action<? super JavaNativeInterfaceNativeComponentDependencies> action) {
		action.execute(dependencies);
	}

	@Override
	public void dependencies(@SuppressWarnings("rawtypes") Closure closure) {
		dependencies(ConfigureUtil.configureUsing(closure));
	}

	@Override
	public void binaries(Action<? super BinaryView<Binary>> action) {
		action.execute(getBinaries());
	}

	@Override
	public void binaries(@SuppressWarnings("rawtypes") Closure closure) {
		binaries(ConfigureUtil.configureUsing(closure));
	}
}
