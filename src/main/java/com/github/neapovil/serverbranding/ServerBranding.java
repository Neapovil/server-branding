package com.github.neapovil.serverbranding;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.electronwill.nightconfig.core.file.FileConfig;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.papermc.paper.network.ChannelInitializeListener;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;

public class ServerBranding extends JavaPlugin
{
    private static ServerBranding instance;
    private FileConfig config;

    @Override
    public void onLoad()
    {
        ChannelInitializeListenerHolder.addListener(new NamespacedKey(this, "listener"), new ChannelInitializeListener() {
            @Override
            public void afterInitChannel(@NonNull Channel channel)
            {
                channel.pipeline().addBefore("packet_handler", "serverbranding", new CustomHandler());
            }
        });
    }

    @Override
    public void onEnable()
    {
        instance = this;

        this.saveResource("config.json", false);

        this.config = FileConfig.builder(this.getDataFolder().toPath().resolve("config.json"))
                .autoreload()
                .autosave()
                .build();

        this.config.load();
    }

    @Override
    public void onDisable()
    {
    }

    public static ServerBranding getInstance()
    {
        return instance;
    }

    class CustomHandler extends ChannelDuplexHandler
    {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception
        {
            if (!(msg instanceof ClientboundCustomPayloadPacket))
            {
                super.write(ctx, msg, promise);
                return;
            }

            final ClientboundCustomPayloadPacket packet = (ClientboundCustomPayloadPacket) msg;

            if (!packet.getIdentifier().equals(ClientboundCustomPayloadPacket.BRAND))
            {
                super.write(ctx, msg, promise);
                return;
            }

            final FriendlyByteBuf brand = new FriendlyByteBuf(Unpooled.buffer()).writeUtf((String) config.get("brandName"));
            final ClientboundCustomPayloadPacket packet1 = new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.BRAND, brand);

            super.write(ctx, packet1, promise);
        }
    }
}
