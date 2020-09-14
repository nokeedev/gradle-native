package dev.nokee.platform.base.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.platform.base.DomainObjectCollection;
import dev.nokee.platform.base.DomainObjectElement;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.utils.Cast;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Spec;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

public class VariantCollection<T extends Variant> implements Realizable {
	private final DomainObjectCollection<T> delegate;
	private final Class<T> elementType;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	private boolean disallowChanges = false;

	// TODO: Make the distinction between public and implementation type
	@Inject
	public VariantCollection(Class<T> elementType, ProviderFactory providers, ObjectFactory objects) {
		this.elementType = elementType;
		this.objects = objects;
		delegate = new DefaultDomainObjectCollection<>(elementType, getObjects(), providers);
	}

	public VariantProvider<T> registerVariant(BuildVariantInternal buildVariant, VariantFactory<T> factory) {
		val identifier = VariantIdentifier.builder().withComponentIdentifier(ComponentIdentifier.ofMain(Component.class, ProjectIdentifier.of("root"))).withUnambiguousNameFromBuildVariant(buildVariant).withType(elementType).build();
		delegate.add(new DomainObjectElement<T>() {
			@Override
			public T get() {
				return factory.create(identifier.getUnambiguousName(), buildVariant);
			}

			@Override
			public Class<T> getType() {
				return elementType;
			}

			@Override
			public DomainObjectIdentifier getIdentifier() {
				return identifier;
			}
		});

		return Cast.uncheckedCastBecauseOfTypeErasure(getObjects().newInstance(VariantProvider.class, buildVariant, delegate.get(identifier)));
	}

	// TODO: I don't like that we have to pass in the viewElementType
	public <S extends Variant> VariantView<S> getAsView(Class<S> viewElementType) {
		Preconditions.checkArgument(viewElementType.isAssignableFrom(elementType), "element type of the view needs to be the same type or a supertype of the element of this collection");
		return getObjects().newInstance(DefaultVariantView.class, delegate.withType(Cast.uncheckedCast("", viewElementType)));
	}

	public Provider<List<? extends T>> filter(Spec<? super T> spec) {
		return delegate.filter(spec);
	}

	public void realize() {
		if (!disallowChanges) {
			throw new IllegalStateException("Please disallow changes before realizing the variants.");
		}
		delegate.getElements().get(); // Crappy way to realize
	}

	public VariantCollection<T> disallowChanges() {
		disallowChanges = true;
		delegate.disallowChanges();
		return this;
	}

	// TODO: This may not be needed, the only place it's used should probably use public API
	public Set<T> get() {
		realize();
		return ImmutableSet.copyOf(delegate.getElements().get());
	}

	public void whenElementKnown(Action<KnownVariant<? extends T>> action) {
		delegate.whenElementKnown(it -> {
			action.execute(getObjects().newInstance(KnownVariant.class, it));
		});
	}
}
