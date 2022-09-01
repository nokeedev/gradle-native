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
package dev.nokee.buildadapter.xcode.internal.plugins;

import com.google.common.collect.Iterables;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import dev.nokee.utils.ProviderUtils;
import dev.nokee.xcode.XCProject;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCTarget;
import dev.nokee.xcode.XCTargetReference;
import org.gradle.api.DefaultTask;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.UntrackedTask;
import org.gradle.api.tasks.options.Option;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.function.Consumer;

import static dev.nokee.utils.ProviderUtils.finalizeValueOnRead;
import static dev.nokee.utils.ProviderUtils.ifPresentOrElse;
import static dev.nokee.utils.ProviderUtils.zip;
import static java.util.stream.Collectors.toList;

@UntrackedTask(because = "Produces only non-cacheable console output")
public /*final*/ abstract class InspectXcodeTask extends DefaultTask {
	@Internal
	public abstract Property<XCProjectReference> getXcodeProject();

	@Internal
	@Option(option = "target", description = "Inspect the specified Xcode target.")
	public abstract Property<String> getTargetFlag();

	@Internal
	@Option(option = "format", description = "Inspection format.")
	public abstract Property<Format> getOutputFormat();

	@Internal
	protected abstract Property<XCTargetReference> getXcodeTarget();

	@Internal
	protected abstract Property<ReportContext> getReportContext();

	@Inject
	public InspectXcodeTask(ObjectFactory objects) {
		getOutputFormat().convention(Format.TEXT);
		finalizeValueOnRead(getReportContext().convention(getOutputFormat().map(it -> {
			switch (it) {
				case JSON: return new JsonReportContext(new OutputStreamWriter(System.out));
				case TEXT: return new TextReportContext(System.out);
				default: throw new UnsupportedOperationException();
			}
		})));
		getXcodeTarget().set(zip(() -> objects.listProperty(Object.class),
			getXcodeProject().map(XCProjectReference::load),
			getTargetFlag(),
			(project, targetName) -> project.getTargets().stream().filter(t -> t.getName().equals(targetName)).findFirst().orElse(null)));
	}

	@TaskAction
	void doInspect() {
		ifPresentOrElse(
			getXcodeTarget(),
			targetReference -> new XCTargetReport(targetReference.load()).report(getReportContext().get()),
			() -> new XCProjectReport(getXcodeProject().get().load()).report(getReportContext().get()));
	}

	/**
	 * Represents a report that can be generated using a {@link ReportContext}.
	 */
	public interface Report {
		void report(ReportContext context);
	}

	public interface ReportContext {
		void attribute(String attribute, String value);
		void attribute(String attribute, Iterable<String> values);

		// TODO: Begin and end document should be part of a derived ReportContext because attributeGroup is can't begin or end a document
		void beginDocument();

		void endDocument();

		void attributeGroup(String attribute, Consumer<? super ReportContext> action);
	}

	/**
	 * Produces a text-based report.
	 */
	public static final class TextReportContext implements ReportContext {
		private PrintStream out;

		public TextReportContext(PrintStream out) {
			this.out = out;
		}

		@Override
		public void attribute(String attribute, String value) {
			out.println(attribute + ": " + value);
		}

		@Override
		public void attribute(String attribute, Iterable<String> values) {
			if (Iterables.isEmpty(values)) {
				out.println(attribute + ": (none)");
			} else {
				out.println(attribute + ":");
				values.forEach(it -> out.println(" - " + it));
			}
		}

		@Override
		public void beginDocument() {

		}

		@Override
		public void endDocument() {

		}

		@Override
		public void attributeGroup(String attribute, Consumer<? super ReportContext> action) {
			out.println(attribute + ":");
			action.accept(new ReportContext() {
				@Override
				public void attribute(String attribute, String value) {
					out.print('\t');
					TextReportContext.this.attribute(attribute, value);
				}

				@Override
				public void attribute(String attribute, Iterable<String> values) {
					out.print('\t');
					TextReportContext.this.attribute(attribute, values);
				}

				@Override
				public void beginDocument() {
					throw new UnsupportedOperationException();
				}

				@Override
				public void endDocument() {
					throw new UnsupportedOperationException();
				}

				@Override
				public void attributeGroup(String attribute, Consumer<? super ReportContext> action) {
					out.print('\t');
					TextReportContext.this.attributeGroup(attribute, action);
				}
			});
		}
	}

	/**
	 * Produces a JSON formatted report.
	 */
	public static final class JsonReportContext implements ReportContext {
		private final JsonWriter writer;

		public JsonReportContext(Writer writer) {
			try {
				this.writer = new GsonBuilder().setPrettyPrinting().create().newJsonWriter(writer);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		@Override
		public void attribute(String attribute, String value) {
			try {
				writer.name(attribute).value(value);
			} catch (IOException e) {
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
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		@Override
		public void beginDocument() {
			try {
				writer.beginObject();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		@Override
		public void endDocument() {
			try {
				writer.endObject();
				writer.flush();
			} catch (IOException e) {
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
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	/**
	 * Generate a Xcode Project Report.
	 */
	private static final class XCProjectReport implements Report {
		private final XCProject project;

		public XCProjectReport(XCProject project) {
			this.project = project;
		}

		@Override
		public void report(ReportContext context) {
			context.beginDocument();
			context.attribute("name", project.getName());
			context.attribute("targets", project.getTargets().stream().map(XCTargetReference::getName).collect(toList()));
			context.endDocument();
		}
	}

	/**
	 * Generates a Xcode Target report.
	 */
	private static final class XCTargetReport implements Report {
		private final XCTarget target;

		public XCTargetReport(XCTarget target) {
			this.target = target;
		}

		@Override
		public void report(ReportContext context) {
			context.beginDocument();
			context.attribute("Project name", target.getProject().load().getName());
			context.attribute("name", target.getName());
			context.attribute("dependencies", target.getDependencies().stream().map(it -> it.getProject().getName() + ":" + it.getName() + " (" + (target.getProject().equals(it.getProject()) ? "local" : "remote") + ")").collect(toList()));
			context.attribute("inputFiles", target.getInputFiles().stream().map(Object::toString).collect(toList()));
			context.attributeGroup("product", it -> {
				it.attribute("location", target.getOutputFile().toString());
			});
			context.endDocument();
		}
	}

	public enum Format {
		TEXT, JSON
	}
}
