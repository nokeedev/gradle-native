package dev.nokee.platform.jni.fixtures

import dev.gradleplugins.test.fixtures.sources.NativeSourceElement
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFileElement
import dev.gradleplugins.test.fixtures.sources.java.JavaPackage
import dev.gradleplugins.test.fixtures.sources.java.JavaSourceElement
import dev.gradleplugins.test.fixtures.sources.java.JavaSourceFileElement
import dev.gradleplugins.test.fixtures.sources.objectivec.ObjectiveCSourceElement
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement

import static dev.gradleplugins.test.fixtures.sources.SourceFileElement.ofFile
import static dev.gradleplugins.test.fixtures.sources.java.JavaSourceElement.ofPackage

class JavaJniObjectiveCNSSavePanelLib extends JniLibraryElement {
	final ObjectiveCNSSavePanelJniBinding nativeBindings
	final JavaSourceElement jvmBindings
	final JavaSourceElement jvmImplementation

	@Override
	SourceElement getJvmSources() {
		return ofElements(jvmBindings, jvmImplementation)
	}

	@Override
	NativeSourceElement getNativeSources() {
		return ofNativeElements(nativeBindings);
	}

	JavaJniObjectiveCNSSavePanelLib(String projectName) {
		def javaPackage = ofPackage('com.example.cocoa')
		String sharedLibraryBaseName = projectName
		jvmBindings = new JavaNativeNSSavePanel(javaPackage, sharedLibraryBaseName)
		nativeBindings = new ObjectiveCNSSavePanelJniBinding(javaPackage)

		jvmImplementation = new JavaNativeLoader(javaPackage);
	}
}

class JavaNativeNSSavePanel extends JavaSourceFileElement {
	private final SourceFileElement source
	private final JavaPackage javaPackage
	private final String sharedLibraryBaseName
	private final String resourcePath

	@Override
	SourceFileElement getSource() {
		return source
	}

	JavaNativeNSSavePanel(JavaPackage javaPackage, String sharedLibraryBaseName, String resourcePath = '') {
		this.javaPackage = javaPackage
		this.sharedLibraryBaseName = sharedLibraryBaseName
		this.resourcePath = resourcePath
		source = ofFile(sourceFile("java/${javaPackage.directoryLayout}", 'NSSavePanel.java', """
package ${javaPackage.name};

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;

public class NSSavePanel {

    static {
        NativeLoader.loadLibrary(NSSavePanel.class.getClassLoader(), "${resourcePath}${sharedLibraryBaseName}");
    }

    public native String saveDialog(String title, String extension);
}
"""))
	}

	JavaNativeNSSavePanel withSharedLibraryBaseName(String sharedLibraryBaseName) {
		return new JavaNativeNSSavePanel(javaPackage, sharedLibraryBaseName, resourcePath)
	}

	JavaNativeNSSavePanel withResourcePath(String resourcePath) {
		return new JavaNativeNSSavePanel(javaPackage, sharedLibraryBaseName, resourcePath)
	}
}

class ObjectiveCNSSavePanelJniBinding extends ObjectiveCSourceElement {
	private final source
	private final JavaPackage javaPackage

	@Override
	SourceElement getHeaders() {
		return empty()
	}

	@Override
	SourceElement getSources() {
		return source
	}

	ObjectiveCNSSavePanelJniBinding(JavaPackage javaPackage) {
		this.javaPackage = javaPackage
		source = ofFiles(sourceFile('objc', 'ns_save_panel.m', """
#include "${javaPackage.jniHeader('NSSavePanel')}"

#import "JavaNativeFoundation/JavaNativeFoundation.h"
#import "AppKit/AppKit.h"

JNIEXPORT jstring JNICALL ${javaPackage.jniMethodName('NSSavePanel', 'saveDialog')}(JNIEnv * env, jobject self, jstring title, jstring extension) {
    // Obligatory opening to the JNI method.  Sets up an autorelease pool,
    // and rethrows Objective-C exceptions as Java exceptions.  (Paired
    // with JNF_COCOA_EXT(env) at end of method.
    JNF_COCOA_ENTER(env);

    // A jstring container for the output value
    jstring path = NULL;

    // Placeholder for the NSString path that will be set inside the block
    __block NSString *nsPath = Nil;;

    // Copy the title to an NSString so it an be used safely inside the block
    // even if it is on a different thread
    NSString *nsTitle = JNFJavaToNSString(env, title);

    // Copy the extension into an NSString so it can be used safely inside
    // the block even if it is on a different thread
    NSString *cocoaExtension = JNFJavaToNSString(env, extension);


    // Create a block for the code that will create and interact with
    // the NSSavePanel so that it can be run on a different thread.  All
    // interaction with the NSSavePanel class needs to be on the main application
    // thread, so if this method is accessed on a different thread (e.g.
    // the AWT event thread, we'll need to block and run this code on the
    // main application thread.
    void (^block)(void);
    block = ^(void){
        // This block's code must ONLY ever be run on the main
        // application thread.

        NSSavePanel *panel = [NSSavePanel savePanel];
        NSArray *types = [NSArray arrayWithObjects: cocoaExtension,nil];
        [panel setAllowedFileTypes: types];
        [panel setCanSelectHiddenExtension:TRUE];
        [panel setExtensionHidden:TRUE];
        [panel setTitle: nsTitle];
        if ( [panel runModal] == NSFileHandlingPanelOKButton ){
            // The user clicked OK in the file save dialog, so we
            // now save the user's file path selection in the nsPath.
            NSURL * out = [[panel URL] filePathURL];

            // Set the nsPath so that it can be accessed outside this
            // block after it is run.  We call retain on the string
            // so that it won't be destroyed after the block is
            // finished executing.
            nsPath = [[out path] retain];
        }
    };

    // Check if this is already running on the main thread.
    if ( [NSThread isMainThread]){
        // We are on the main thread, so we can execute the block directly.
        block();
    } else {
        // We are not on the main thread so we need to run the block on the
        // main thread, and wait for it to complete.
        [JNFRunLoop performOnMainThreadWaiting:YES withBlock:block];
    }


    if ( nsPath != nil ){
        // Since nsPath is Not nil, it looks like the user chose a file
        // Copy the NSString path back to the jstring to be returned
        // from the method.
        path = JNFNSToJavaString(env, nsPath);

        // Release the nsPath to prevent memory leak.
        [nsPath release];
    }

    // Return the path.  This may be null
    return path;

    // Matching the opening JNF_COCOA_ENTER(env) at the beginning of the method.
    JNF_COCOA_EXIT(env);

    // It is necessary to return NULL here in case there was some failure or
    // exception that prevented us from reaching the return statements inside
    // the JNF_COCOA_ENTER/EXIT region.
    return NULL;
}
"""))
	}
}
