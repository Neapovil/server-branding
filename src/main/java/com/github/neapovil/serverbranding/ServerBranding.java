package com.github.neapovil.serverbranding;

import org.bukkit.plugin.java.JavaPlugin;

public class ServerBranding extends JavaPlugin
{
    private static ServerBranding instance;

    @Override
    public void onEnable()
    {
        instance = this;
    }

    @Override
    public void onDisable()
    {
    }

    public static ServerBranding getInstance()
    {
        return instance;
    }
}
