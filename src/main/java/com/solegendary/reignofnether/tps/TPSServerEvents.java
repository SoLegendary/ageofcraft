package com.solegendary.reignofnether.tps;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TPSServerEvents {

    private static int updateTPSticks = 0;
    private static final int UPDATE_TPS_TICKS_MAX = 10;
    private static final long[] UNLOADED = new long[]{0L};

    // logic borrowed from net.minecraftforge.server.command.TPSCommand
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END)
            return;

        MinecraftServer server = evt.getServer();
        long[] times = server.getTickTime(Level.OVERWORLD);
        if (times == null)
            times = UNLOADED;

        double worldTickTime = (double)mean(times) * 1.0E-6;

        updateTPSticks += 1;
        if (updateTPSticks >= UPDATE_TPS_TICKS_MAX) {
            updateTPSticks = 0;
            TPSClientBoundPacket.updateTickTime(worldTickTime);
        }
    }

    private static long mean(long[] values) {
        long sum = 0L;
        int var4 = values.length;
        for (long v : values)
            sum += v;

        return sum / (long)values.length;
    }
}
