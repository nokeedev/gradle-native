package dev.nokee.runtime.base;

import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.apache.commons.io.FilenameUtils.separatorsToUnix;

public final class VerifyTask {
	private final List<String> verifySegments = new ArrayList<>();

	private VerifyTask() {
		val outStream = new ByteArrayOutputStream();
		val out = new PrintWriter(outStream);
		out.println("def verifyTask = tasks.register('verify') {");
		out.println("	ext.transformed = { files ->");
		out.println("		files.find { it.parentFile.name == 'transformed' || it.path.contains('.transforms') || it.path.contains('transforms-') }");
		out.println("	}");
		out.println("}");
		out.flush();

		verifySegments.add(outStream.toString());
	}

	public VerifyTask that(Supplier<? extends Object> supplier) {
		val outStream = new ByteArrayOutputStream();
		val out = new PrintWriter(outStream);
		out.println("verifyTask.configure {");
		out.println("	doLast {");
		out.println("		assert " + separatorsToUnix(supplier.get().toString()));
		out.println("	}");
		out.println("}");
		out.flush();

		verifySegments.add(outStream.toString());

		return this;
	}

	@Override
	public String toString() {
		return String.join(System.lineSeparator(), verifySegments);
	}

	public static VerifyTask verifyTask() {
		return new VerifyTask();
	}

	public static String artifactType(String type) {
		val outStream = new ByteArrayOutputStream();
		val out = new PrintWriter(outStream);
		out.println("incoming.artifactView {");
		out.println("	attributes {");
		out.println("		attribute(Attribute.of('artifactType', String), '" + type + "')");
		out.println("	}");
		out.println("	lenient = true");
		out.print("}.files"); // no new line is on purpose
		out.flush();

		return outStream.toString();
	}

	public static String allFiles() {
		return "incoming.artifactView { lenient = true }.files";
	}

	public static String uncompressedFiles() {
		val outStream = new ByteArrayOutputStream();
		val out = new PrintWriter(outStream);
		out.println("incoming.artifactView {");
		out.println("	attributes {");
		out.println("		attribute(dev.nokee.runtime.nativebase.internal.ArtifactCompressionState.ARTIFACT_COMPRESSION_STATE_ATTRIBUTE, dev.nokee.runtime.nativebase.internal.ArtifactCompressionState.UNCOMPRESSED)");
		out.println("	}");
		out.print("}.files"); // no new line is on purpose
		out.flush();

		return outStream.toString();
	}
}
