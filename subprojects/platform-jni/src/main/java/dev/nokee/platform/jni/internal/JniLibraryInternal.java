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

import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.base.internal.SourceViewAdapter;
import dev.nokee.language.nativebase.internal.NativeSourcesAware;
import dev.nokee.model.capabilities.variants.IsVariant;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.core.ModelElements;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.HasDevelopmentBinary;
import dev.nokee.platform.base.internal.BaseVariant;
import dev.nokee.platform.base.internal.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.SourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.jni.JavaNativeInterfaceNativeComponentDependencies;
import dev.nokee.platform.jni.JniJarBinary;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.utils.ConfigureUtils;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

import static dev.nokee.runtime.nativebase.TargetMachine.TARGET_MACHINE_COORDINATE_AXIS;

@DomainObjectEntities.Tag({IsVariant.class})
public /*final*/ abstract class JniLibraryInternal extends BaseVariant implements JniLibrary, VariantInternal, ModelNodeAware
	, NativeSourcesAware
	, ModelBackedTaskAwareComponentMixIn
	, SourceAwareComponentMixIn<SourceView<LanguageSourceSet>, SourceViewAdapter<LanguageSourceSet>>
	, DependencyAwareComponentMixIn<JavaNativeInterfaceNativeComponentDependencies>
	, BinaryAwareComponentMixIn
	, HasBaseName
	, HasDevelopmentBinary
{
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Inject
	public JniLibraryInternal(ObjectFactory objects, ModelObjectRegistry<Task> taskRegistry, ModelObjectRegistry<DependencyBucket> bucketRegistry, Factory<BinaryView<Binary>> binariesFactory, Factory<SourceView<LanguageSourceSet>> sourcesFactory) {
		getExtensions().create("dependencies", DefaultJavaNativeInterfaceNativeComponentDependencies.class, getIdentifier(), bucketRegistry);
		getExtensions().add("developmentBinary", objects.property(Binary.class));
		getExtensions().add("baseName", objects.property(String.class));
		getExtensions().add("assembleTask", taskRegistry.register(getIdentifier().child(TaskName.of("assemble")), Task.class).asProvider());
		getExtensions().add("sharedLibraryTask", taskRegistry.register(getIdentifier().child(TaskName.of("sharedLibrary")), Task.class).asProvider());
		getExtensions().add("objectsTask", taskRegistry.register(getIdentifier().child(TaskName.of("objects")), Task.class).asProvider());
		getExtensions().add("binaries", binariesFactory.create());
		getExtensions().add("sources", sourcesFactory.create());
	}

	@Override
	public DefaultJavaNativeInterfaceNativeComponentDependencies getDependencies() {
		return (DefaultJavaNativeInterfaceNativeComponentDependencies) DependencyAwareComponentMixIn.super.getDependencies();
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return BinaryAwareComponentMixIn.super.getBinaries();
	}

	@Override
	public abstract Property<String> getResourcePath();

	public abstract ConfigurableFileCollection getNativeRuntimeFiles();

	@Override
	@SuppressWarnings("unchecked")
	public Property<Binary> getDevelopmentBinary() {
		return (Property<Binary>) getExtensions().getByName("developmentBinary");
	}

	@Override
	@SuppressWarnings("unchecked")
	public Property<String> getBaseName() {
		return (Property<String>) getExtensions().getByName("baseName");
	}

	@Override
	public JniJarBinary getJavaNativeInterfaceJar() {
		return ModelElements.of(this).element("jniJar", JniJarBinary.class).get();
	}

	public JniJarBinary getJar() {
		return ModelElements.of(this).element("jniJar", JniJarBinary.class).get();
	}

	public SharedLibraryBinaryInternal getSharedLibrary() {
		return (SharedLibraryBinaryInternal) ModelElements.of(this).element("sharedLibrary", SharedLibraryBinary.class).get();
	}

	@Override
	public void sharedLibrary(Action<? super SharedLibraryBinary> action) {
		action.execute(getSharedLibrary());
	}

	@Override
	public void sharedLibrary(@SuppressWarnings("rawtypes") Closure closure) {
		sharedLibrary(ConfigureUtils.configureUsing(closure));
	}

	@SuppressWarnings("unchecked")
	public TaskProvider<Task> getSharedLibraryTask() {
		return (TaskProvider<Task>) getExtensions().getByName("sharedLibraryTask");
	}

	@SuppressWarnings("unchecked")
	public TaskProvider<Task> getObjectsTask() {
		return (TaskProvider<Task>) getExtensions().getByName("objectsTask");
	}

	public TargetMachine getTargetMachine() {
		return getBuildVariant().getAxisValue(TARGET_MACHINE_COORDINATE_AXIS);
	}

	@SuppressWarnings("unchecked")
	public TaskProvider<Task> getAssembleTask() {
		return (TaskProvider<Task>) getExtensions().getByName("assembleTask");
	}

	@Override
	public ModelNode getNode() {
		return node;
	}

	@Override
	public String toString() {
		return "JNI library '" + getName() + "'";
	}
}
