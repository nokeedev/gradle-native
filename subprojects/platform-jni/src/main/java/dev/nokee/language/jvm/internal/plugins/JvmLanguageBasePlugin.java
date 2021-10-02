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

import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.jvm.GroovySourceSet;
import dev.nokee.language.jvm.JavaSourceSet;
import dev.nokee.language.jvm.KotlinSourceSet;
import dev.nokee.language.jvm.internal.JvmSourceSetExtensible;
import dev.nokee.model.internal.core.ModelActions;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.registry.ModelConfigurer;
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

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.bridgeSourceSet;
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
				sourceSetContainer(project).matching(nameOf(componentOf(sources))).all(registerJavaSourceSet(sources));
			})));
		});

		project.getPluginManager().withPlugin("groovy-base", appliedPlugin -> {
			modelConfigurer.configure(matching(discoveringInstanceOf(JvmSourceSetExtensible.class), ModelActions.once(sources -> {
				sourceSetContainer(project).matching(nameOf(componentOf(sources))).all(registerGroovySourceSet(sources));
			})));
		});

		project.getPluginManager().withPlugin("org.jetbrains.kotlin.jvm", appliedPlugin -> {
			modelConfigurer.configure(matching(discoveringInstanceOf(JvmSourceSetExtensible.class), ModelActions.once(sources -> {
				sourceSetContainer(project).matching(nameOf(componentOf(sources))).all(registerKotlinSourceSet(sources));
			})));
		});
	}

	private static SourceSetContainer sourceSetContainer(Project project) {
		return project.getExtensions().getByType(SourceSetContainer.class);
	}

	private static ModelNode componentOf(ModelNode sources) {
		// We assume it's a ComponentSources model node
		assert sources.canBeViewedAs(of(ComponentSources.class));
		assert ModelNodeUtils.getParent(sources).isPresent();
		assert ModelNodeUtils.getParent(sources).get().canBeViewedAs(of(Component.class));
		return ModelNodeUtils.getParent(sources).get();
	}

	private static Spec<? super SourceSet> nameOf(ModelNode component) {
		// We assume it's a Component model node
		assert component.canBeViewedAs(of(Component.class));
		String componentName = component.getPath().getName();
		return sourceSet -> sourceSet.getName().equals(componentName);
	}

	private static Action<SourceSet> registerJavaSourceSet(ModelNode sources) {
		return new Action<SourceSet>() {
			@Override
			public void execute(SourceSet sourceSet) {
				sources.register(bridgeSourceSet(asSourceDirectorySet(sourceSet), JavaSourceSet.class));
			}

			private SourceDirectorySet asSourceDirectorySet(SourceSet sourceSet) {
				return sourceSet.getJava();
			}
		};
	}

	private static Action<SourceSet> registerGroovySourceSet(ModelNode sources) {
		return new Action<SourceSet>() {
			@Override
			public void execute(SourceSet sourceSet) {
				sources.register(bridgeSourceSet(asSourceDirectorySet(sourceSet), GroovySourceSet.class));
			}

			private SourceDirectorySet asSourceDirectorySet(SourceSet sourceSet) {
				return ((org.gradle.api.tasks.GroovySourceSet) new DslObject(sourceSet).getConvention().getPlugins().get("groovy")).getGroovy();
			}
		};
	}

	private static Action<SourceSet> registerKotlinSourceSet(ModelNode sources) {
		return new Action<SourceSet>() {
			@Override
			public void execute(SourceSet sourceSet) {
				sources.register(bridgeSourceSet(asSourceDirectorySet(sourceSet), KotlinSourceSet.class));
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
}
