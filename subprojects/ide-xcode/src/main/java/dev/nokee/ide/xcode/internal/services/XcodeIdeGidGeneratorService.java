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
package dev.nokee.ide.xcode.internal.services;

import dev.nokee.ide.xcode.internal.xcodeproj.GidGenerator;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import javax.inject.Inject;
import java.util.Collections;

public abstract class XcodeIdeGidGeneratorService extends GidGenerator implements BuildService<BuildServiceParameters.None> {
	@Inject
	public XcodeIdeGidGeneratorService() {
		super(Collections.emptySet());
	}
}
