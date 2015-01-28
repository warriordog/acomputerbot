package net.acomputerdog.ircbot.plugin;

import java.util.*;

public class PluginList {
    private final Map<String, IrcPlugin> pluginMap = new HashMap<>();

    public void addPlugin(IrcPlugin plugin) {
        if (pluginMap.containsKey(plugin.getID())) {
            throw new IllegalStateException("Duplicate plugin with ID " + plugin.getID());
        }
        pluginMap.put(plugin.getID(), plugin);
    }

    public IrcPlugin getPlugin(String plugin) {
        return pluginMap.get(plugin);
    }

    public void removePlugin(String plugin) {
        pluginMap.remove(plugin);
    }

    public void removePlugin(IrcPlugin plugin) {
        removePlugin(plugin.getID());
    }

    public void clear() {
        pluginMap.clear();
    }

    public Collection<IrcPlugin> getPlugins() {
        return Collections.unmodifiableCollection(pluginMap.values());
    }

    public Set<String> getPluginNames() {
        return Collections.unmodifiableSet(pluginMap.keySet());
    }

    public int size() {
        return pluginMap.size();
    }

}
