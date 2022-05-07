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
package dev.nokee.language.jvm.internal.plugins;

import dev.nokee.language.base.internal.SourcePropertyComponent;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.jvm.internal.GroovySourceSetRegistrationFactory;
import dev.nokee.language.jvm.internal.JavaSourceSetRegistrationFactory;
import dev.nokee.language.jvm.internal.KotlinSourceSetRegistrationFactory;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.model.internal.type.ModelTypeUtils;
import dev.nokee.platform.base.internal.TaskRegistrationFactory;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;

public class JvmLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		project.getPlugins().withType(JavaBasePlugin.class, ignored -> {
			project.getExtensions().add("__nokee_javaSourceSetFactory", new JavaSourceSetRegistrationFactory());
			project.getExtensions().add("__nokee_groovySourceSetFactory", new GroovySourceSetRegistrationFactory());
			project.getExtensions().add("__nokee_kotlinSourceSetFactory", new KotlinSourceSetRegistrationFactory());

			val sourceSetRegistry = NamedDomainObjectRegistry.of(project.getExtensions().getByType(SourceSetContainer.class));

			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val taskRegistrationFactory = project.getExtensions().getByType(TaskRegistrationFactory.class);
			project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(JavaSourceSetRegistrationFactory.DefaultJavaSourceSet.Tag.class), ModelComponentReference.of(IdentifierComponent.class), (entity, tag, identifier) -> {
				registry.register(taskRegistrationFactory.create(TaskIdentifier.of(TaskName.of("compile"), JavaCompile.class, identifier.get()), JavaCompile.class).build());
			})));
			project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(GroovySourceSetRegistrationFactory.DefaultGroovySourceSet.Tag.class), ModelComponentReference.of(IdentifierComponent.class), (entity, projection, identifier) -> {
				registry.register(taskRegistrationFactory.create(TaskIdentifier.of(TaskName.of("compile"), GroovyCompile.class, identifier.get()), GroovyCompile.class).build());
			})));
			// ComponentFromEntity<FullyQualifiedNameComponent> read-only (on parent only)
			project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(KotlinSourceSetRegistrationFactory.DefaultKotlinSourceSet.Tag.class), ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(FullyQualifiedNameComponent.class), (entity, projection, identifier, parent, fullyQualifiedName) -> {
				val sourceSetProvider = sourceSetRegistry.registerIfAbsent(parent.get().get(FullyQualifiedNameComponent.class).get().toString());
				@SuppressWarnings("unchecked")
				val KotlinCompile  = (Class<Task>) ModelTypeUtils.toUndecoratedType(sourceSetProvider.flatMap(it -> project.getTasks().named(it.getCompileTaskName("kotlin"))).get().getClass());
				registry.register(taskRegistrationFactory.create(TaskIdentifier.of(TaskName.of("compile"), KotlinCompile, identifier.get()), KotlinCompile).build());
			})));

			// ComponentFromEntity<FullyQualifiedNameComponent> read-only (on parent only)
			project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(GroovySourceSetRegistrationFactory.DefaultGroovySourceSet.Tag.class), ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(SourcePropertyComponent.class), (entity, tag, parent, sourceProperty) -> {
				val sourceSetProvider = sourceSetRegistry.registerIfAbsent(parent.get().get(FullyQualifiedNameComponent.class).get().toString());
				sourceSetProvider.get();
				((ConfigurableFileCollection) sourceProperty.get().get(GradlePropertyComponent.class).get()).from(sourceSetProvider.map(GroovySourceSetRegistrationFactory::asSourceDirectorySet));
			}));
			project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(JavaSourceSetRegistrationFactory.DefaultJavaSourceSet.Tag.class), ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(SourcePropertyComponent.class), (entity, tag, parent, sourceProperty) -> {
				val sourceSetProvider = sourceSetRegistry.registerIfAbsent(parent.get().get(FullyQualifiedNameComponent.class).get().toString());
				((ConfigurableFileCollection) sourceProperty.get().get(GradlePropertyComponent.class).get()).from(sourceSetProvider.map(JavaSourceSetRegistrationFactory::asSourceDirectorySet));
			}));
			project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(KotlinSourceSetRegistrationFactory.DefaultKotlinSourceSet.Tag.class), ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(SourcePropertyComponent.class), (entity, tag, parent, sourceProperty) -> {
				val sourceSetProvider = sourceSetRegistry.registerIfAbsent(parent.get().get(FullyQualifiedNameComponent.class).get().toString());
				((ConfigurableFileCollection) sourceProperty.get().get(GradlePropertyComponent.class).get()).from(sourceSetProvider.map(KotlinSourceSetRegistrationFactory::asSourceDirectorySet));
			}));
		});
	}
}
