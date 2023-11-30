/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.platform.nativebase.internal.plugins;

import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.nativebase.internal.NativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.NativeLibraryComponent;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.model.internal.names.ElementName.ofMain;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;

public class NativePlatformPluginSupport<T extends Component> implements Action<Project> {
	private Class<? extends Plugin<Project>> languagePluginType;
	private Class<T> componentType;
	private Class<? extends Variant> variantType;
	private Action<? super T> configureAction;
	private String extensionName;

	public NativePlatformPluginSupport() {
	}

	// TODO: Language plugin should have base type
	public NativePlatformPluginSupport<T> useLanguagePlugin(Class<? extends Plugin<Project>> languagePluginType) {
		this.languagePluginType = languagePluginType;
		return this;
	}

	@SuppressWarnings("unchecked")
	public <U extends Component> NativePlatformPluginSupport<U> registerComponent(Class<U> componentType) {
		this.componentType = (Class<T>) componentType;
		return (NativePlatformPluginSupport<U>) this;
	}

	public <U extends Variant & VariantOf<T>> NativePlatformPluginSupport<T> registerVariant(Class<? extends U> variantType) {
		this.variantType = variantType;
		return this;
	}

	public NativePlatformPluginSupport<T> registerAsMainComponent(Action<? super T> configureAction) {
		this.configureAction = configureAction;
		return this;
	}

	public NativePlatformPluginSupport<T> mountAsExtension() {
		if (NativeApplicationComponent.class.isAssignableFrom(componentType)) {
			this.extensionName = "application";
		} else if (NativeLibraryComponent.class.isAssignableFrom(componentType)) {
			this.extensionName = "library";
		} else {
			throw new UnsupportedOperationException("not an application or library component");
		}
		return this;
	}

	public interface VariantOf<T extends Component> {}

	@Override
	public void execute(Project project) {
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(languagePluginType);

		model(project, factoryRegistryOf(Component.class)).registerFactory(componentType);
		model(project, factoryRegistryOf(Variant.class)).registerFactory(variantType);

		final NamedDomainObjectProvider<T> componentProvider = model(project, registryOf(Component.class)).register(ProjectIdentifier.of(project).child(ofMain()), componentType).asProvider();
		componentProvider.configure(configureAction);
		final T extension = componentProvider.get();

		project.getExtensions().add(extensionName, extension);
	}
}