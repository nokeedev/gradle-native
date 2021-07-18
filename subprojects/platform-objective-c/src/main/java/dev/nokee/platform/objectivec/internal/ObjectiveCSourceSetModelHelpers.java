package dev.nokee.platform.objectivec.internal;

import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.model.internal.core.NodeAction;
import dev.nokee.platform.base.internal.ComponentName;

import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.*;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.configureEachSourceSet;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.whenComponentSourcesDiscovered;

public final class ObjectiveCSourceSetModelHelpers {
	private ObjectiveCSourceSetModelHelpers() {}

	public static NodeAction configureObjectiveCSourceSetConventionUsingMavenAndGradleCoreNativeLayout(ComponentName componentName) {
		return whenComponentSourcesDiscovered().apply(configureEachSourceSet(of(ObjectiveCSourceSet.class), withConventionOf(maven(componentName), defaultObjectiveCGradle(componentName))));
	}
}
