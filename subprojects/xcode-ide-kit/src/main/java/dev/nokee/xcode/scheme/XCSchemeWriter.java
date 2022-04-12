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
package dev.nokee.xcode.scheme;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;

public final class XCSchemeWriter implements Closeable {
	private final XMLStreamWriter writer;

	public XCSchemeWriter(Writer delegate) {
		try {
			writer = XMLOutputFactory.newFactory().createXMLStreamWriter(delegate);
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * <Scheme
	 * LastUpgradeVersion = "0830"
	 * version = "1.3">
	 * ...
	 * </Scheme>
	 */
	public void write(XCScheme scheme) {
		run(() -> {
			writer.writeStartDocument();
			writer.writeStartElement("Scheme");
			writer.writeAttribute("LastUpgradeVersion", scheme.getLastUpgradeVersion());
			writer.writeAttribute("version", scheme.getVersion());

			writeBuildAction(scheme.getBuildAction());

			writeTestAction(scheme.getTestAction());

			writeLaunchAction(scheme.getLaunchAction());

			writeProfileAction(scheme.getProfileAction());

			writeAnalyzeAction(scheme.getAnalyzeAction());

			writeArchiveAction(scheme.getArchiveAction());

			writer.writeEndElement();
			writer.writeEndDocument();
		});
	}

	private void writeArchiveAction(XCScheme.ArchiveAction action) throws XMLStreamException {
		writer.writeStartElement("ArchiveAction");
		writer.writeAttribute("buildConfiguration", action.getBuildConfiguration());
		writer.writeAttribute("revealArchiveInOrganizer", toYesNo(action.getRevealArchiveInOrganizer()));
		writer.writeEndElement();
	}

	private void writeAnalyzeAction(XCScheme.AnalyzeAction action) throws XMLStreamException {
		writer.writeStartElement("AnalyzeAction");
		writer.writeAttribute("buildConfiguration", action.getBuildConfiguration());
		writer.writeEndElement();
	}

	private void writeProfileAction(XCScheme.ProfileAction action) throws XMLStreamException {
		writer.writeStartElement("ProfileAction");
		writer.writeAttribute("buildConfiguration", action.getBuildConfiguration());
		writer.writeAttribute("shouldUseLaunchSchemeArgsEnv", toYesNo(action.getShouldUseLaunchSchemeArgsEnv()));
		writer.writeAttribute("savedToolIdentifier", action.getSavedToolIdentifier());
		writer.writeAttribute("useCustomWorkingDirectory", toYesNo(action.getUseCustomWorkingDirectory()));
		writer.writeAttribute("debugDocumentVersioning", toYesNo(action.getDebugDocumentVersioning()));
		writer.writeEndElement();
	}

	private void writeLaunchAction(XCScheme.LaunchAction action) throws XMLStreamException {
		writer.writeStartElement("LaunchAction");
		writer.writeAttribute("buildConfiguration", action.getBuildConfiguration());
		writer.writeAttribute("selectedDebuggerIdentifier", action.getSelectedDebuggerIdentifier());
		writer.writeAttribute("selectedLauncherIdentifier", action.getSelectedLauncherIdentifier());
		writer.writeAttribute("launchStyle", action.getLaunchStyle());
		writer.writeAttribute("useCustomWorkingDirectory", toYesNo(action.getUseCustomWorkingDirectory()));
		writer.writeAttribute("ignoresPersistentStateOnLaunch", toYesNo(action.getIgnoresPersistentStateOnLaunch()));
		writer.writeAttribute("debugDocumentVersioning", toYesNo(action.getDebugDocumentVersioning()));
		writer.writeAttribute("debugServiceExtension", action.getDebugServiceExtension());
		writer.writeAttribute("allowLocationSimulation", toYesNo(action.getAllowLocationSimulation()));

		if (action.getBuildableProductRunnable() != null) {
			writer.writeStartElement("BuildableProductRunnable");
			writer.writeAttribute("runnableDebuggingMode", action.getBuildableProductRunnable().getRunnableDebuggingMode());
			writeBuildableReference(action.getBuildableProductRunnable().getBuildableReference());
			writer.writeEndElement();
		}

		writer.writeEndElement();
	}

	private void writeTestAction(XCScheme.TestAction action) throws XMLStreamException {
		writer.writeStartElement("TestAction");
		writer.writeAttribute("buildConfiguration", action.getBuildConfiguration());
		writer.writeAttribute("selectedDebuggerIdentifier", action.getSelectedDebuggerIdentifier());
		writer.writeAttribute("selectedLauncherIdentifier", action.getSelectedLauncherIdentifier());
		writer.writeAttribute("shouldUseLaunchSchemeArgsEnv", toYesNo(action.getShouldUseLaunchSchemeArgsEnv()));

		writer.writeStartElement("Testables");
		for (XCScheme.TestAction.TestableReference testable : action.getTestables()) {
			writer.writeStartElement("TestableReference");
			writer.writeAttribute("skipped", toYesNo(testable.getSkipped()));
			writeBuildableReference(testable.getBuildableReference());
			writer.writeEndElement();
		}
		writer.writeEndElement();

		writer.writeEndElement();
	}

	/**
	 * <BuildAction
	 * parallelizeBuildables = "YES"
	 * buildImplicitDependencies = "YES">
	 * <BuildActionEntries>
	 * </BuildActionEntries>
	 * </BuildAction>
	 */
	private void writeBuildAction(XCScheme.BuildAction action) throws XMLStreamException {
		writer.writeStartElement("BuildAction");
		writer.writeAttribute("parallelizeBuildables", toYesNo(action.getParallelizeBuildables()));
		writer.writeAttribute("buildImplicitDependencies", toYesNo(action.getBuildImplicitDependencies()));

		writer.writeStartElement("BuildActionEntries");
		for (XCScheme.BuildAction.BuildActionEntry entry : action.getBuildActionEntries()) {
			writeBuildActionEntry(entry);
		}
		writer.writeEndElement();

		writer.writeEndElement();
	}

	private void writeBuildActionEntry(XCScheme.BuildAction.BuildActionEntry entry) throws XMLStreamException {
		writer.writeStartElement("BuildActionEntry");
		writer.writeAttribute("buildForTesting", toYesNo(entry.isBuildForTesting()));
		writer.writeAttribute("buildForRunning", toYesNo(entry.isBuildForRunning()));
		writer.writeAttribute("buildForProfiling", toYesNo(entry.isBuildForProfiling()));
		writer.writeAttribute("buildForArchiving", toYesNo(entry.isBuildForArchiving()));
		writer.writeAttribute("buildForAnalyzing", toYesNo(entry.isBuildForAnalyzing()));

		writeBuildableReference(entry.getBuildableReference());

		writer.writeEndElement();
	}

	private void writeBuildableReference(XCScheme.BuildableReference buildableReference) throws XMLStreamException {
		writer.writeStartElement("BuildableReference");
		writer.writeAttribute("BuildableIdentifier", buildableReference.getBuildableIdentifier());
		writer.writeAttribute("BlueprintIdentifier", buildableReference.getBlueprintIdentifier());
		writer.writeAttribute("BuildableName", buildableReference.getBuildableName());
		writer.writeAttribute("BlueprintName", buildableReference.getBlueprintName());
		writer.writeAttribute("ReferencedContainer", buildableReference.getReferencedContainer());
		writer.writeEndElement();
	}

	private static String toYesNo(boolean value) {
		return value ? "YES" : "NO";
	}

	public void flush() {
		run(() -> writer.flush());
	}

	@Override
	public void close() throws IOException {
		try {
			writer.close();
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}

	private void run(XmlStreamRunnable runnable) {
		try {
			runnable.run();
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	private interface XmlStreamRunnable {
		void run() throws XMLStreamException;
	}
}
