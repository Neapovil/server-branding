package com.github.neapovil.serverbranding;

import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.packetlistener.PacketListenerAPI;
import org.inventivetalent.packetlistener.handler.PacketHandler;
import org.inventivetalent.packetlistener.handler.ReceivedPacket;
import org.inventivetalent.packetlistener.handler.SentPacket;

import com.electronwill.nightconfig.core.file.FileConfig;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutCustomPayload;

public class ServerBranding extends JavaPlugin
{
    private static ServerBranding instance;
    private FileConfig config;

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

        PacketListenerAPI.addPacketHandler(new PacketHandler(this) {
            @Override
            public void onReceive(ReceivedPacket event)
            {
            }

            @Override
            public void onSend(SentPacket event)
            {
                if (!event.getPacketName().equals("PacketPlayOutCustomPayload"))
                {
                    return;
                }

                final PacketPlayOutCustomPayload packet = (PacketPlayOutCustomPayload) event.getPacket();

                // minecraft:brand
                if (!packet.b().equals(PacketPlayOutCustomPayload.a))
                {
                    return;
                }

                final PacketPlayOutCustomPayload packet1 = new PacketPlayOutCustomPayload(PacketPlayOutCustomPayload.a,
                        new PacketDataSerializer(Unpooled.buffer()).a((String) config.get("brandName")));

                event.setPacket(packet1);
            }
        });
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
