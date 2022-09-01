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

import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.function.Consumer;

/**
 * Produces a JSON formatted report.
 */
public final class JsonReportContext implements ReportContext {
	private final JsonWriter writer;

	public JsonReportContext(Writer writer) {
		try {
			this.writer = new GsonBuilder().setPrettyPrinting().create().newJsonWriter(writer);
		} catch (
			IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void attribute(String attribute, String value) {
		try {
			writer.name(attribute).value(value);
		} catch (
			IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void attribute(String attribute, Iterable<String> values) {
		try {
			writer.name(attribute).beginArray();
			for (String it : values) {
				writer.value(it);
			}
			writer.endArray();
		} catch (
			IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void beginDocument() {
		try {
			writer.beginObject();
		} catch (
			IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void endDocument() {
		try {
			writer.endObject();
			writer.flush();
		} catch (
			IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void attributeGroup(String attribute, Consumer<? super ReportContext> action) {
		try {
			writer.name(attribute);
			writer.beginObject();
			action.accept(this);
			writer.endObject();
		} catch (
			IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
