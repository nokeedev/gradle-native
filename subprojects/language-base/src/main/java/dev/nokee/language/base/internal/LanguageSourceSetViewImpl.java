package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectView;
import dev.nokee.model.internal.AbstractDomainObjectView;
import dev.nokee.model.internal.HasConfigureElementByNameSupport;
import org.gradle.api.Action;

public final class LanguageSourceSetViewImpl<T extends LanguageSourceSet> extends AbstractDomainObjectView<LanguageSourceSet, T> implements LanguageSourceSetViewInternal<T>, DomainObjectView<T>, HasConfigureElementByNameSupport<T> {
	private final KnownLanguageSourceSetFactory knownLanguageSourceSetFactory;
	private final ConfigureDirectlyOwnedSourceSetByNameMethodInvoker methodInvoker;
	private final Class<T> viewElementType;

	public LanguageSourceSetViewImpl(DomainObjectIdentifier viewOwner, Class<T> viewElementType, LanguageSourceSetRepository repository, LanguageSourceSetConfigurer configurer, LanguageSourceSetViewFactory viewFactory, KnownLanguageSourceSetFactory knownLanguageSourceSetFactory) {
		super(viewOwner, viewElementType, repository, configurer, viewFactory);
		this.viewElementType = viewElementType;
		this.methodInvoker = new ConfigureDirectlyOwnedSourceSetByNameMethodInvoker(this);
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

	//region configure by name/type
	@Override
	public Object invokeMethod(String name, Object args) {
		return methodInvoker.invokeMethod(name, args);
	}

	public void configure(String name, Action<? super T> action) {
		configurer.configure(viewOwner, name, viewElementType, action);
	}

	public <S extends T> void configure(String name, Class<S> type, Action<? super S> action) {
		configurer.configure(viewOwner, name, type, action);
	}
	//endregion
}
