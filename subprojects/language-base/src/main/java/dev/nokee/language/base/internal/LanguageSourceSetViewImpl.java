package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.LanguageSourceSetView;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectView;
import dev.nokee.model.internal.AbstractDomainObjectView;
import dev.nokee.utils.ProviderUtils;
import dev.nokee.utils.TransformerUtils;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.isDescendent;

public final class LanguageSourceSetViewImpl<T extends LanguageSourceSet> extends AbstractDomainObjectView<LanguageSourceSet, T> implements LanguageSourceSetView<T>, DomainObjectView<T> {
	public LanguageSourceSetViewImpl(DomainObjectIdentifier viewOwner, Class<T> viewElementType, LanguageSourceSetRepository repository, LanguageSourceSetConfigurer configurer, LanguageSourceSetViewFactory viewFactory) {
		super(viewOwner, viewElementType, repository.filtered(id -> isDescendent(id, viewOwner) && viewElementType.isAssignableFrom(id.getType())).map(ProviderUtils.map(viewElementType::cast)).map(TransformerUtils.toSetTransformer()), configurer, viewFactory);
	}
}
