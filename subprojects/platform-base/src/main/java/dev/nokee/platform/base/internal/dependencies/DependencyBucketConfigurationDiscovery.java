/*
 * Copyright 2024 the original author or authors.
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

package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.ModelObjectIdentity;
import dev.nokee.model.internal.discover.DisRule;
import dev.nokee.model.internal.discover.Discovery;
import dev.nokee.model.internal.discover.RealizeRule;
import dev.nokee.model.internal.discover.SubTypeOfRule;
import dev.nokee.model.internal.type.ModelType;
import org.gradle.api.artifacts.Configuration;

import java.util.Collections;
import java.util.List;

public class DependencyBucketConfigurationDiscovery implements Discovery {
	@Override
	public <T> List<DisRule> discover(ModelType<T> discoveringType) {
		return Collections.singletonList(new RealizeRule(new SubTypeOfRule(discoveringType, new DisRule() {
			@Override
			public void execute(Details details) {
				// The Configuration shadow the DependencyBucket
				details.newCandidate(ModelObjectIdentity.ofIdentity(details.getCandidate().getIdentifier(), ModelType.of(Configuration.class)));
			}
		})));
	}
}
