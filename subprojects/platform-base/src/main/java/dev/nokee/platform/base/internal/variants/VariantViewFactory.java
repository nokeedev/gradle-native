package dev.nokee.platform.base.internal.variants;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.AbstractDomainObjectViewFactory;
import dev.nokee.platform.base.Variant;

import static java.util.Objects.requireNonNull;

public final class VariantViewFactory extends AbstractDomainObjectViewFactory<Variant> {
	private final VariantRepository repository;
	private final VariantConfigurer configurer;
	private final KnownVariantFactory knownObjectFactory;

	public VariantViewFactory(VariantRepository repository, VariantConfigurer configurer, KnownVariantFactory knownObjectFactory) {
		super(Variant.class);
		this.repository = repository;
		this.configurer = configurer;
		this.knownObjectFactory = knownObjectFactory;
	}

	@Override
	public <S extends Variant> VariantViewImpl<S> create(DomainObjectIdentifier viewOwner, Class<S> elementType) {
		return new VariantViewImpl<>(requireNonNull(viewOwner), elementType, repository, configurer, this, knownObjectFactory);
	}
}
