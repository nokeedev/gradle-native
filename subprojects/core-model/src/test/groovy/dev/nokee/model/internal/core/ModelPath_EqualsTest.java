/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelPath.root;

class ModelPath_EqualsTest {
    @Test
	@SuppressWarnings("UnstableApiUsage")
    void canEquals() {
        new EqualsTester()
                .addEqualityGroup(path("a.b.c"), path("a.b.c"))
                .addEqualityGroup(path("z.y.z"))
                .addEqualityGroup(root(), path("a").getParent().get())
                .addEqualityGroup(path("z.y.q").getParent().get(), path("z.y"))
                .testEquals();
    }
}
