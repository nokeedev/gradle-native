package dev.nokee.buildadapter.cmake.internal.fileapi;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

@Value
public class IndexReplyFile {
	File replyFile;
	@Getter(AccessLevel.NONE) Index content;

	public Index get() {
		return content;
	}

	public static IndexReplyFile of(File replyFile) {
		return new IndexReplyFile(replyFile, load(replyFile));
	}

	public static Index load(File replyFile) {
		try {
			return new Gson().fromJson(FileUtils.readFileToString(replyFile, Charset.defaultCharset()), Index.class);
		} catch (IOException e) {
			throw new UncheckedIOException(e); // TODO
		}
	}
}
