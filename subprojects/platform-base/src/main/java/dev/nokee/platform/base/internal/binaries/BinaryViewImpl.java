package dev.nokee.platform.base.internal.binaries;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectView;
import dev.nokee.model.internal.AbstractDomainObjectView;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.utils.ProviderUtils;
import dev.nokee.utils.TransformerUtils;
import groovy.lang.Closure;
import org.gradle.api.specs.Spec;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.isDescendent;

public final class BinaryViewImpl<T extends Binary> extends AbstractDomainObjectView<Binary, T> implements BinaryView<T>, DomainObjectView<T> {
	public BinaryViewImpl(DomainObjectIdentifier viewOwner, Class<T> viewElementType, BinaryRepository binaryRepository, BinaryConfigurer configurer, BinaryViewFactory binaryViewFactory) {
		super(viewOwner, viewElementType, binaryRepository.filtered(id -> isDescendent(id, viewOwner) && viewElementType.isAssignableFrom(id.getType())).map(ProviderUtils.map(viewElementType::cast)).map(TransformerUtils.toSetTransformer()), configurer, binaryViewFactory);
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
	public <S extends T> BinaryViewImpl<S> withType(Class<S> type) {
		return (BinaryViewImpl<S>) super.withType(type);
	}
}
