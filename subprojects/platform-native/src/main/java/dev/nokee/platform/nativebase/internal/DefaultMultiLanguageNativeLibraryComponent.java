package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.internal.LanguageSourceSetRepository;
import dev.nokee.language.base.internal.LanguageSourceSetViewFactory;
import dev.nokee.language.nativebase.internal.HasNativeLanguageSupport;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

public final class DefaultMultiLanguageNativeLibraryComponent extends DefaultNativeLibraryComponent implements HasNativeLanguageSupport {
	public DefaultMultiLanguageNativeLibraryComponent(ComponentIdentifier<?> identifier, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations, DependencyHandler dependencyHandler, DomainObjectEventPublisher eventPublisher, VariantViewFactory viewFactory, VariantRepository variantRepository, BinaryViewFactory binaryViewFactory, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory, LanguageSourceSetRepository languageSourceSetRepository, LanguageSourceSetViewFactory languageSourceSetViewFactory) {
		super(identifier, objects, providers, tasks, layout, configurations, dependencyHandler, eventPublisher, viewFactory, variantRepository, binaryViewFactory, taskRegistry, taskViewFactory, languageSourceSetRepository, languageSourceSetViewFactory);
	}
}
