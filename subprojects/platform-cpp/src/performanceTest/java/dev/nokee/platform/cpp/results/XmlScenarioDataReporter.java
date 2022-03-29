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
package dev.nokee.platform.cpp.results;

import com.google.common.collect.Streams;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class XmlScenarioDataReporter implements DataReporter<PerformanceTestResult> {
	private static final String TEST_RESULTS_DIRECTORY_PROP_KEY = "dev.nokee.performance.results.dir";

	@Override
	public void report(PerformanceTestResult results) {
		final Path testResultsDirectory = Optional.ofNullable(System.getProperty(TEST_RESULTS_DIRECTORY_PROP_KEY)).map(File::new).map(File::toPath).orElseThrow(RuntimeException::new);

		try (Writer outWriter = new FileWriter(createParentDirectories(testResultsDirectory.resolve("SCENARIO-dev.nokee.platform.cpp.RealWorldCppBuildPerformanceTest.xml")).toFile())) {
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outWriter);
			writer.writeStartDocument();
			writer.writeStartElement("scenario");
			writer.writeAttribute("name", "dev.nokee.platform.cpp.RealWorldCppBuildPerformanceTest");
			writer.writeAttribute("hostname", InetAddress.getLocalHost().getHostName());

			results.forEach((name, result) -> {
				try {
					writer.writeStartElement("experiment");
					writer.writeAttribute("name", name);

					// TODO: Should we write the warmup/measurement count as attribute or together with actions?
					writer.writeAttribute("warmup", String.valueOf(result.getExecutionResult().getWarmUpRuns()));
					writer.writeAttribute("measurement", String.valueOf(result.getExecutionResult().getMeasurementRuns()));

					writer.writeStartElement("build");
					writeObject(writer, result.getBuildAction());
					writer.writeEndElement();
					writer.writeStartElement("clean");
					result.getCleanAction().ifPresent(it -> writeObject(writer, it));
					writer.writeEndElement();

					// TODO: Write invocation spec

					writer.writeStartElement("series");
					writer.writeAttribute("max", result.getTotalTime().getMax().format());
					writer.writeAttribute("min", result.getTotalTime().getMin().format());
					writer.writeAttribute("median", result.getTotalTime().getMedian().format());
					writer.writeAttribute("se", result.getTotalTime().getStandardError().format());
					Streams.stream(result.getTotalTime()).forEach(it -> {
						try {
							writer.writeStartElement("data");
							writer.writeCharacters(it.format());
							writer.writeEndElement();
						} catch (XMLStreamException e) {
							throw new RuntimeException(e);
						}
					});
					writer.writeEndElement();
					writer.writeEndElement();
				} catch (XMLStreamException e) {
					throw new RuntimeException(e);
				}
			});

			writer.writeEndElement();
			writer.writeEndDocument();
		} catch (XMLStreamException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Path createParentDirectories(Path path) throws IOException {
		Files.createDirectories(path.getParent());
		return path;
	}

	private static void writeObject(XMLStreamWriter self, Object o) {
		try {
			final Method writeObjectMethod = o.getClass().getMethod("writeObject", XMLStreamWriter.class);
			writeObjectMethod.setAccessible(true);
			writeObjectMethod.invoke(o, self);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}
