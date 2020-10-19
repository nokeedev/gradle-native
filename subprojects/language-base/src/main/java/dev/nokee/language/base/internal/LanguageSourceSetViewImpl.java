package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectView;
import dev.nokee.model.internal.AbstractDomainObjectView;
import dev.nokee.model.internal.HasConfigureElementByNameSupport;
import dev.nokee.utils.ProviderUtils;
import dev.nokee.utils.TransformerUtils;
import org.gradle.api.Action;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.isDescendent;

public final class LanguageSourceSetViewImpl<T extends LanguageSourceSet> extends AbstractDomainObjectView<LanguageSourceSet, T> implements LanguageSourceSetViewInternal<T>, DomainObjectView<T>, HasConfigureElementByNameSupport {
	private final KnownLanguageSourceSetFactory knownLanguageSourceSetFactory;
	private final ConfigureDirectlyOwnedSourceSetByNameMethodInvoker methodInvoker;

	public LanguageSourceSetViewImpl(DomainObjectIdentifier viewOwner, Class<T> viewElementType, LanguageSourceSetRepository repository, LanguageSourceSetConfigurer configurer, LanguageSourceSetViewFactory viewFactory, KnownLanguageSourceSetFactory knownLanguageSourceSetFactory) {
		super(viewOwner, viewElementType, repository.filtered(id -> isDescendent(id, viewOwner) && viewElementType.isAssignableFrom(id.getType())).map(ProviderUtils.map(viewElementType::cast)).map(TransformerUtils.toSetTransformer()), configurer, viewFactory);
		this.methodInvoker = new ConfigureDirectlyOwnedSourceSetByNameMethodInvoker(repository);
		this.knownLanguageSourceSetFactory = knownLanguageSourceSetFactory;
	}

	@Override
	public <S extends T> LanguageSourceSetViewImpl<S> withType(Class<S> type) {
		return (LanguageSourceSetViewImpl<S>) super.withType(type);
	}

	@Override
	public void whenElementKnown(Action<? super KnownLanguageSourceSet<T>> action) {
		configurer.whenElementKnown(viewOwner, viewElementType, identifier -> action.execute(knownLanguageSourceSetFactory.create(identifier)));
	}

	@Override
	public <S extends T> void whenElementKnown(Class<S> type, Action<? super KnownLanguageSourceSet<S>> action) {
		configurer.whenElementKnown(viewOwner, type, identifier -> action.execute(knownLanguageSourceSetFactory.create(identifier)));
	}

	@Override
	public Object invokeMethod(String name, Object args) {
		return methodInvoker.invokeMethod(name, args);
	}
}
