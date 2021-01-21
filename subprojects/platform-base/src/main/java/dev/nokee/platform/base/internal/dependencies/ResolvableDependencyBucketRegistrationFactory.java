package dev.nokee.platform.base.internal.dependencies;

import com.google.common.annotations.VisibleForTesting;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactory;
import lombok.val;
import org.gradle.api.artifacts.ArtifactView;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;

import javax.inject.Inject;
import java.util.function.Consumer;

import static dev.nokee.model.internal.core.ModelActions.initialize;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Bucket.ofResolvable;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Subject.ofName;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.*;

public final class ResolvableDependencyBucketRegistrationFactory implements NodeRegistrationFactory<ResolvableDependencyBucket> {
	private final ProjectConfigurationRegistry configurationRegistry;
	private final ConfigurationNamingScheme namingScheme;
	private final ConfigurationDescriptionScheme descriptionScheme;

	@VisibleForTesting
	ResolvableDependencyBucketRegistrationFactory(ProjectConfigurationRegistry configurationRegistry) {
		this(configurationRegistry, ConfigurationNamingScheme.identity(), ConfigurationDescriptionScheme.forThisProject());
	}

	public ResolvableDependencyBucketRegistrationFactory(ProjectConfigurationRegistry configurationRegistry, ConfigurationNamingScheme namingScheme, ConfigurationDescriptionScheme descriptionScheme) {
		this.configurationRegistry = configurationRegistry;
		this.namingScheme = namingScheme;
		this.descriptionScheme = descriptionScheme;
	}

	@Override
	public NodeRegistration<ResolvableDependencyBucket> create(String name) {
		return NodeRegistration.of(name, of(ResolvableDependencyBucket.class))
			.action(self(initialize(context -> {
				context.withProjection(managed(of(IncomingArtifacts.class)));
				context.withProjection(managed(of(DependencyBucketProjection.class), name));
				context.withProjection(ofInstance(configurationRegistry.createIfAbsent(nameOf(name),
					asResolvable()
						.andThen(descriptionOf(name))
						.andThen(realizeNodeBeforeResolve()))));
			})));
	}

	private String nameOf(String name) {
		return namingScheme.configurationName(name);
	}

	private Consumer<Configuration> descriptionOf(String name) {
		return description(descriptionScheme.description(ofName(name), ofResolvable()));
	}

	private static Consumer<Configuration> realizeNodeBeforeResolve() {
		val node = ModelNodeContext.getCurrentModelNode();
		return configuration -> {
			((ConfigurationInternal) configuration).beforeLocking(it -> node.realize());
		};
	}

	static class IncomingArtifacts {
		private final Configuration delegate = ModelNodeContext.getCurrentModelNode().get(Configuration.class);

		@Inject
		public IncomingArtifacts() {}

		public FileCollection get() {
			return delegate.getIncoming().getFiles();
		}

		public FileCollection getAsLenient() {
			return delegate.getIncoming().artifactView(this::asLenient).getFiles();
		}

		private void asLenient(ArtifactView.ViewConfiguration view) {
			view.setLenient(true);
		}
	}
}
