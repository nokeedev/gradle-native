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

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetRegistrationFactory;
import dev.nokee.language.base.internal.ModelBackedLanguageSourceSetLegacyMixIn;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.jvm.GroovySourceSet;
import dev.nokee.language.jvm.JavaSourceSet;
import dev.nokee.language.jvm.KotlinSourceSet;
import dev.nokee.language.jvm.internal.JvmSourceSetExtensible;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.core.ModelActions;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ParentNode;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentSources;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.lang.reflect.InvocationTargetException;

import static dev.nokee.language.base.internal.SourceSetExtensible.discoveringInstanceOf;
import static dev.nokee.model.internal.core.ModelActions.matching;
import static dev.nokee.model.internal.type.ModelType.of;

public class JvmLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		val modelConfigurer = project.getExtensions().getByType(ModelConfigurer.class);
		project.getPluginManager().withPlugin("java-base", appliedPlugin -> {
			modelConfigurer.configure(matching(discoveringInstanceOf(JvmSourceSetExtensible.class), ModelActions.once(sources -> {
				sourceSetContainer(project).matching(nameOf(componentOf(sources))).all(registerJavaSourceSet(sources, project));
			})));
		});

		project.getPluginManager().withPlugin("groovy-base", appliedPlugin -> {
			modelConfigurer.configure(matching(discoveringInstanceOf(JvmSourceSetExtensible.class), ModelActions.once(sources -> {
				sourceSetContainer(project).matching(nameOf(componentOf(sources))).all(registerGroovySourceSet(sources, project));
			})));
		});

		project.getPluginManager().withPlugin("org.jetbrains.kotlin.jvm", appliedPlugin -> {
			modelConfigurer.configure(matching(discoveringInstanceOf(JvmSourceSetExtensible.class), ModelActions.once(sources -> {
				sourceSetContainer(project).matching(nameOf(componentOf(sources))).all(registerKotlinSourceSet(sources, project));
			})));
		});
	}

	private static SourceSetContainer sourceSetContainer(Project project) {
		return project.getExtensions().getByType(SourceSetContainer.class);
	}

	private static ModelNode componentOf(ModelNode sources) {
		// We assume it's a ComponentSources model node
		assert ModelNodeUtils.canBeViewedAs(sources, of(ComponentSources.class));
		assert ModelNodeUtils.getParent(sources).isPresent();
		assert ModelNodeUtils.canBeViewedAs(ModelNodeUtils.getParent(sources).get(), of(Component.class));
		return ModelNodeUtils.getParent(sources).get();
	}

	private static Spec<? super SourceSet> nameOf(ModelNode component) {
		// We assume it's a Component model node
		assert ModelNodeUtils.canBeViewedAs(component, of(Component.class));
		String componentName = ModelNodeUtils.getPath(component).getName();
		return sourceSet -> sourceSet.getName().equals(componentName);
	}

	private static Action<SourceSet> registerJavaSourceSet(ModelNode sources, Project project) {
		return new Action<SourceSet>() {
			@Override
			public void execute(SourceSet sourceSet) {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val sourceSetFactory = project.getExtensions().getByType(LanguageSourceSetRegistrationFactory.class);
				val directorySet = asSourceDirectorySet(sourceSet);
				registry.register(sourceSetFactory.create(LanguageSourceSetIdentifier.of(sources.getComponent(ParentNode.class).get().getComponent(DomainObjectIdentifier.class), directorySet.getName()), JavaSourceSet.class, DefaultJavaSourceSet.class, directorySet).build());
			}

			private SourceDirectorySet asSourceDirectorySet(SourceSet sourceSet) {
				return sourceSet.getJava();
			}
		};
	}

	public static class DefaultJavaSourceSet implements JavaSourceSet, ModelBackedLanguageSourceSetLegacyMixIn<JavaSourceSet> {}

	private static Action<SourceSet> registerGroovySourceSet(ModelNode sources, Project project) {
		return new Action<SourceSet>() {
			@Override
			public void execute(SourceSet sourceSet) {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val sourceSetFactory = project.getExtensions().getByType(LanguageSourceSetRegistrationFactory.class);
				val directorySet = asSourceDirectorySet(sourceSet);
				registry.register(sourceSetFactory.create(LanguageSourceSetIdentifier.of(sources.getComponent(ParentNode.class).get().getComponent(DomainObjectIdentifier.class), directorySet.getName()), GroovySourceSet.class, DefaultGroovySourceSet.class, directorySet).build());
			}

			private SourceDirectorySet asSourceDirectorySet(SourceSet sourceSet) {
				return ((org.gradle.api.tasks.GroovySourceSet) new DslObject(sourceSet).getConvention().getPlugins().get("groovy")).getGroovy();
			}
		};
	}

	public static class DefaultGroovySourceSet implements GroovySourceSet, ModelBackedLanguageSourceSetLegacyMixIn<GroovySourceSet> {}

	private static Action<SourceSet> registerKotlinSourceSet(ModelNode sources, Project project) {
		return new Action<SourceSet>() {
			@Override
			public void execute(SourceSet sourceSet) {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val sourceSetFactory = project.getExtensions().getByType(LanguageSourceSetRegistrationFactory.class);
				val directorySet = asSourceDirectorySet(sourceSet);
				registry.register(sourceSetFactory.create(LanguageSourceSetIdentifier.of(sources.getComponent(ParentNode.class).get().getComponent(DomainObjectIdentifier.class), "kotlin"), KotlinSourceSet.class, DefaultKotlinSourceSet.class, directorySet).build());
			}

			private SourceDirectorySet asSourceDirectorySet(SourceSet sourceSet) {
				try {
					val kotlinSourceSet = new DslObject(sourceSet).getConvention().getPlugins().get("kotlin");
					val DefaultKotlinSourceSet = kotlinSourceSet.getClass();
					val getKotlin = DefaultKotlinSourceSet.getMethod("getKotlin");
					return (SourceDirectorySet) getKotlin.invoke(kotlinSourceSet);
				} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	public static class DefaultKotlinSourceSet implements KotlinSourceSet, ModelBackedLanguageSourceSetLegacyMixIn<KotlinSourceSet> {}
}
