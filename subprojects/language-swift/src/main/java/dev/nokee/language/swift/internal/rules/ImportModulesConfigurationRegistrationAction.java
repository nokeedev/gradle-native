/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.language.swift.internal.rules;

import dev.nokee.language.nativebase.internal.FrameworkAwareIncomingArtifacts;
import dev.nokee.language.swift.internal.SwiftSourceSetSpec;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import java.nio.file.Path;
import java.util.Set;

import static dev.nokee.language.nativebase.internal.FrameworkAwareIncomingArtifacts.frameworks;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.TransformerUtils.toSetTransformer;
import static dev.nokee.utils.TransformerUtils.transformEach;

public final class ImportModulesConfigurationRegistrationAction implements Action<SwiftSourceSetSpec> {
	private final ObjectFactory objects;

	public ImportModulesConfigurationRegistrationAction(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void execute(SwiftSourceSetSpec sourceSet) {
		Configuration importModules = sourceSet.getImportModules().getAsConfiguration();
		forSwiftApiUsage().execute(importModules);
		val incomingArtifacts = FrameworkAwareIncomingArtifacts.from(incomingArtifactsOf(importModules));
		sourceSet.getDependentFrameworkSearchPaths().from(incomingArtifacts.getAs(frameworks()).map(parentFiles()));
		sourceSet.getDependentImportModules().from(incomingArtifacts.getAs(frameworks().negate()));
	}

	private Action<Configuration> forSwiftApiUsage() {
		return configureAttributes(builder -> builder.usage(objects.named(Usage.class, Usage.SWIFT_API)));
	}

	private static Transformer<Set<Path>, Iterable<? extends Path>> parentFiles() {
		return transformEach(Path::getParent).andThen(toSetTransformer(Path.class));
	}

	private Provider<ResolvableDependencies> incomingArtifactsOf(Configuration config) {
		return ProviderUtils.fixed(config.getIncoming());
	}
}
