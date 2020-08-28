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
public class CodeModelReplyFile {
	File replyFile;
	@With @Getter(AccessLevel.NONE) CodeModel content;

	public CodeModel get() {
		return content;
	}

	public static CodeModelReplyFile of(File replyFile) {
		return new CodeModelReplyFile(replyFile, load(replyFile));
	}

	public static CodeModel load(File replyFile) {
		try {
			return new Gson().fromJson(FileUtils.readFileToString(replyFile, Charset.defaultCharset()), CodeModel.class);
		} catch (IOException e) {
			throw new UncheckedIOException(e); // TODO
		}
	}
}
