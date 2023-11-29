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
package dev.nokee.platform.swift;

import dev.nokee.language.swift.HasSwiftSources;
import dev.nokee.platform.nativebase.NativeApplicationExtension;

/**
 * Configuration for an application written in Swift, defining the dependencies that make up the application plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the Swift Application Plugin.</p>
 *
 * @since 0.5
 */
public interface SwiftApplication extends NativeApplicationExtension
	, HasSwiftSources
{
}
