/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
