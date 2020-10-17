package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.internal.AbstractPolymorphicDomainObjectInstantiator;

public final class LanguageSourceSetInstantiatorImpl extends AbstractPolymorphicDomainObjectInstantiator<LanguageSourceSet> implements LanguageSourceSetInstantiator {
	public LanguageSourceSetInstantiatorImpl(String displayName) {
		super(LanguageSourceSet.class, displayName);
	}
}
