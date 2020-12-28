package dev.nokee.platform.objectivecpp.internal;

import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.model.internal.core.NodeAction;
import dev.nokee.platform.base.internal.ComponentName;

import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.*;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.configureEachSourceSet;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.whenComponentSourcesDiscovered;

public final class ObjectiveCppSourceSetModelHelpers {
	private ObjectiveCppSourceSetModelHelpers() {}

	public static NodeAction configureObjectiveCppSourceSetConventionUsingMavenAndGradleCoreNativeLayout(ComponentName componentName) {
		return whenComponentSourcesDiscovered().apply(configureEachSourceSet(of(ObjectiveCppSourceSet.class), withConventionOf(maven(componentName), defaultObjectiveCppGradle(componentName))));
	}
}
