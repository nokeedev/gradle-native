/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.DependencyFactory;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.DeclarableDependencyBucket;
import dev.nokee.utils.ActionUtils;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.FileCollectionDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderConvertible;

import javax.inject.Inject;
import java.util.Collection;
import java.util.function.BiConsumer;

import static dev.nokee.utils.ProviderUtils.finalizeValue;
import static dev.nokee.utils.TransformerUtils.peek;

// Note 1: We can't realistically delay until realize because Kotlin plugin suck big time and Gradle removed important APIs... Too bad, blame Gradle or Kotlin.
public /*final*/ abstract class DeclarableDependencyBucketSpec extends ModelElementSupport implements DeclarableDependencyBucket
	, DependencyBucketMixIn
{
	private final NamedDomainObjectProvider<Configuration> delegate;
	private final DependencyFactory dependencyFactory;

	@Inject
	public DeclarableDependencyBucketSpec(DependencyHandler handler, ModelObjectRegistry<Configuration> configurationRegistry) {
		this.delegate = configurationRegistry.register(getIdentifier(), Configuration.class).asProvider();
		getExtensions().add("$configuration", delegate.get());

		this.dependencyFactory = DependencyFactory.forHandler(handler);
	}

	@Override
	public void addDependency(Dependency dependency) {
		defaultAction().execute(dependency);
		delegate.configure(dependencies(add(dependency)));
	}

	@Override
	public <DependencyType extends Dependency> void addDependency(DependencyType dependency, Action<? super DependencyType> configureAction) {
		defaultAction().execute(dependency);
		configureAction.execute(dependency);
		delegate.configure(dependencies(add(dependency)));
	}

	@Override
	public <DependencyType extends Dependency> void addDependency(Provider<DependencyType> dependencyProvider) {
		// See Note 1.
		delegate.configure(dependencies(add(dependencyProvider.map(peek(defaultAction()::execute)))));
	}

	@Override
	public <DependencyType extends Dependency> void addDependency(Provider<DependencyType> dependencyProvider, Action<? super DependencyType> configureAction) {
		// See Note 1.
		delegate.configure(dependencies(add(dependencyProvider.map(peek(defaultAction()::execute)).map(peek(configureAction::execute)))));
	}

	@Override
	public void addDependency(FileCollection fileCollection) {
		addDependency(dependencyFactory.create(fileCollection));
	}

	@Override
	public void addDependency(Project project) {
		addDependency(dependencyFactory.create(project));
	}

	@Override
	public void addDependency(CharSequence dependencyNotation) {
		addDependency(dependencyFactory.create(dependencyNotation));
	}

	@Override
	public void addDependency(CharSequence dependencyNotation, Action<? super ExternalModuleDependency> configureAction) {
		addDependency(dependencyFactory.create(dependencyNotation), configureAction);
	}

	@Override
	public void addDependency(FileCollection fileCollection, Action<? super FileCollectionDependency> configureAction) {
		addDependency(dependencyFactory.create(fileCollection), configureAction);
	}

	@Override
	public void addDependency(ProviderConvertible<? extends Dependency> dependencyProvider) {
		addDependency(dependencyProvider.asProvider());
	}

	@Override
	public <DependencyType extends Dependency> void addDependency(ProviderConvertible<DependencyType> dependencyProvider, Action<? super DependencyType> configureAction) {
		addDependency(dependencyProvider.asProvider(), configureAction);
	}

	private ActionUtils.Action<Dependency> defaultAction() {
		final Action<ModuleDependency> action = finalizeValue(getDefaultDependencyAction()).map(ActionUtils.Action::of).getOrElse(ActionUtils.doNothing());
		return dependency -> {
			if (dependency instanceof ModuleDependency) {
				action.execute((ModuleDependency) dependency);
			} else {
				// ignores
			}
		};
	}

	private static Action<Configuration> dependencies(BiConsumer<? super Configuration, ? super DependencySet> configureAction) {
		return configuration -> configureAction.accept(configuration, configuration.getDependencies());
	}

	private static <SELF, ElementType> BiConsumer<SELF, Collection<ElementType>> add(ElementType element) {
		return (self, collection) -> collection.add(element);
	}

	private static <SELF, ElementType> BiConsumer<SELF, DomainObjectCollection<ElementType>> add(Provider<ElementType> elementProvider) {
		return (self, collection) -> collection.addLater(elementProvider);
	}

	@Override
	protected String getTypeName() {
		return "declarable dependency bucket";
	}
}
