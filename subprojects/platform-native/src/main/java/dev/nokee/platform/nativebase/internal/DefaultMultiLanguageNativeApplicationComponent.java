package dev.nokee.platform.nativebase.internal;

import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

public final class DefaultMultiLanguageNativeApplicationComponent extends DefaultNativeApplicationComponent {
	public DefaultMultiLanguageNativeApplicationComponent(ComponentIdentifier<?> identifier, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ConfigurationContainer configurations, DependencyHandler dependencyHandler, DomainObjectEventPublisher eventPublisher, VariantViewFactory viewFactory, VariantRepository variantRepository, BinaryViewFactory binaryViewFactory, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory, ModelLookup modelLookup) {
		super(identifier, objects, providers, tasks, configurations, dependencyHandler, eventPublisher, viewFactory, variantRepository, binaryViewFactory, taskRegistry, taskViewFactory, modelLookup);
	}
}
