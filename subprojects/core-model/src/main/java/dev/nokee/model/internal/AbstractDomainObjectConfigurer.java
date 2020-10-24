package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import lombok.val;
import org.gradle.api.Action;

import java.util.function.Consumer;

public abstract class AbstractDomainObjectConfigurer<T> implements DomainObjectConfigurer<T> {
	private final KnownDomainObjectActions<T> knownConfigureActions = new KnownDomainObjectActions<>();
	private final DomainObjectActions<T> configureActions = new DomainObjectActions<>();
	private final KnownDomainObjects<T> knownObjects;
	private final DomainObjects<T> objects;
	private final NamedDomainObjectConfigurer<T> nameAwareConfigurer;

	public AbstractDomainObjectConfigurer(Class<T> entityType, DomainObjectEventPublisher eventPublisher) {
		this.knownObjects = new KnownDomainObjects<>(entityType, eventPublisher, knownConfigureActions);
		this.objects = new DomainObjects<>(entityType, eventPublisher, configureActions);
		this.nameAwareConfigurer = new NamedDomainObjectConfigurer<>(entityType, knownObjects, this);
	}

	public <S extends T> void configureEach(DomainObjectIdentifier owner, Class<S> type, Action<? super S> action) {
		val filteringConfigureAction = DomainObjectActions.onlyIf(owner, action);
		Consumer<S> lookupConfigureAction = object -> {
			filteringConfigureAction.accept(objects.lookupIdentifier(object), object);
		};
		Consumer<T> configurationAction = DomainObjectActions.onlyIf(type, lookupConfigureAction);
		objects.forEach(configurationAction);
		configureActions.add(configurationAction);
	}

	public <S extends T> void configure(TypeAwareDomainObjectIdentifier<S> identifier, Action<? super S> action) {
		knownObjects.assertKnownObject(identifier);
		val object = objects.findByIdentifier(identifier);
		if (!object.isPresent()) {
			configureActions.add(futureObject -> {
				if (objects.lookupIdentifier(futureObject).equals(identifier)) {
					action.execute(identifier.getType().cast(futureObject));
				}
			});
		} else {
			action.execute(identifier.getType().cast(object.get()));
		}
	}

	public <S extends T> void configure(DomainObjectIdentifier owner, String name, Class<S> type, Action<? super S> action) {
		nameAwareConfigurer.configure(owner, name, type, action);
	}

	public <S extends T> void whenElementKnown(DomainObjectIdentifier owner, Class<S> type, Action<? super TypeAwareDomainObjectIdentifier<S>> action) {
		val knownAction = KnownDomainObjectActions.onlyIf(owner, type, action);
		knownObjects.forEach(knownAction);
		knownConfigureActions.add(knownAction);
	}
}
