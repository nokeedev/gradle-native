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
import dev.nokee.language.jvm.internal.CompileTaskComponent;
import dev.nokee.language.jvm.internal.GroovySourceSetSpec;
import dev.nokee.language.jvm.internal.JavaSourceSetSpec;
import dev.nokee.language.jvm.internal.JvmSourceSetTag;
import dev.nokee.language.jvm.internal.KotlinSourceSetSpec;
import dev.nokee.language.jvm.internal.SourceSetComponent;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.tags.ModelComponentTag;
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
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;

import java.lang.reflect.InvocationTargetException;

public class JvmLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		project.getPlugins().withType(JavaBasePlugin.class, ignored -> {
			// ComponentFromEntity<FullyQualifiedNameComponent> read-only (on parent only)
			project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(JvmSourceSetTag.class), ModelComponentReference.of(ParentComponent.class), (entity, projection, parent) -> {
				val sourceSetRegistry = NamedDomainObjectRegistry.of(project.getExtensions().getByType(SourceSetContainer.class));
				val sourceSetProvider = sourceSetRegistry.registerIfAbsent(parent.get().get(FullyQualifiedNameComponent.class).get().toString());
				entity.addComponent(new SourceSetComponent(sourceSetProvider));
			})));

			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val taskRegistrationFactory = project.getExtensions().getByType(TaskRegistrationFactory.class);
			project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(JavaSourceSetSpec.Tag.class), ModelComponentReference.of(IdentifierComponent.class), (entity, tag, identifier) -> {
				val compileTask = registry.register(taskRegistrationFactory.create(TaskIdentifier.of(TaskName.of("compile"), JavaCompile.class, identifier.get()), JavaCompile.class).build());
				entity.addComponent(new CompileTaskComponent(ModelNodes.of(compileTask)));
			})));
			project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(GroovySourceSetSpec.Tag.class), ModelComponentReference.of(IdentifierComponent.class), (entity, projection, identifier) -> {
				val compileTask = registry.register(taskRegistrationFactory.create(TaskIdentifier.of(TaskName.of("compile"), GroovyCompile.class, identifier.get()), GroovyCompile.class).build());
				entity.addComponent(new CompileTaskComponent(ModelNodes.of(compileTask)));
			})));
			project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(KotlinSourceSetSpec.Tag.class), ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(SourceSetComponent.class), (entity, projection, identifier, parent, sourceSet) -> {
				val sourceSetProvider = sourceSet.get();
				@SuppressWarnings("unchecked")
				val KotlinCompile  = (Class<Task>) ModelTypeUtils.toUndecoratedType(sourceSetProvider.flatMap(it -> project.getTasks().named(it.getCompileTaskName("kotlin"))).get().getClass());
				val compileTask = registry.register(taskRegistrationFactory.create(TaskIdentifier.of(TaskName.of("compile"), KotlinCompile, identifier.get()), KotlinCompile).build());
				entity.addComponent(new CompileTaskComponent(ModelNodes.of(compileTask)));
			})));

			// ComponentFromEntity<FullyQualifiedNameComponent> read-only (on parent only)
			project.getExtensions().getByType(ModelConfigurer.class).configure(new AttachGroovySourcesToGroovySourceSet());
			project.getExtensions().getByType(ModelConfigurer.class).configure(new AttachJavaSourcesToJavaSourceSet());
			project.getExtensions().getByType(ModelConfigurer.class).configure(new AttachKotlinSourcesToKotlinSourceSet());
		});
	}

	private static final class AttachGroovySourcesToGroovySourceSet extends ModelActionWithInputs.ModelAction4<ModelComponentTag<GroovySourceSetSpec.Tag>, ParentComponent, SourcePropertyComponent, SourceSetComponent> {
		private AttachGroovySourcesToGroovySourceSet() {
			super(ModelTags.referenceOf(GroovySourceSetSpec.Tag.class), ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(SourcePropertyComponent.class), ModelComponentReference.of(SourceSetComponent.class));
		}

		@Override
		protected void execute(ModelNode entity, ModelComponentTag<GroovySourceSetSpec.Tag> tag, ParentComponent parent, SourcePropertyComponent sourceProperty, SourceSetComponent sourceSet) {
			val sourceSetProvider = sourceSet.get();
			sourceSetProvider.get();
			((ConfigurableFileCollection) sourceProperty.get().get(GradlePropertyComponent.class).get()).from(sourceSetProvider.map(AttachGroovySourcesToGroovySourceSet::asSourceDirectorySet));
		}

		private static SourceDirectorySet asSourceDirectorySet(SourceSet sourceSet) {
			return ((org.gradle.api.tasks.GroovySourceSet) new DslObject(sourceSet).getConvention().getPlugins().get("groovy")).getGroovy();
		}
	}

	private static final class AttachJavaSourcesToJavaSourceSet extends ModelActionWithInputs.ModelAction4<ModelComponentTag<JavaSourceSetSpec.Tag>, ParentComponent, SourcePropertyComponent, SourceSetComponent> {
		private AttachJavaSourcesToJavaSourceSet() {
			super(ModelTags.referenceOf(JavaSourceSetSpec.Tag.class), ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(SourcePropertyComponent.class), ModelComponentReference.of(SourceSetComponent.class));
		}

		@Override
		protected void execute(ModelNode entity, ModelComponentTag<JavaSourceSetSpec.Tag> tag, ParentComponent parent, SourcePropertyComponent sourceProperty, SourceSetComponent sourceSet) {
			val sourceSetProvider = sourceSet.get();
			((ConfigurableFileCollection) sourceProperty.get().get(GradlePropertyComponent.class).get()).from(sourceSetProvider.map(AttachJavaSourcesToJavaSourceSet::asSourceDirectorySet));
		}

		private static SourceDirectorySet asSourceDirectorySet(SourceSet sourceSet) {
			return sourceSet.getJava();
		}
	}

	private static final class AttachKotlinSourcesToKotlinSourceSet extends ModelActionWithInputs.ModelAction4<ModelComponentTag<KotlinSourceSetSpec.Tag>, ParentComponent, SourcePropertyComponent, SourceSetComponent> {
		private AttachKotlinSourcesToKotlinSourceSet() {
			super(ModelTags.referenceOf(KotlinSourceSetSpec.Tag.class), ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(SourcePropertyComponent.class), ModelComponentReference.of(SourceSetComponent.class));
		}

		@Override
		protected void execute(ModelNode entity, ModelComponentTag<KotlinSourceSetSpec.Tag> tag, ParentComponent parent, SourcePropertyComponent sourceProperty, SourceSetComponent sourceSet) {
			val sourceSetProvider = sourceSet.get();
			((ConfigurableFileCollection) sourceProperty.get().get(GradlePropertyComponent.class).get()).from(sourceSetProvider.map(AttachKotlinSourcesToKotlinSourceSet::asSourceDirectorySet));
		}

		private static SourceDirectorySet asSourceDirectorySet(SourceSet sourceSet) {
			try {
				val kotlinSourceSet = new DslObject(sourceSet).getConvention().getPlugins().get("kotlin");
				val DefaultKotlinSourceSet = kotlinSourceSet.getClass();
				val getKotlin = DefaultKotlinSourceSet.getMethod("getKotlin");
				return (SourceDirectorySet) getKotlin.invoke(kotlinSourceSet);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
