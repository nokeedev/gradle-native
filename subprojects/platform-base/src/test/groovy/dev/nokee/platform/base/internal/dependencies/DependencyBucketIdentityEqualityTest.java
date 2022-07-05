/*
 * Copyright 2021 the original author or authors.
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

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.names.ElementName.of;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.builder;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.consumable;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.declarable;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.resolvable;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketType.Consumable;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketType.Resolvable;

class DependencyBucketIdentityEqualityTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(declarable("gugu"), builder().name(of("gugu")).build())
			.addEqualityGroup(consumable("gugu"), builder().name(of("gugu")).type(Consumable).build())
			.addEqualityGroup(resolvable("gugu"), builder().name(of("gugu")).type(Resolvable).build())
			.addEqualityGroup(declarable("naxa"), builder().name(of("naxa")).build())
			.addEqualityGroup(builder().name(of("gugu")).displayName("keldsed").build())
			.testEquals();
	}
}
