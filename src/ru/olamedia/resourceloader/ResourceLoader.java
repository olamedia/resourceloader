package ru.olamedia.resourceloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class ResourceLoader extends ClassLoader {
	private ArrayList<ClassLoader> classloaders = new ArrayList<ClassLoader>();
	private static ResourceLoader instance;
	public static ResourceLoader getInstance(){
		if (null == instance){
			instance = new ResourceLoader();
		}
		return instance;
	}
	/*private ResourceLoader(ClassLoader parent) {
		super(parent);
	}*/

	private ResourceLoader() {
		// super(ClassLoader.class.getClassLoader());
		// super(Thread.currentThread().getContextClassLoader());
	}
	public static void register(){
		getInstance().addLoader(Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(getInstance());
	}

	public void addLoader(ClassLoader loader) {
		classloaders.add(loader);
	}

	public void addURL(URL url) {
		addLoader(new URLClassLoader(new URL[] { url }));
	}
	public void addJar(File file){
		try {
			addURL(new URL("jar:file://" + file.getCanonicalFile() + "!/"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void addJar(String path){
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
