package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactory;
import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;

import java.util.function.Consumer;

import static dev.nokee.model.internal.core.ModelActions.initialize;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Bucket.ofConsumable;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Subject.ofName;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationUtils.*;

public final class ConsumableDependencyBucketRegistrationFactory implements NodeRegistrationFactory<ConsumableDependencyBucket> {
	private final ProjectConfigurationRegistry configurationRegistry;
	private final ConfigurationNamingScheme namingScheme;
	private final ConfigurationDescriptionScheme descriptionScheme;

	public ConsumableDependencyBucketRegistrationFactory(ProjectConfigurationRegistry configurationRegistry, ConfigurationNamingScheme namingScheme, ConfigurationDescriptionScheme descriptionScheme) {
		this.configurationRegistry = configurationRegistry;
		this.namingScheme = namingScheme;
		this.descriptionScheme = descriptionScheme;
	}

	@Override
	public NodeRegistration<ConsumableDependencyBucket> create(String name) {
		return NodeRegistration.of(name, of(ConsumableDependencyBucket.class))
			.action(self(initialize(context -> {
				context.withProjection(managed(of(DependencyBucketProjection.class), name));
				val outgoingArtifacts = context.withProjection(managed(of(OutgoingArtifacts.class)));

				context.withProjection(ofInstance(configurationRegistry.createIfAbsent(nameOf(name),
					asConsumable().andThen(attachOutgoingArtifactToConfiguration(outgoingArtifacts)).andThen(descriptionOf(name)))));
			})));
	}

	private String nameOf(String name) {
		return namingScheme.configurationName(name);
	}

	private Consumer<Configuration> descriptionOf(String name) {
		return description(descriptionScheme.description(ofName(name), ofConsumable()));
	}

	interface OutgoingArtifacts {
		ListProperty<PublishArtifact> getArtifacts();
	}

	private static Consumer<Configuration> attachOutgoingArtifactToConfiguration(KnownDomainObject<? extends OutgoingArtifacts> provider) {
		return withObjectFactory((configuration, objectFactory) -> {
			configuration.getOutgoing().getArtifacts().addAllLater(asCollectionProvider(objectFactory, provider.flatMap(OutgoingArtifacts::getArtifacts)));
		});
	}

	private static <T> Provider<? extends Iterable<T>> asCollectionProvider(ObjectFactory objectFactory, Provider<? extends Iterable<T>> provider) {
		val result = (ListProperty<T>) objectFactory.listProperty(Object.class);
		result.addAll(provider);
		return result;
	}
}
