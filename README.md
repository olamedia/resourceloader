Resource Loader
======

```java
ResourceLoader.register(); // will replace ClassLoader of the current Thread: Thread.currentThread().setContextClassLoader()
ResourceLoader loader = ResourceLoader.getInstance();
// usage examples:
loader.addURL(URL url)
loader.addPath(File file)
loader.addPath(String path)
loader.addJar(File file)
loader.addJar(String path)
// loading resources:
loader.getResourceAsStream(String name);
// or
Thread.currentThread().getContextClassLoader().getResourceAsStream(String name);
```
