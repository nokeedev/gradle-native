package dev.nokee.platform.base.internal;

import com.google.common.base.Preconditions;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.internal.Cast;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
		T result = factory.create(name, args.buildVariant);
		args.defaultAction.execute(result);
		return result;
	}

	// TODO: It sucks that I'm leaking implementation details via the return type...
	//  This needs to be cleaned up before making public.
	public NamedDomainObjectProvider<T> registerVariant(BuildVariant buildVariant, Action<? super T> defaultAction) {
		String variantName = StringUtils.uncapitalize(buildVariant.getDimensions().stream().map(this::determineName).map(StringUtils::capitalize).collect(Collectors.joining()));
		variantCreationArguments.put(variantName, new VariantCreationArguments<T>(buildVariant, defaultAction));
		return collection.register(variantName);
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
		BuildVariant buildVariant;
		Action<? super T> defaultAction;
	}
}
