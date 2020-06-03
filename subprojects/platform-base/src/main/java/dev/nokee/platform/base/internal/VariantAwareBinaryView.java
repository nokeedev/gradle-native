package dev.nokee.platform.base.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import dev.nokee.internal.Cast;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.specs.Spec;

import javax.inject.Inject;
import java.util.Set;

public abstract class VariantAwareBinaryView<T extends Binary> extends AbstractView<T> implements BinaryView<T> {
	private final Class<T> binaryType;
	private final VariantView<? extends Variant> variants;

	@Inject
	public VariantAwareBinaryView(Class<T> binaryType, VariantView<? extends Variant> variants) {
		this.binaryType = binaryType;
		this.variants = variants;
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		Preconditions.checkArgument(action != null, "configure each action for binary view must not be null");
		variants.configureEach(variant -> variant.getBinaries().configureEach(type, action));
	}

	@Override
	public <S extends T> BinaryView<S> withType(Class<S> type) {
		Preconditions.checkArgument(type != null, "binary view subview type must not be null");
		if (binaryType.equals(type)) {
			return Cast.uncheckedCast("view types are the same", this);
		}
		return Cast.uncheckedCast("of type erasure", getObjects().newInstance(VariantAwareBinaryView.class, type, variants));
	}

	@Override
	public void configureEach(Action<? super T> action) {
		Preconditions.checkArgument(action != null, "configure each action for binary view must not be null");
		variants.configureEach(variant -> variant.getBinaries().configureEach(binaryType, action));
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		Preconditions.checkArgument(action != null, "configure each action for binary view must not be null");
		variants.configureEach(variant -> variant.getBinaries().withType(binaryType).configureEach(spec, action));
	}

	@Override
	public Set<? extends T> get() {
		ImmutableSet.Builder<T> builder = ImmutableSet.builder();
		variants.get().forEach(it -> builder.addAll(it.getBinaries().withType(binaryType).get()));
		return builder.build();
	}

	@Override
	protected String getDisplayName() {
		return "binary view";
	}
}
