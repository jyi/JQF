/*
 * Copyright (c) 2017-2018 The Regents of the University of California
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.cs.jqf.instrument;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import kr.ac.unist.cse.jqf.Log;

import janala.instrument.SnoopInstructionTransformer;
import org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter;

/**
 * @author Rohan Padhye
 */
public class InstrumentingClassLoader extends URLClassLoader {

    private final URL[] urls;
    private Set<String> instrumented = new HashSet<>();
    private ClassFileTransformer transformer = new SnoopInstructionTransformer();

    public InstrumentingClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.urls = urls;
    }

    public InstrumentingClassLoader(String[] paths, ClassLoader parent) throws MalformedURLException {
        this(stringsToUrls(paths), parent);
    }

    public static URL[] stringsToUrls(String[] paths) throws MalformedURLException {
        URL[] urls = new URL[paths.length];
        for (int i = 0; i < paths.length; i++) {
            urls[i] = new File(paths[i]).toURI().toURL();
        }
        return urls;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        String path = null;
        InputStream is = null;
        URLConnection connection = null;
        for (URL url : this.urls) {
            try {
                path = url + name;
                connection = new URL(path).openConnection();
                is = connection.getInputStream();
            } catch (FileNotFoundException e) {
                // skip
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (is != null) {
            return is;
        } else {
            return super.getResourceAsStream(name);
        }
    }

    public void instrumentClass(String name) throws ClassNotFoundException {
        if (!instrumented.contains(name)) {
            findClass(name);
            instrumented.add(name);
        }
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes;

        // Try to read the class file in as a resource
        String internalName = name.replace('.', '/');
        String path = internalName.concat(".class");
        try (InputStream in = getResourceAsStream(path)) {
            if (in == null) {
                throw new ClassNotFoundException("Cannot find class " + name);
            }
            BufferedInputStream buf = new BufferedInputStream(in);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b;
            while ((b = buf.read()) != -1) {
                baos.write(b);
            }
            bytes = baos.toByteArray();
        } catch (IOException e) {
            throw new ClassNotFoundException("I/O exception while loading class.", e);
        }

        assert (bytes != null);

        byte[] transformedBytes;
        try {
            infoLog("Transform: " + name);
            transformedBytes = transformer.transform(this, internalName, null, null, bytes);

            // additional transformation to dump program states
            ClassPreProcessorAgentAdapter adapter = new ClassPreProcessorAgentAdapter();
            // TODO: should we use this or this.getParent() as the first parmater of transform?
            transformedBytes = adapter.transform(this, internalName, null, null,
                    transformedBytes != null ? transformedBytes : bytes);
        } catch (IllegalClassFormatException e) {
            // Just use original bytes
            transformedBytes = null;
        }

        // Load the class with transformed bytes, if possible
        if (transformedBytes != null) {
            bytes = transformedBytes;
        }

        return defineClass(name, bytes,
                0, bytes.length);
    }

    public void infoLog(String str, Object... args) {
        if (Log.verbose) {
            String line = String.format(str, args);
            if (Log.logFile != null) {
                appendLineToFile(Log.logFile, line);
            } else {
                System.err.println(line);
            }
        }
    }

    protected void appendLineToFile(File file, String line) {
        try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
            out.println(line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
