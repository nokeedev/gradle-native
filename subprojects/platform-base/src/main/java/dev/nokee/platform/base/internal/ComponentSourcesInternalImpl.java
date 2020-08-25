package dev.nokee.platform.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.LanguageSourceSetInstantiator;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.DomainObjectCollection;
import dev.nokee.platform.base.DomainObjectElement;
import dev.nokee.platform.base.SourceView;
import lombok.val;
import org.gradle.api.Action;

import javax.inject.Inject;

import static dev.nokee.model.DomainObjectIdentifier.named;

class ComponentSourcesInternalImpl implements ComponentSourcesInternal {
	private final LanguageSourceSetInstantiator sourceSetInstantiator;
	private final SourceViewFactory viewFactory;
	private final DomainObjectCollection<LanguageSourceSet> collection;

	@Inject
	public ComponentSourcesInternalImpl(LanguageSourceSetInstantiator sourceSetInstantiator, SourceViewFactory viewFactory, DomainObjectCollectionFactory domainObjectCollectionFactory) {
		this.sourceSetInstantiator = sourceSetInstantiator;
		this.viewFactory = viewFactory;
		this.collection = domainObjectCollectionFactory.create(LanguageSourceSet.class);
	}

	@Override
	public <U extends LanguageSourceSet> U register(String name, Class<U> type) {
		// TODO Use identifier to derive the sourceset identifier
		val result = sourceSetInstantiator.create(named(name), type);
		collection.add(DomainObjectElement.of(type, result));
		return result;
	}

	public <U extends LanguageSourceSet> U register(String name, Class<U> type, Action<? super U> action) {
		// TODO Use identifier to derive the sourceset identifier
		val result = sourceSetInstantiator.create(named(name), type);
		action.execute(result);
		collection.add(DomainObjectElement.of(type, result));
		return result;
	}

	public SourceView<LanguageSourceSet> getAsView() {
		return viewFactory.create(collection.getAsView());
	}

	@Override
	public ComponentSourcesInternal disallowChanges() {
		collection.disallowChanges();
		return this;
	}
}
