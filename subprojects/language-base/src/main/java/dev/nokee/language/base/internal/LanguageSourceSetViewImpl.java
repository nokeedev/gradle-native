package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectView;
import dev.nokee.model.internal.AbstractDomainObjectView;
import dev.nokee.model.internal.DomainObjectIdentifierUtils;
import dev.nokee.model.internal.HasConfigureElementByNameSupport;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import dev.nokee.utils.ProviderUtils;
import dev.nokee.utils.TransformerUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.UnknownDomainObjectException;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.*;

public final class LanguageSourceSetViewImpl<T extends LanguageSourceSet> extends AbstractDomainObjectView<LanguageSourceSet, T> implements LanguageSourceSetViewInternal<T>, DomainObjectView<T>, HasConfigureElementByNameSupport<T> {
	private final LanguageSourceSetRepository repository;
	private final KnownLanguageSourceSetFactory knownLanguageSourceSetFactory;
	private final ConfigureDirectlyOwnedSourceSetByNameMethodInvoker methodInvoker;
	private final Class<T> viewElementType;

	public LanguageSourceSetViewImpl(DomainObjectIdentifier viewOwner, Class<T> viewElementType, LanguageSourceSetRepository repository, LanguageSourceSetConfigurer configurer, LanguageSourceSetViewFactory viewFactory, KnownLanguageSourceSetFactory knownLanguageSourceSetFactory) {
		super(viewOwner, viewElementType, repository.filtered(id -> isDescendent(id, viewOwner) && viewElementType.isAssignableFrom(id.getType())).map(ProviderUtils.map(viewElementType::cast)).map(TransformerUtils.toSetTransformer()), configurer, viewFactory);
		this.viewElementType = viewElementType;
		this.methodInvoker = new ConfigureDirectlyOwnedSourceSetByNameMethodInvoker(this);
		this.repository = repository;
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

	@SuppressWarnings("unchecked")
	private TypeAwareDomainObjectIdentifier<T> getIdentifierByName(String name) {
		return (TypeAwareDomainObjectIdentifier<T>) repository.findKnownIdentifier(directlyOwnedBy(viewOwner).and(named(name)).and(DomainObjectIdentifierUtils.withType(viewElementType))).orElseThrow(() -> createNotFoundException(name));
	}

	public void configure(String name, Action<? super T> action) {
		configurer.configure(getIdentifierByName(name), action);
	}

	public <S extends T> void configure(String name, Class<S> type, Action<? super S> action) {
		val identifier = getIdentifierByName(name);
		if (!type.isAssignableFrom(identifier.getType())) {
			throw createWrongTypeException(name, type, identifier.getType());
		}
		configurer.configure((TypeAwareDomainObjectIdentifier<S>)identifier, action);
	}

	protected UnknownDomainObjectException createNotFoundException(String name) {
		return new UnknownDomainObjectException(String.format("%s with name '%s' not found.", getTypeDisplayName(), name));
	}

	protected InvalidUserDataException createWrongTypeException(String name, Class expected, Class actual) {
		return new InvalidUserDataException(String.format("The domain object '%s' (%s) is not a subclass of the given type (%s).", name, actual.getCanonicalName(), expected.getCanonicalName()));
	}

	protected String getTypeDisplayName() {
		return viewElementType.getSimpleName();
	}
}
