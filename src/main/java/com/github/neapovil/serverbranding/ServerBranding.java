package com.github.neapovil.serverbranding;

import java.io.IOException;
import java.nio.file.Files;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.papermc.paper.network.ChannelInitializeListener;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;

public class ServerBranding extends JavaPlugin
{
    private static ServerBranding instance;
    private Config config;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onLoad()
    {
        ChannelInitializeListenerHolder.addListener(new NamespacedKey(this, "listener"), new ChannelInitializeListener() {
            @Override
            public void afterInitChannel(@NonNull Channel channel)
            {
                channel.pipeline().addBefore("packet_handler", "plugin-serverbranding", new CustomHandler());
            }
        });
    }

    @Override
    public void onEnable()
    {
        instance = this;

        this.saveResource("config.json", false);

        this.load();

        new CommandAPICommand("serverbranding")
                .withPermission("serverbranding.command")
                .withArguments(new LiteralArgument("set"))
                .withArguments(new GreedyStringArgument("brand"))
                .executes((sender, args) -> {
                    final String brand = (String) args.get("brand");

                    try
                    {
                        this.config.brandName = brand;
                        this.save();
                        sender.sendMessage(Component.text("Server brand changed to: ").append(LegacyComponentSerializer.legacyAmpersand().deserialize(brand)));
                    }
                    catch (IOException e)
                    {
                        this.getLogger().severe(e.getMessage());
                        throw CommandAPI.failWithString("Unable to save the new server brand.");
                    }
                })
                .register();
    }

    @Override
    public void onDisable()
    {
    }

    public static ServerBranding instance()
    {
        return instance;
    }

    private void load()
    {
        try
        {
            final String string = Files.readString(this.getDataFolder().toPath().resolve("config.json"));
            this.config = this.gson.fromJson(string, Config.class);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void save() throws IOException
    {
        final String string = this.gson.toJson(this.config);
        Files.write(this.getDataFolder().toPath().resolve("config.json"), string.getBytes());
    }

    class CustomHandler extends ChannelDuplexHandler
    {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception
        {
            if (!(msg instanceof ClientboundCustomPayloadPacket packet))
            {
                super.write(ctx, msg, promise);
                return;
            }

            if (!packet.getIdentifier().equals(ClientboundCustomPayloadPacket.BRAND))
            {
                super.write(ctx, msg, promise);
                return;
            }

            final FriendlyByteBuf brand = new FriendlyByteBuf(Unpooled.buffer()).writeUtf(ChatColor.translateAlternateColorCodes('&', config.brandName));
            final ClientboundCustomPayloadPacket packet1 = new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.BRAND, brand);

            super.write(ctx, packet1, promise);
        }
    }

    class Config
    {
        public String brandName;
    }
}
