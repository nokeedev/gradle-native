package dev.nokee.platform.base.internal.dependencies;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.model.internal.KnownDomainObjects;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.DependencyBucketName;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ConfigurationContainer;

import java.util.Optional;

@AutoFactory(className = "ComponentDependenciesContainerFactoryImpl", implementing = {ComponentDependenciesContainerFactory.class})
public final class ComponentDependenciesContainerImpl implements ComponentDependenciesContainer {
	private final DomainObjectIdentifierInternal containerIdentifier;
	private final ConfigurationContainer configurations;
	private final DependencyBucketInstantiator instantiator;
	private final DependencyBucketCreationGuard guard = new DependencyBucketCreationGuard();
	private final KnownDomainObjects<DependencyIdentifier<?>, DependencyBucket> knownObjects = new KnownDomainObjects<>();

	public ComponentDependenciesContainerImpl(DomainObjectIdentifier containerIdentifier, @Provided ConfigurationContainer configurations, @Provided DependencyBucketInstantiator instantiator) {
		this.containerIdentifier = (DomainObjectIdentifierInternal)containerIdentifier;
		this.configurations = configurations;
		this.instantiator = instantiator;
	}

	@Override
	public <T extends DependencyBucket> T register(DependencyBucketName name, Class<T> type) {
		val identifier = DependencyIdentifier.of(name, type, containerIdentifier);
		return knownObjects.withKnownIdentifier(identifier, id1 -> guard.guard(identifier, id2 -> instantiator.newInstance(identifier, type)));
	}

	@Override
	public <T extends DependencyBucket> T register(DependencyBucketName name, Class<T> type, Action<? super T> action) {
		val identifier = DependencyIdentifier.of(name, type, containerIdentifier);
		T bucket = knownObjects.withKnownIdentifier(identifier, id1 -> guard.guard(identifier, id2 -> instantiator.newInstance(identifier, type)));
		action.execute(bucket);
		return bucket;
	}

	private <T extends DependencyBucket> T newInstance(DomainObjectIdentifier knownIdentifier) {
		@SuppressWarnings("unchecked")
		val identifier = (DependencyIdentifier<T>) knownIdentifier;
		return guard.guard(identifier, id -> instantiator.newInstance(identifier, identifier.getType()));
	}

	@Override
	public void configureEach(Action<? super DependencyBucket> action) {
		configurations.configureEach(guard.mapToDependencyBucket(action));
//		configurations.configureEach(knowIdentifiers.onlyKnownIdentifier(guard.mapToDependencyBucket(action)));
	}

	@Override
	public Optional<DependencyBucket> findByName(DependencyBucketName name) {
		return knownObjects.findByIdentifier(it -> it.getName().equals(name));
	}
}
