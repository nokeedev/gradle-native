package dev.gradleplugins.documentationkit.dsl.docbook.model;

import lombok.Value;

@Value
public class ExtensionMetaData {
	String pluginId;
	String extensionId;
	String extensionClass;
}
