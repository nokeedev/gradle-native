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
	private final XMLStreamWriter delegate;

	public XCSchemeWriter(Writer writer) {
		try {
			this.delegate = XMLOutputFactory.newFactory().createXMLStreamWriter(writer);
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
			delegate.writeStartDocument();
			delegate.writeStartElement("Scheme");
			delegate.writeAttribute("LastUpgradeVersion", scheme.getLastUpgradeVersion());
			delegate.writeAttribute("version", scheme.getVersion());

			writeBuildAction(scheme.getBuildAction());

			writeTestAction(scheme.getTestAction());

			writeLaunchAction(scheme.getLaunchAction());

			writeProfileAction(scheme.getProfileAction());

			writeAnalyzeAction(scheme.getAnalyzeAction());

			writeArchiveAction(scheme.getArchiveAction());

			delegate.writeEndElement();
			delegate.writeEndDocument();
		});
	}

	private void writeArchiveAction(XCScheme.ArchiveAction action) throws XMLStreamException {
		delegate.writeStartElement("ArchiveAction");
		delegate.writeAttribute("buildConfiguration", action.getBuildConfiguration());
		delegate.writeAttribute("revealArchiveInOrganizer", toYesNo(action.getRevealArchiveInOrganizer()));
		delegate.writeEndElement();
	}

	private void writeAnalyzeAction(XCScheme.AnalyzeAction action) throws XMLStreamException {
		delegate.writeStartElement("AnalyzeAction");
		delegate.writeAttribute("buildConfiguration", action.getBuildConfiguration());
		delegate.writeEndElement();
	}

	private void writeProfileAction(XCScheme.ProfileAction action) throws XMLStreamException {
		delegate.writeStartElement("ProfileAction");
		delegate.writeAttribute("buildConfiguration", action.getBuildConfiguration());
		delegate.writeAttribute("shouldUseLaunchSchemeArgsEnv", toYesNo(action.getShouldUseLaunchSchemeArgsEnv()));
		delegate.writeAttribute("savedToolIdentifier", action.getSavedToolIdentifier());
		delegate.writeAttribute("useCustomWorkingDirectory", toYesNo(action.getUseCustomWorkingDirectory()));
		delegate.writeAttribute("debugDocumentVersioning", toYesNo(action.getDebugDocumentVersioning()));
		delegate.writeEndElement();
	}

	private void writeLaunchAction(XCScheme.LaunchAction action) throws XMLStreamException {
		delegate.writeStartElement("LaunchAction");
		delegate.writeAttribute("buildConfiguration", action.getBuildConfiguration());
		delegate.writeAttribute("selectedDebuggerIdentifier", action.getSelectedDebuggerIdentifier());
		delegate.writeAttribute("selectedLauncherIdentifier", action.getSelectedLauncherIdentifier());
		delegate.writeAttribute("launchStyle", action.getLaunchStyle());
		delegate.writeAttribute("useCustomWorkingDirectory", toYesNo(action.getUseCustomWorkingDirectory()));
		delegate.writeAttribute("ignoresPersistentStateOnLaunch", toYesNo(action.getIgnoresPersistentStateOnLaunch()));
		delegate.writeAttribute("debugDocumentVersioning", toYesNo(action.getDebugDocumentVersioning()));
		delegate.writeAttribute("debugServiceExtension", action.getDebugServiceExtension());
		delegate.writeAttribute("allowLocationSimulation", toYesNo(action.getAllowLocationSimulation()));

		if (action.getBuildableProductRunnable() != null) {
			delegate.writeStartElement("BuildableProductRunnable");
			delegate.writeAttribute("runnableDebuggingMode", action.getBuildableProductRunnable().getRunnableDebuggingMode());
			writeBuildableReference(action.getBuildableProductRunnable().getBuildableReference());
			delegate.writeEndElement();
		}

		delegate.writeEndElement();
	}

	private void writeTestAction(XCScheme.TestAction action) throws XMLStreamException {
		delegate.writeStartElement("TestAction");
		delegate.writeAttribute("buildConfiguration", action.getBuildConfiguration());
		delegate.writeAttribute("selectedDebuggerIdentifier", action.getSelectedDebuggerIdentifier());
		delegate.writeAttribute("selectedLauncherIdentifier", action.getSelectedLauncherIdentifier());
		delegate.writeAttribute("shouldUseLaunchSchemeArgsEnv", toYesNo(action.getShouldUseLaunchSchemeArgsEnv()));

		delegate.writeStartElement("Testables");
		for (XCScheme.TestAction.TestableReference testable : action.getTestables()) {
			delegate.writeStartElement("TestableReference");
			delegate.writeAttribute("skipped", toYesNo(testable.getSkipped()));
			writeBuildableReference(testable.getBuildableReference());
			delegate.writeEndElement();
		}
		delegate.writeEndElement();

		delegate.writeEndElement();
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
		delegate.writeStartElement("BuildAction");
		delegate.writeAttribute("parallelizeBuildables", toYesNo(action.getParallelizeBuildables()));
		delegate.writeAttribute("buildImplicitDependencies", toYesNo(action.getBuildImplicitDependencies()));

		delegate.writeStartElement("BuildActionEntries");
		for (XCScheme.BuildAction.BuildActionEntry entry : action.getBuildActionEntries()) {
			writeBuildActionEntry(entry);
		}
		delegate.writeEndElement();

		delegate.writeEndElement();
	}

	private void writeBuildActionEntry(XCScheme.BuildAction.BuildActionEntry entry) throws XMLStreamException {
		delegate.writeStartElement("BuildActionEntry");
		delegate.writeAttribute("buildForTesting", toYesNo(entry.isBuildForTesting()));
		delegate.writeAttribute("buildForRunning", toYesNo(entry.isBuildForRunning()));
		delegate.writeAttribute("buildForProfiling", toYesNo(entry.isBuildForProfiling()));
		delegate.writeAttribute("buildForArchiving", toYesNo(entry.isBuildForArchiving()));
		delegate.writeAttribute("buildForAnalyzing", toYesNo(entry.isBuildForAnalyzing()));

		writeBuildableReference(entry.getBuildableReference());

		delegate.writeEndElement();
	}

	private void writeBuildableReference(XCScheme.BuildableReference buildableReference) throws XMLStreamException {
		delegate.writeStartElement("BuildableReference");
		delegate.writeAttribute("BuildableIdentifier", buildableReference.getBuildableIdentifier());
		delegate.writeAttribute("BlueprintIdentifier", buildableReference.getBlueprintIdentifier());
		delegate.writeAttribute("BuildableName", buildableReference.getBuildableName());
		delegate.writeAttribute("BlueprintName", buildableReference.getBlueprintName());
		delegate.writeAttribute("ReferencedContainer", buildableReference.getReferencedContainer());
		delegate.writeEndElement();
	}

	private static String toYesNo(boolean value) {
		return value ? "YES" : "NO";
	}

	public void flush() {
		run(() -> delegate.flush());
	}

	@Override
	public void close() throws IOException {
		try {
			delegate.close();
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
