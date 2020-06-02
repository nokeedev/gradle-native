package dev.nokee.platform.ios.internal;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import dev.nokee.core.exec.internal.AbstractCommandLineTool;
import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.Input;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

public class DescriptorCommandLineTool extends AbstractCommandLineTool {
	private final CommandLineToolDescriptor toolDescriptor;

	public DescriptorCommandLineTool(File toolDescriptor) {
		try {
			this.toolDescriptor = new GsonBuilder().registerTypeAdapter(File.class, (JsonDeserializer<File>) (json, typeOfT, context) -> new File(json.getAsString())).create().fromJson(FileUtils.readFileToString(toolDescriptor, Charset.defaultCharset()), CommandLineToolDescriptor.class);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public String getExecutable() {
		return toolDescriptor.getPath().getAbsolutePath();
	}

	@Input
	public String getVersion() {
		return toolDescriptor.getVersion();
	}
}
