package dev.nokee.buildadapter.cmake.internal.fileapi;

import java.io.File;

public interface CodeModelClient {
	CodeModelReplyFiles query(File baseDirectory);
}
