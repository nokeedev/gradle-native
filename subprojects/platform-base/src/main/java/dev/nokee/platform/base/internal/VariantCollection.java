package dev.nokee.platform.base.internal;

import com.google.common.base.Preconditions;
import dev.nokee.internal.Cast;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.runtime.base.internal.Dimension;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class VariantCollection<T extends Variant> implements Realizable {
	private final Map<String, VariantCreationArguments<T>> variantCreationArguments = new HashMap<>();
	private final Class<T> elementType;
	private final NamedDomainObjectContainer<T> collection;
	private boolean disallowChanges = false;
	private final DomainObjectSet<KnownVariant<T>> knownVariants = Cast.uncheckedCast("of type erasure", getObjects().domainObjectSet(KnownVariant.class));

	@Inject
	protected abstract ObjectFactory getObjects();

	// TODO: Make the distinction between public and implementation type
	@Inject
	public VariantCollection(Class<T> elementType) {
		Preconditions.checkArgument(Named.class.isAssignableFrom(elementType), "element type of the collection needs to implement Named");
		this.elementType = elementType;
		this.collection = getObjects().domainObjectContainer(elementType, this::create);
	}

	private T create(String name) {
		VariantCreationArguments<T> args = variantCreationArguments.remove(name);
		T result = args.factory.create(name, args.buildVariant);
		return result;
	}

	public VariantProvider<T> registerVariant(BuildVariant buildVariant, VariantFactory<T> factory) {
		if (disallowChanges) {
			throw new IllegalStateException("The value cannot be changed any further.");
		}
		String variantName = StringUtils.uncapitalize(buildVariant.getDimensions().stream().map(this::determineName).map(StringUtils::capitalize).collect(Collectors.joining()));
		variantCreationArguments.put(variantName, new VariantCreationArguments<T>(buildVariant, factory));
		return Cast.uncheckedCast("of type erasure", getObjects().newInstance(VariantProvider.class, buildVariant, elementType, collection.register(variantName)));
	}

	private String determineName(Dimension dimension) {
		if (dimension instanceof Named) {
			return ((Named) dimension).getName();
		}
		throw new IllegalArgumentException("Can't determine name");
	}

	// TODO: I don't like that we have to pass in the viewElementType
	public <S extends Variant> VariantView<S> getAsView(Class<S> viewElementType) {
		Preconditions.checkArgument(viewElementType.isAssignableFrom(elementType), "element type of the view needs to be the same type or a supertype of the element of this collection");
		return Cast.uncheckedCast("of type erasure", getObjects().newInstance(DefaultVariantView.class, viewElementType, collection, this));
	}

	public void realize() {
		if (!disallowChanges) {
			throw new IllegalStateException("Please disallow changes before realizing the variants.");
		}
		// TODO: Account for no variant, is that even possible?
		collection.iterator().next();
	}

	public VariantCollection<T> disallowChanges() {
		disallowChanges = true;
		return this;
	}

	// TODO: This may not be needed, the only place it's used should probably use public API
	public Set<T> get() {
		return collection;
	}

	public void whenElementKnown(Action<? super KnownVariant<T>> action) {
		knownVariants.all(action);
	}

//	public <S> void whenElementKnown(Class<S> type, Action<? super KnownVariant<S>> action) {
//		knownVariants.all(new TypeFilteringAction<>(type, action));
//	}

	@Value
	private static class VariantCreationArguments<T extends Variant> {
		BuildVariant buildVariant;
		VariantFactory<T> factory;
	}

//	private static class TypeFilteringAction<T extends Variant, S> implements Action<T> {
//		private final Class<S> type;
//		private final Action<? super KnownVariant<S>> action;
//
//		TypeFilteringAction(Class<S> type, Action<? super KnownVariant<S>> action) {
//			this.type = type;
//			this.action = action;
//		}
//
//		@Override
//		public void execute(T t) {
//			if (type.isInstance(t)) {
//				action.execute(type.cast(t));
//			}
//		}
//	}
}
