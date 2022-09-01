/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal.reporting;

import dev.nokee.buildadapter.xcode.internal.plugins.InspectXcodeTask;

import java.util.function.Consumer;

public interface ReportContext {
	void attribute(String attribute, String value);

	void attribute(String attribute, Iterable<String> values);

	// TODO: Begin and end document should be part of a derived ReportContext because attributeGroup is can't begin or end a document
	void beginDocument();

	void endDocument();

	void attributeGroup(String attribute, Consumer<? super ReportContext> action);
}
