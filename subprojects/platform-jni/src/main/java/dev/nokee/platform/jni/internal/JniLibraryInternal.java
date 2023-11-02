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

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.base.internal.SourceViewAdapter;
import dev.nokee.language.nativebase.internal.NativeSourcesAwareTag;
import dev.nokee.model.internal.core.ModelElements;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.BaseVariant;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedDependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.ModelBackedSourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.VariantMixIn;
import dev.nokee.platform.base.internal.assembletask.HasAssembleTaskMixIn;
import dev.nokee.platform.base.internal.developmentbinary.HasDevelopmentBinaryMixIn;
import dev.nokee.platform.jni.JavaNativeInterfaceNativeComponentDependencies;
import dev.nokee.platform.jni.JniJarBinary;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.utils.ConfigureUtils;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;

import static dev.nokee.runtime.nativebase.TargetMachine.TARGET_MACHINE_COORDINATE_AXIS;

@DomainObjectEntities.Tag(NativeSourcesAwareTag.class)
public /*final*/ abstract class JniLibraryInternal extends BaseVariant implements JniLibrary, VariantInternal, ModelNodeAware
	, VariantMixIn
	, ModelBackedTaskAwareComponentMixIn
	, ModelBackedSourceAwareComponentMixIn<SourceView<LanguageSourceSet>, SourceViewAdapter<LanguageSourceSet>>
	, ModelBackedDependencyAwareComponentMixIn<JavaNativeInterfaceNativeComponentDependencies, ModelBackedJavaNativeInterfaceNativeComponentDependencies>
	, ModelBackedBinaryAwareComponentMixIn
	, ModelBackedHasBaseNameMixIn
	, HasDevelopmentBinaryMixIn
	, HasAssembleTaskMixIn
{
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Override
	public abstract Property<String> getResourcePath();

	public abstract ConfigurableFileCollection getNativeRuntimeFiles();

	@Override
	public Property<Binary> getDevelopmentBinary() {
		return HasDevelopmentBinaryMixIn.super.getDevelopmentBinary();
	}

	@Override
	public JniJarBinary getJavaNativeInterfaceJar() {
		return ModelElements.of(this).element("jniJar", JniJarBinary.class).get();
	}

	public JniJarBinary getJar() {
		return ModelElements.of(this).element("jniJar", JniJarBinary.class).get();
	}

	public SharedLibraryBinary getSharedLibrary() {
		return ModelElements.of(this).element("sharedLibrary", SharedLibraryBinary.class).get();
	}

	@Override
	public void sharedLibrary(Action<? super SharedLibraryBinary> action) {
		action.execute(getSharedLibrary());
	}

	@Override
	public void sharedLibrary(@SuppressWarnings("rawtypes") Closure closure) {
		sharedLibrary(ConfigureUtils.configureUsing(closure));
	}

	public TaskProvider<Task> getSharedLibraryTask() {
		return (TaskProvider<Task>) ModelElements.of(this).element("sharedLibrary", Task.class).asProvider();
	}

	public TaskProvider<Task> getObjectsTask() {
		return (TaskProvider<Task>) ModelElements.of(this).element("objects", Task.class).asProvider();
	}

	public TargetMachine getTargetMachine() {
		return getBuildVariant().getAxisValue(TARGET_MACHINE_COORDINATE_AXIS);
	}

	public TaskProvider<Task> getAssembleTask() {
		return (TaskProvider<Task>) ModelElements.of(this).element("assemble", Task.class).asProvider();
	}

	@Override
	@SuppressWarnings("unchecked")
	public BinaryView<Binary> getBinaries() {
		return ModelProperties.getProperty(this, "binaries").as(BinaryView.class).get();
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
