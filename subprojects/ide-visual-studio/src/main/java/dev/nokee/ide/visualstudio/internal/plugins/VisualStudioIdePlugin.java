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
package dev.nokee.ide.visualstudio.internal.plugins;

import dev.nokee.ide.visualstudio.VisualStudioIdeProjectExtension;
import dev.nokee.ide.visualstudio.internal.rules.CreateNativeComponentVisualStudioIdeProject;
import dev.nokee.model.internal.ModelElementFactory;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public abstract class VisualStudioIdePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(VisualStudioIdeBasePlugin.class);

		project.getPlugins().withType(ComponentModelBasePlugin.class, mapComponentToVisualStudioIdeProjects(project, (VisualStudioIdeProjectExtension) project.getExtensions().getByName(VisualStudioIdeBasePlugin.VISUAL_STUDIO_EXTENSION_NAME)));
	}

	private Action<ComponentModelBasePlugin> mapComponentToVisualStudioIdeProjects(Project project, VisualStudioIdeProjectExtension extension) {
		return new Action<ComponentModelBasePlugin>() {
			@Override
			public void execute(ComponentModelBasePlugin appliedPlugin) {
				val modelConfigurer = project.getExtensions().getByType(ModelConfigurer.class);
				val action = new CreateNativeComponentVisualStudioIdeProject(extension, project.getLayout(), project.getObjects(), project.getProviders());
				modelConfigurer.configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IsComponent.class), ModelComponentReference.of(ModelElementFactory.class), (entity, tag, factory) -> {
					action.execute(factory.createElement(entity));
				})));
			}
		};
	}
}
