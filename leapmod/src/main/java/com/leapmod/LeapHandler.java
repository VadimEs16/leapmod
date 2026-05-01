package com.leapmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class LeapHandler {

    private static final double LEAP_HORIZONTAL = 2.8;
    private static final double LEAP_VERTICAL   = 0.8;
    private static final int    COOLDOWN_TICKS  = 25;
    private static final int    DOUBLE_TAP_WINDOW = 8;

    private boolean prevCtrlDown = false;
    private int lastCtrlPressTick = -999;
    private int cooldownUntilTick = 0;
    private int currentTick = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.level == null || mc.isPaused()) return;

        currentTick++;

        // Перевіряємо саме клавішу Ctrl — не стан спринту
        long window = mc.getWindow().getWindow();
        boolean ctrlDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;

        // Edge trigger — момент натискання Ctrl
        if (ctrlDown && !prevCtrlDown) {
            int gap = currentTick - lastCtrlPressTick;

            if (gap > 1 && gap <= DOUBLE_TAP_WINDOW) {
                if (currentTick >= cooldownUntilTick) {
                    performLeap(player);
                    cooldownUntilTick = currentTick + COOLDOWN_TICKS;
                    lastCtrlPressTick = -999;
                }
            } else {
                lastCtrlPressTick = currentTick;
            }
        }

        prevCtrlDown = ctrlDown;
    }

    private void performLeap(LocalPlayer player) {
        float yaw = (float) Math.toRadians(player.getYRot());
        double dx = -Math.sin(yaw) * LEAP_HORIZONTAL;
        double dy = LEAP_VERTICAL;
        double dz =  Math.cos(yaw) * LEAP_HORIZONTAL;

        Vec3 current = player.getDeltaMovement();
        player.setDeltaMovement(current.x + dx, dy, current.z + dz);
        player.hasImpulse = true;
    }
}
