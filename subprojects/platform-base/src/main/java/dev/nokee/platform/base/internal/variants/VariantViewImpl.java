package dev.nokee.platform.base.internal.variants;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectView;
import dev.nokee.model.internal.AbstractDomainObjectView;
import dev.nokee.platform.base.Variant;
import dev.nokee.utils.ProviderUtils;
import dev.nokee.utils.TransformerUtils;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.specs.Spec;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.isDescendent;

public final class VariantViewImpl<T extends Variant> extends AbstractDomainObjectView<Variant, T> implements VariantViewInternal<T>, DomainObjectView<T> {
	private final KnownVariantFactory knownVariantFactory;

	VariantViewImpl(DomainObjectIdentifier viewOwner, Class<T> viewElementType, VariantRepository repository, VariantConfigurer configurer, VariantViewFactory viewFactory, KnownVariantFactory knownVariantFactory) {
		super(viewOwner, viewElementType, repository.filtered(id -> isDescendent(id, viewOwner) && viewElementType.isAssignableFrom(id.getType())).map(ProviderUtils.map(viewElementType::cast)).map(TransformerUtils.toSetTransformer()), configurer, viewFactory);
		this.knownVariantFactory = knownVariantFactory;
	}

	@Override
	public void configureEach(Closure<Void> closure) {
		DomainObjectView.super.configureEach(closure);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Closure<Void> closure) {
		DomainObjectView.super.configureEach(type, closure);
	}

	@Override
	public void configureEach(Spec<? super T> spec, Closure<Void> closure) {
		DomainObjectView.super.configureEach(spec, closure);
	}

	@Override
	public <S extends T> VariantViewImpl<S> withType(Class<S> type) {
		return (VariantViewImpl<S>) super.withType(type);
	}

	@Override
	public void whenElementKnown(Action<? super KnownVariant<T>> action) {
		configurer.whenElementKnown(viewOwner, viewElementType, identifier -> action.execute(knownVariantFactory.create(identifier)));
	}

	@Override
	public <S extends T> void whenElementKnown(Class<S> type, Action<? super KnownVariant<S>> action) {
		configurer.whenElementKnown(viewOwner, type, identifier -> action.execute(knownVariantFactory.create(identifier)));
	}
}
