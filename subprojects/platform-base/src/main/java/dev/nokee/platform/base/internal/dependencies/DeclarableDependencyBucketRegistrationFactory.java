package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactory;
import dev.nokee.utils.ActionUtils;
import org.gradle.api.artifacts.Configuration;

import static dev.nokee.model.internal.core.ModelActions.initialize;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Bucket.ofDeclarable;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Subject.ofName;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.asDeclarable;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.description;

public final class DeclarableDependencyBucketRegistrationFactory implements NodeRegistrationFactory<DeclarableDependencyBucket> {
	private final ProjectConfigurationRegistry configurationRegistry;
	private final ConfigurationNamingScheme namingScheme;
	private final ConfigurationDescriptionScheme descriptionScheme;

	public DeclarableDependencyBucketRegistrationFactory(ProjectConfigurationRegistry configurationRegistry, ConfigurationNamingScheme namingScheme, ConfigurationDescriptionScheme descriptionScheme) {
		this.configurationRegistry = configurationRegistry;
		this.namingScheme = namingScheme;
		this.descriptionScheme = descriptionScheme;
	}

	@Override
	public NodeRegistration<DeclarableDependencyBucket> create(String name) {
		return NodeRegistration.of(name, of(DeclarableDependencyBucket.class))
			.action(self(initialize(context -> {
				context.withProjection(managed(of(DependencyBucketProjection.class), name));
				context.withProjection(ofInstance(configurationRegistry.createIfAbsent(nameOf(name),
					asDeclarable().andThen(descriptionOf(name)))));
			})));
	}

	private String nameOf(String name) {
		return namingScheme.configurationName(name);
	}

	private ActionUtils.Action<Configuration> descriptionOf(String name) {
		return description(descriptionScheme.description(ofName(name), ofDeclarable()));
	}
}
