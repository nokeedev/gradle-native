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
import dev.nokee.model.internal.core.*;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.SourceView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ResolvableComponentDependencies;
import dev.nokee.platform.jni.JavaNativeInterfaceNativeComponentDependencies;
import dev.nokee.platform.jni.JniJarBinary;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.runtime.nativebase.TargetMachine;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;

import static dev.nokee.runtime.nativebase.TargetMachine.TARGET_MACHINE_COORDINATE_AXIS;

public class JniLibraryInternal extends BaseVariant implements JniLibrary, VariantInternal, ModelNodeAware, HasPublicType
	, ModelBackedTaskAwareComponentMixIn
	, ModelBackedSourceAwareComponentMixIn<SourceView<LanguageSourceSet>>
	, ModelBackedDependencyAwareComponentMixIn<JavaNativeInterfaceNativeComponentDependencies>
	, ModelBackedBinaryAwareComponentMixIn
	, ModelBackedHasBaseNameMixIn
{
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Inject
	public JniLibraryInternal(VariantIdentifier<JniLibraryInternal> identifier, ObjectFactory objects, BinaryViewFactory binaryViewFactory) {
		super(identifier, objects, binaryViewFactory);
	}

	@Override
	public Property<String> getResourcePath() {
		return ModelProperties.getProperty(this, "resourcePath").as(Property.class).get();
	}

	@Override
	public String getName() {
		return VariantNamer.INSTANCE.determineName(getIdentifier());
	}

	public ConfigurableFileCollection getNativeRuntimeFiles() {
		return ModelProperties.getProperty(this, "nativeRuntimeFiles").as(ConfigurableFileCollection.class).get();
	}

	@Override
	public Property<Binary> getDevelopmentBinary() {
		return ModelProperties.getProperty(this, "developmentBinary").as(Property.class).get();
	}

	public ResolvableComponentDependencies getResolvableDependencies() {
		return node.getComponent(NativeIncomingDependencies.class);
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
		sharedLibrary(ConfigureUtil.configureUsing(closure));
	}

	public TargetMachine getTargetMachine() {
		return getBuildVariant().getAxisValue(TARGET_MACHINE_COORDINATE_AXIS);
	}

	public TaskProvider<Task> getAssembleTask() {
		return (TaskProvider<Task>) ModelElements.of(this).element("assemble", Task.class).asProvider();
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return ModelProperties.getProperty(this, "binaries").as(BinaryView.class).get();
	}

	@Override
	public ModelNode getNode() {
		return node;
	}

	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(JniLibrary.class);
	}

	@Override
	public String toString() {
		return node.getComponent(VariantIdentifier.class).getDisplayName();
	}
}
