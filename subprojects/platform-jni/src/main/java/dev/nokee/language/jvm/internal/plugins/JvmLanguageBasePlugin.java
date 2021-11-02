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

import dev.nokee.language.base.internal.LanguageSourceSetRegistrationFactory;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.jvm.internal.GroovySourceSetRegistrationFactory;
import dev.nokee.language.jvm.internal.JavaSourceSetRegistrationFactory;
import dev.nokee.language.jvm.internal.JvmCompileTaskRegistrationActionFactory;
import dev.nokee.language.jvm.internal.KotlinSourceSetRegistrationFactory;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.TaskRegistrationFactory;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.SourceSetContainer;

public class JvmLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		project.getPlugins().withType(JavaBasePlugin.class, ignored -> {
			val compileTaskRegistrationFactory = new JvmCompileTaskRegistrationActionFactory(
				() -> project.getExtensions().getByType(ModelRegistry.class),
				() -> project.getExtensions().getByType(TaskRegistrationFactory.class),
				() -> project.getExtensions().getByType(ModelPropertyRegistrationFactory.class)
			);
			project.getExtensions().add("__nokee_javaSourceSetFactory", new JavaSourceSetRegistrationFactory(
				NamedDomainObjectRegistry.of(project.getExtensions().getByType(SourceSetContainer.class)),
				project.getExtensions().getByType(LanguageSourceSetRegistrationFactory.class),
				compileTaskRegistrationFactory
			));
			project.getExtensions().add("__nokee_groovySourceSetFactory", new GroovySourceSetRegistrationFactory(
				NamedDomainObjectRegistry.of(project.getExtensions().getByType(SourceSetContainer.class)),
				project.getExtensions().getByType(LanguageSourceSetRegistrationFactory.class),
				compileTaskRegistrationFactory
			));
			project.getExtensions().add("__nokee_kotlinSourceSetFactory", new KotlinSourceSetRegistrationFactory(
				NamedDomainObjectRegistry.of(project.getExtensions().getByType(SourceSetContainer.class)),
				project.getExtensions().getByType(LanguageSourceSetRegistrationFactory.class)
			));
		});
	}
}
