package net.acomputerdog.ircbot.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginLoader {

    private final PluginList pluginList;

    private final boolean recursive;

    public PluginLoader(boolean recursive) {
        this.recursive = recursive;
        pluginList = new PluginList();
    }

    public PluginLoader() {
        this(false);
    }

    public PluginList getPlugins() {
        return pluginList;
    }

    public void loadPlugins(File... files) throws MalformedURLException {
        List<PluginLoc> urls = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) { //needed so that a top-level directory can be scanned in non-recursive mode
                File[] subFiles = file.listFiles();
                if (subFiles != null && subFiles.length > 0) {
                    for (File subFile : subFiles) {
                        scanPath(urls, subFile);
                    }
                }
            } else {
                scanPath(urls, file);
            }
        }
        loadPlugins(urls);
    }

    private void scanPath(List<PluginLoc> urls, File path) throws MalformedURLException {
        if (path.isDirectory()) {
            addDirectory(urls, path);
        } else if (path.isFile()) {
            addFile(urls, path);
        }
    }

    private void addDirectory(List<PluginLoc> urls, File dir) throws MalformedURLException {
        if (recursive) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    scanPath(urls, file);
                }
            }
        }
    }

    private void addFile(List<PluginLoc> urls, File file) {
        if (file.isFile() && file.getPath().endsWith(".jar")) {
            try {
                ZipFile zip = new ZipFile(file);
                zip.stream().filter(e -> e.getName().contains("IrcPlugin") && e.getName().contains(".class")).forEach(e -> addEntry(urls, file, e));
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
    }

    private void addEntry(List<PluginLoc> locs, File file, ZipEntry entry) {
        try {
            String entryName = entry.getName();
            locs.add(new PluginLoc(file.toURI().toURL(), entry.getName().substring(0, entryName.length() - 6).replace('/', '.')));
        } catch (MalformedURLException ignored) {
            ignored.printStackTrace();
        }
    }

    private void loadPlugins(List<PluginLoc> locs) {
        URL[] urls = new URL[locs.size()];
        String[] strings = new String[locs.size()];
        for (int index = 0; index < locs.size(); index++) {
            PluginLoc loc = locs.get(index);
            urls[index] = loc.url;
            strings[index] = loc.className;
        }
        URLClassLoader loader = new URLClassLoader(urls);
        for (String str : strings) {
            try {
                IrcPlugin plugin = (IrcPlugin) loader.loadClass(str).newInstance();
                pluginList.addPlugin(plugin);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }

    private static class PluginLoc {
        private final URL url;
        private final String className;

        private PluginLoc(URL url, String className) {
            this.url = url;
            this.className = className;
        }
    }
}
