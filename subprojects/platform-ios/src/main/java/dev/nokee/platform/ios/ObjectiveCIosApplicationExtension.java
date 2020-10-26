package dev.nokee.platform.ios;

import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

public interface ObjectiveCIosApplicationExtension extends DependencyAwareComponent<NativeComponentDependencies>, VariantAwareComponent<IosApplication>, BinaryAwareComponent, SourceAwareComponent {
	/**
	 * Defines the source files or directories of this application.
	 * You can add files or directories to this collection.
	 * When a directory is added, all source files are included for compilation.
	 *
	 * <p>When this collection is empty, the directory {@code src/main/objc} is used by default.</p>
	 *
	 * @since 0.5
	 */
	ObjectiveCSourceSet getObjectiveCSources();

	/**
	 * Configures the source files or directories of this application.
	 *
	 * @param action The action to execute for source set configuration.
	 * @see #getObjectiveCSources()
	 * @since 0.5
	 */
	void objectiveCSources(Action<? super ObjectiveCSourceSet> action);

	/**
	 * Configures the source files or directories of this application.
	 *
	 * @param closure The closure to execute for source set configuration.
	 * @see #getObjectiveCSources()
	 * @since 0.5
	 */
	default void objectiveCSources(@DelegatesTo(value = ObjectiveCSourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		objectiveCSources(ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Defines the private headers search directories of this application.
	 *
	 * <p>When this collection is empty, the directory {@code src/main/headers} is used by default.</p>
	 *
	 * @since 0.5
	 */
	CHeaderSet getPrivateHeaders();

	/**
	 * Configures the private headers search directories of this application.
	 *
	 * @param action The action to execute for source set configuration.
	 * @see #getPrivateHeaders()
	 * @since 0.5
	 */
	void privateHeaders(Action<? super CHeaderSet> action);

	/**
	 * Configures the private headers search directories of this application.
	 *
	 * @param closure The action to execute for source set configuration.
	 * @see #getPrivateHeaders()
	 * @since 0.5
	 */
	default void privateHeaders(@DelegatesTo(value = CHeaderSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		privateHeaders(ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Defines the iOS resources directories of this application.
	 *
	 * <p>When this collection is empty, the directory {@code src/main/resources} is used by default.</p>
	 *
	 * @since 0.5
	 */
	IosResourceSet getResources();

	/**
	 * Configures the resources directories of this application.
	 *
	 * @param action The action to execute for source set configuration.
	 * @see #getResources()
	 * @since 0.5
	 */
	void resources(Action<? super IosResourceSet> action);

	/**
	 * Configures the iOS resources directories of this application.
	 *
	 * @param closure The action to execute for source set configuration.
	 * @see #getResources()
	 * @since 0.5
	 */
	default void resources(@DelegatesTo(value = IosResourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		resources(ConfigureUtil.configureUsing(closure));
	}
}
