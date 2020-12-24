package dev.nokee.platform.nativebase;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.platform.base.ComponentSources;

/**
 * Represents a component sources that carries an native header set named {@literal public}.
 *
 * @see ComponentSources
 * @see NativeHeaderSet
 * @since 0.5
 */
public interface HasPublicSourceSet {
// Note: There is no safe accessor for public {@link NativeHeaderSet} as `public` is a Java keyword. Use `get('public' CHeaderSet)`.
	/**
	 * Returns a native header set provider for the source set named {@literal public}.
	 *
	 * @return a provider for {@literal public} source set, never null
	 */
	default DomainObjectProvider<NativeHeaderSet> getPublic() {
		return ((FunctionalSourceSet) this).get("public", NativeHeaderSet.class);
	}
	// Note: Use configure("public", CppHeaderSet) {}  for public headers
}
