package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.LanguageSourceSetFactory;
import dev.nokee.language.base.LanguageSourceSetFactoryRegistry;
import dev.nokee.language.base.LanguageSourceSetInstantiator;
import dev.nokee.model.DomainObjectIdentifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class LanguageSourceSetInstantiatorImpl implements LanguageSourceSetInstantiator, LanguageSourceSetFactoryRegistry {
	private final Map<Class<? extends LanguageSourceSet>, LanguageSourceSetFactory<? extends LanguageSourceSet>> factories = new HashMap<>();

	@Override
	public <U extends LanguageSourceSet> void registerFactory(Class<U> type, LanguageSourceSetFactory<? extends U> factory) {
		if (!LanguageSourceSet.class.isAssignableFrom(type)) {
			String message = String.format("Cannot register a factory for type %s because it is not a subtype of container element type %s.", type.getSimpleName(), LanguageSourceSet.class.getSimpleName());
			throw new IllegalArgumentException(message);
		}
		if (factories.containsKey(type)){
			throw new IllegalArgumentException(String.format("Cannot register a factory for type %s because a factory for this type is already registered.", type.getSimpleName()));
		}
		// TODO: Make sure there isn't a factory already registered
		factories.put(type, factory);
	}

	@Override
	public <S extends LanguageSourceSet> S create(DomainObjectIdentifier identifier, Class<S> type) {
		return type.cast(factories.get(type).create(identifier));
	}

	@Override
	public Set<Class<? extends LanguageSourceSet>> getCreatableTypes() {
		return factories.keySet();
	}

//	public <T extends LanguageSourceSet> void registerFactory(Class<T> languageType, LanguageSourceSetFactory<T> factory) {
		// TODO: Make sure there isn't a factory already registered
//		factories.put(languageType, factory);
//	}
}
