/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.jni.internal.plugins;

import com.google.common.collect.Iterables;
import dev.nokee.language.jvm.internal.plugins.JvmLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.actions.ModelAction;
import dev.nokee.model.internal.actions.ModelActionSystem;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.BinaryIdentity;
import dev.nokee.platform.base.internal.CompileTaskTag;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.TaskRegistrationFactory;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.Variants;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.jni.JniJarBinary;
import dev.nokee.platform.jni.JvmJarBinary;
import dev.nokee.platform.jni.internal.JarTaskComponent;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryComponentRegistrationFactory;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryVariantRegistrationFactory;
import dev.nokee.platform.jni.internal.JniJarArtifactComponent;
import dev.nokee.platform.jni.internal.JniJarBinaryRegistrationFactory;
import dev.nokee.platform.jni.internal.JniLibraryComponentInternal;
import dev.nokee.platform.jni.internal.JniLibraryInternal;
import dev.nokee.platform.jni.internal.JvmJarArtifactComponent;
import dev.nokee.platform.jni.internal.JvmJarBinaryRegistrationFactory;
import dev.nokee.platform.jni.internal.ModelBackedJniJarBinary;
import dev.nokee.platform.jni.internal.ModelBackedJvmJarBinary;
import dev.nokee.platform.jni.internal.actions.WhenPlugin;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.tasks.bundling.Jar;

import static dev.nokee.model.internal.actions.ModelAction.configure;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.jni.internal.actions.WhenPlugin.any;
import static dev.nokee.utils.TaskUtils.configureBuildGroup;
import static dev.nokee.utils.TaskUtils.configureDescription;

public class JniLibraryBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(JvmLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		project.getExtensions().add("__nokee_jniJarBinaryFactory", new JniJarBinaryRegistrationFactory());
		project.getExtensions().add("__nokee_jvmJarBinaryFactory", new JvmJarBinaryRegistrationFactory());
		project.getExtensions().add("__nokee_jniLibraryComponentFactory", new JavaNativeInterfaceLibraryComponentRegistrationFactory(project));
		project.getExtensions().add("__nokee_jniLibraryVariantFactory", new JavaNativeInterfaceLibraryVariantRegistrationFactory(project));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionSystem.updateSelectorForTag(CompileTaskTag.class));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(JniLibraryInternal.class), ModelComponentReference.of(VariantIdentifier.class), (entity, projection, identifier) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val binaryIdentifier = BinaryIdentifier.of(identifier, BinaryIdentity.ofMain("jniJar", "JNI JAR binary"));
			val jniJar = registry.instantiate(ModelRegistration.builder()
				.withComponent(binaryIdentifier)
				.withComponent(new ElementNameComponent("jniJar"))
				.withComponent(new ParentComponent(entity))
				.withComponent(IsBinary.tag())
				.withComponent(ConfigurableTag.tag())
				.withComponent(createdUsing(of(ModelBackedJniJarBinary.class), ModelBackedJniJarBinary::new))
				.build());
			registry.instantiate(configure(jniJar.getId(), JniJarBinary.class, binary -> {
				binary.getJarTask().configure(task -> {
					task.getArchiveBaseName().set(project.provider(() -> {
						val baseName = ModelProperties.getProperty(entity, "baseName").as(String.class).get();
						return baseName + identifier.getAmbiguousDimensions().getAsKebabCase().map(it -> "-" + it).orElse("");
					}));
				});
			}));
			ModelStates.register(jniJar);
			entity.addComponent(new JniJarArtifactComponent(jniJar));
		})));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(ModelBackedJniJarBinary.class), ModelComponentReference.of(BinaryIdentifier.class), (entity, projection, identifier) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val taskRegistrationFactory = project.getExtensions().getByType(TaskRegistrationFactory.class);
			val jarTask = registry.instantiate(taskRegistrationFactory.create(TaskIdentifier.of(TaskName.of("jar"), identifier), Jar.class).withComponent(new ElementNameComponent("jar")).build());
			registry.instantiate(configure(jarTask.getId(), Jar.class, configureBuildGroup()));
			registry.instantiate(configure(jarTask.getId(), Jar.class, task -> {
				task.getDestinationDirectory().convention(task.getProject().getLayout().getBuildDirectory().dir("libs"));
			}));
			registry.instantiate(configure(jarTask.getId(), Jar.class, task -> {
				task.getArchiveBaseName().convention(identifier.getName().get());
			}));
			registry.instantiate(configure(jarTask.getId(), Jar.class, task -> {
				if (task.getDescription() == null) {
					task.setDescription(String.format("Assembles a JAR archive containing the shared library for %s.", identifier));
				}
			}));
			ModelStates.register(jarTask);
			entity.addComponent(new JarTaskComponent(jarTask));
		})));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(ModelBackedJvmJarBinary.class), ModelComponentReference.of(BinaryIdentifier.class), (entity, projection, identifier) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val taskRegistrationFactory = project.getExtensions().getByType(TaskRegistrationFactory.class);
			val jarTask = registry.instantiate(taskRegistrationFactory.create(TaskIdentifier.of(TaskName.of("jar"), identifier), Jar.class).withComponent(new ElementNameComponent("jar")).build());
			registry.instantiate(configure(jarTask.getId(), Jar.class, configureBuildGroup()));
			registry.instantiate(configure(jarTask.getId(), Jar.class, task -> {
				task.getDestinationDirectory().convention(task.getProject().getLayout().getBuildDirectory().dir("libs"));
			}));
			registry.instantiate(configure(jarTask.getId(), Jar.class, task -> task.getArchiveBaseName().convention(identifier.getName().get())));
			registry.instantiate(configure(jarTask.getId(), Jar.class, task -> {
				if (task.getDescription() == null) {
					task.setDescription(String.format("Assembles a JAR archive containing the classes for %s.", identifier));
				}
			}));
			ModelStates.register(jarTask);
			entity.addComponent(new JarTaskComponent(jarTask));
		})));


		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(Variants.class), ModelComponentReference.of(JvmJarArtifactComponent.class), (entity, variants, jvmJar) -> {
			if (Iterables.size(variants) == 1) {
				project.getExtensions().getByType(ModelRegistry.class).instantiate(ModelAction.configure(jvmJar.get().getId(), JvmJarBinary.class, binary -> {
					binary.getJarTask().configure(configureDescription("Assembles a JAR archive containing the classes and shared library for %s.", ModelNodes.of(binary).getComponent(BinaryIdentifier.class)));
				}));
			}
		}));

		val registerJvmJarBinaryAction = new Action<AppliedPlugin>() {
			@Override
			public void execute(AppliedPlugin ignored) {
				project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(JniLibraryComponentInternal.class), ModelComponentReference.of(ComponentIdentifier.class), (entity, projection, identifier) -> {
					val registry = project.getExtensions().getByType(ModelRegistry.class);
					val binaryIdentifier = BinaryIdentifier.of(identifier, BinaryIdentity.ofMain("jvmJar", "JVM JAR binary"));
					val jvmJar = registry.instantiate(ModelRegistration.builder()
						.withComponent(binaryIdentifier)
						.withComponent(new ElementNameComponent("jvmJar"))
						.withComponent(new ParentComponent(entity))
						.withComponent(IsBinary.tag())
						.withComponent(ConfigurableTag.tag())
						.withComponent(createdUsing(of(ModelBackedJvmJarBinary.class), ModelBackedJvmJarBinary::new))
						.build());
					registry.instantiate(configure(jvmJar.getId(), JvmJarBinary.class, binary -> {
						binary.getJarTask().configure(task -> task.getArchiveBaseName().set(project.provider(() -> {
							return ModelProperties.getProperty(entity, "baseName").as(String.class).get();
						})));
					}));
					ModelStates.register(jvmJar);
					entity.addComponent(new JvmJarArtifactComponent(jvmJar));
				})));
			}
		};
		new WhenPlugin(any("java", "groovy", "org.jetbrains.kotlin.jvm"), registerJvmJarBinaryAction).execute(project);
	}
}
