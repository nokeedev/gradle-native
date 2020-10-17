package dev.nokee.platform.base.internal.variants;

import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import org.gradle.api.Action;

public interface VariantViewInternal<T extends Variant> extends VariantView<T> {
	void whenElementKnown(Action<? super KnownVariant<T>> action);
	<S extends T> void whenElementKnown(Class<S> type, Action<? super KnownVariant<S>> action);
}
