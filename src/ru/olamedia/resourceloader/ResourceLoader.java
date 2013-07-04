/**
 * The MIT License
 * 
 * Copyright (c) 2013 olamedia
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
package ru.olamedia.resourceloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Hashtable;

import com.sun.xml.internal.ws.util.ASCIIUtility;

public class ResourceLoader extends ClassLoader {
	private ArrayList<ClassLoader> classloaders = new ArrayList<ClassLoader>();
	private Hashtable<String, Class<?>> classes = new Hashtable<String, Class<?>>();
	private static ResourceLoader instance;

	public static ResourceLoader getInstance() {
		if (null == instance) {
			instance = new ResourceLoader();
		}
		return instance;
	}

	private ResourceLoader() {
		super(ResourceLoader.class.getClassLoader());
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return findClass(name);
	}

	@Override
	protected String findLibrary(String libname) {
		return super.findLibrary(libname);
	}

	public Class<?> findClass(String name) {
		final String filename = name.replace('.', File.separatorChar).concat(".class");
		// System.out.println("findClass(" + filename + ")");
		byte classByte[];
		Class<?> result = null;
		result = classes.get(name);
		if (result != null) {
			return result;
		}
		try {
			return findSystemClass(name);
		} catch (Exception e) {
		}
		try {
			InputStream in = getResourceAsStream(filename);
			classByte = ASCIIUtility.getBytes(in);
			result = defineClass(name, classByte, 0, classByte.length, null);
			classes.put(name, result);
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	public static void register() {
		getInstance().addLoader(Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(getInstance());
	}

	public void addLoader(ClassLoader loader) {
		classloaders.add(loader);
	}

	public void addURL(URL url) {
		addLoader(new URLClassLoader(new URL[] { url }));
	}

	public void addJar(File file) {
		try {
			addURL(new URL("jar:file://" + file.getCanonicalFile() + "!/"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addJar(String path) {
		try {
			addURL(new URL("jar:file://" + new File(path).getCanonicalFile() + "!/"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addPath(File file) {
		try {
			addURL(new URL("file://" + file.getCanonicalPath()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addPath(String path) {
		try {
			addURL(new URL("file://" + new File(path).getCanonicalPath()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public URL getResource(String name) {
		URL url = null;
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		url = stackTraceElements[stackTraceElements.length - 1].getClass().getResource(name);
		if (null != url) {
			return url;
		}
		for (ClassLoader loader : classloaders) {
			if (null != loader) {
				url = loader.getResource(name);
				if (null != url) {
					return url;
				}
			} else {
				System.err.println("null loader");
			}
		}
		url = ClassLoader.getSystemResource(name);
		if (null != url) {
			return url;
		}
		url = this.getClass().getResource(name);
		if (null != url) {
			return url;
		}
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream in = null;
		in = testGetResourceAsStream(name);
		if (null != in) {
			return in;
		}
		if (name.startsWith("/")) {
			return testGetResourceAsStreamMultipleNames(name.substring(1));
		} else if (name.startsWith("./")) {
			return testGetResourceAsStreamMultipleNames(name.substring(2));
		} else {
			return testGetResourceAsStreamMultipleNames(name);
		}
	}

	private InputStream testGetResourceAsStreamMultipleNames(String name) {
		InputStream in = null;
		in = testGetResourceAsStream(name);
		if (null == in) {
			in = testGetResourceAsStream("./" + name);
			if (null == in) {
				in = testGetResourceAsStream("/" + name);
			}
		}
		return in;
	}

	private InputStream testGetResourceAsStream(String name) {
		InputStream in = null;
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		in = stackTraceElements[stackTraceElements.length - 1].getClass().getResourceAsStream(name);
		if (null != in) {
			return in;
		}
		for (ClassLoader loader : classloaders) {
			if (null != loader) {
				in = loader.getResourceAsStream(name);
				if (null != in) {
					return in;
				}
			} else {
			}
		}
		in = ClassLoader.getSystemResourceAsStream(name);
		if (null != in) {
			return in;
		}
		in = this.getClass().getResourceAsStream(name);
		if (null != in) {
			return in;
		}
		return null;
	}
}
