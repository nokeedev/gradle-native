package dev.nokee.platform.base.internal;

import com.google.common.base.Preconditions;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import lombok.Value;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.internal.Cast;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class VariantCollection<T extends Variant> implements Realizable {
	private final Map<String, VariantCreationArguments<T>> variantCreationArguments = new HashMap<>();
	private final Class<T> elementType;
	private final VariantFactory<T> factory;
	private final NamedDomainObjectContainer<T> collection;

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	public VariantCollection(Class<T> elementType, VariantFactory<T> factory) {
		Preconditions.checkArgument(Named.class.isAssignableFrom(elementType), "element type of the collection needs to implement Named");
		this.elementType = elementType;
		this.factory = factory;
		this.collection = getObjects().domainObjectContainer(elementType, this::create);
	}

	private T create(String name) {
		VariantCreationArguments<T> args = variantCreationArguments.remove(name);
		T result = factory.create(name, args.names, args.targetMachine);
		args.defaultAction.execute(result);
		return result;
	}

	// TODO: The naming scheme and dimensions (targetMachine in this case) are closly related.
	//  They should navigate together as a single type.
	// TODO: It sucks that I'm leaking implementation details via the return type...
	//  This needs to be cleaned up before making public.
	public NamedDomainObjectProvider<T> registerVariant(NamingScheme names, Object targetMachine, Action<? super T> defaultAction) {
		String variantName = names.getVariantName();//targetMachine.getOperatingSystemFamily().getName() + StringUtils.capitalize(targetMachine.getArchitecture().getName());
		variantCreationArguments.put(variantName, new VariantCreationArguments<T>(names, targetMachine, defaultAction));
		return collection.register(variantName);
	}

	// TODO: I don't like that we have to pass in the viewElementType
	public <S extends Variant> VariantView<S> getAsView(Class<S> viewElementType) {
		Preconditions.checkArgument(viewElementType.isAssignableFrom(elementType), "element type of the view needs to be the same type or a supertype of the element of this collection");
		return Cast.uncheckedCast(getObjects().newInstance(DefaultVariantView.class, collection));
	}

	public void realize() {
		// TODO: Account for no variant, is that even possible?
		collection.iterator().next();
	}

	// TODO: This may not be needed, the only place it's used should probably use public API
	public Set<T> get() {
		return collection;
	}

	@Value
	private static class VariantCreationArguments<T extends Variant> {
		NamingScheme names;
		Object targetMachine;
		Action<? super T> defaultAction;
	}
}
