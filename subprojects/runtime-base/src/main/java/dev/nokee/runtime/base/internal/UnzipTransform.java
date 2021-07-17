package dev.nokee.runtime.base.internal;

import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.apache.commons.io.FilenameUtils.removeExtension;

/**
 * Provides a generic transform from a zipped file to an extracted directory.  The extracted directory
 * is located in the output directory of the transform and is named after the zipped file name
 * minus the extension.
 */
public abstract class UnzipTransform implements TransformAction<TransformParameters.None> {

    @PathSensitive(PathSensitivity.NAME_ONLY)
    @InputArtifact
    public abstract Provider<FileSystemLocation> getZippedFile();

    @Override
    public void transform(TransformOutputs outputs) {
        File zippedFile = getZippedFile().get().getAsFile();
        String unzippedDirName = removeExtension(zippedFile.getName());
        File unzipDir = outputs.dir(unzippedDirName);
        try {
            unzipTo(zippedFile, unzipDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void unzipTo(File headersZip, File unzipDir) throws IOException {
        try (ZipInputStream inputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(headersZip)))) {
            ZipEntry entry;
            while ((entry = inputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                File outFile = new File(unzipDir, entry.getName());
                Files.createParentDirs(outFile);
                try (FileOutputStream outputStream = new FileOutputStream(outFile)) {
                    IOUtils.copyLarge(inputStream, outputStream);
                }
            }
        }
    }
}
