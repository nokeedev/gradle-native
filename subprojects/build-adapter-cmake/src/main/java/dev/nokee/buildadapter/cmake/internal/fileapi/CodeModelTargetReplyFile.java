package dev.nokee.buildadapter.cmake.internal.fileapi;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.With;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

@Value
public class CodeModelTargetReplyFile {
	File replyFile;
	@With @Getter(AccessLevel.NONE) CodeModelTarget content;

	public CodeModelTarget get() {
		return content;
	}

	public static CodeModelTargetReplyFile of(File replyFile) {
		return new CodeModelTargetReplyFile(replyFile, load(replyFile));
	}

	public static CodeModelTarget load(File replyFile) {
		try {
			return new Gson().fromJson(FileUtils.readFileToString(replyFile, Charset.defaultCharset()), CodeModelTarget.class);
		} catch (IOException e) {
			throw new UncheckedIOException(e); // TODO
		}
	}
}
