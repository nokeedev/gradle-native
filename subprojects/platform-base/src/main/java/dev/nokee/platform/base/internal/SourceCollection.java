package dev.nokee.platform.base.internal;

import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.SourceSet;
import dev.nokee.language.base.internal.SourceSetTransform;
import dev.nokee.language.base.internal.UTType;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.internal.Cast;

import javax.inject.Inject;

public abstract class SourceCollection<T extends UTType> {
	private final T elementType;
	private final DomainObjectSet<SourceSet<T>> collection;

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	public SourceCollection(T elementType) {
		this.elementType = elementType;
		this.collection = Cast.uncheckedCast(getObjects().domainObjectSet(SourceSet.class));
	}

	public boolean add(SourceSet<? extends UTType> sourceSet) {
		return collection.add(Cast.uncheckedCast(sourceSet));
	}

	public <S extends UTType> void configureEach(UTType type, Action<? super SourceSet<S>> action) {
		collection.all(it -> {
			if (it.getType().equals(type)) {
				action.execute(Cast.uncheckedCast(it));
			}
		});
	}

	// TODO: Return type should probably be an immutable type
	public <IN extends UTType, OUT extends UTType> DomainObjectSet<GeneratedSourceSet<OUT>> transformEach(IN type, SourceSetTransform<IN, OUT> transform) {
		DomainObjectSet<GeneratedSourceSet<OUT>> result = Cast.uncheckedCast(getObjects().domainObjectSet(SourceSet.class));
		collection.all(it -> {
			if (it.getType().equals(type)) {
				SourceSet<OUT> sourceSet = ((SourceSet<IN>)it).transform(transform);
				result.add(Cast.uncheckedCast(sourceSet));
			}
		});
		return result;
	}
}
