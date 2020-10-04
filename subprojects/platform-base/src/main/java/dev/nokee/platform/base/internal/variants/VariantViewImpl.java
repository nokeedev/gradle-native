package dev.nokee.platform.base.internal.variants;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.AbstractDomainObjectView;
import dev.nokee.platform.base.Variant;
import dev.nokee.utils.ProviderUtils;
import dev.nokee.utils.TransformerUtils;
import org.gradle.api.Action;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.isDescendent;

public final class VariantViewImpl<T extends Variant> extends AbstractDomainObjectView<Variant, T> implements VariantViewInternal<T> {
	private final KnownVariantFactory knownVariantFactory;

	VariantViewImpl(DomainObjectIdentifier viewOwner, Class<T> viewElementType, VariantRepository repository, VariantConfigurer configurer, VariantViewFactory viewFactory, KnownVariantFactory knownVariantFactory) {
		super(viewOwner, viewElementType, repository.filtered(id -> isDescendent(id, viewOwner) && viewElementType.isAssignableFrom(id.getType())).map(ProviderUtils.map(viewElementType::cast)).map(TransformerUtils.toSetTransformer()), configurer, viewFactory);
		this.knownVariantFactory = knownVariantFactory;
	}

	@Override
	public <S extends T> VariantViewInternal<S> withType(Class<S> type) {
		return (VariantViewInternal<S>) super.withType(type);
	}

	@Override
	public void whenElementKnown(Action<? super KnownVariant<T>> action) {
		configurer.whenElementKnown(viewOwner, viewElementType, identifier -> action.execute(knownVariantFactory.create(identifier)));
	}
}
