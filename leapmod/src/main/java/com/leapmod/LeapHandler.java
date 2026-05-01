package com.leapmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class LeapHandler {

    // Config
    private static final double LEAP_HORIZONTAL = 2.8;  // сила вперед
    private static final double LEAP_VERTICAL   = 0.8;  // сила вгору
    private static final int    COOLDOWN_TICKS  = 25;   // тіків між стрибками (25 = ~1.25 сек)
    private static final int    DOUBLE_TAP_WINDOW = 8;  // тіків для подвійного натискання

    private boolean prevSprinting = false;
    private int lastSprintStartTick = -999;
    private int cooldownUntilTick   = 0;
    private int currentTick         = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.level == null) return;
        if (mc.isPaused()) return;

        currentTick++;

        boolean sprinting = player.isSprinting();

        // Edge trigger — момент ПОЧАТКУ спринту
        if (sprinting && !prevSprinting) {
            int gap = currentTick - lastSprintStartTick;

            if (gap > 1 && gap <= DOUBLE_TAP_WINDOW) {
                // Подвійний Ctrl — leap!
                if (currentTick >= cooldownUntilTick) {
                    performLeap(player);
                    cooldownUntilTick = currentTick + COOLDOWN_TICKS;
                    lastSprintStartTick = -999; // скидаємо
                }
            } else {
                lastSprintStartTick = currentTick;
            }
        }

        prevSprinting = sprinting;
    }

    private void performLeap(LocalPlayer player) {
        // Отримуємо напрямок погляду камери (горизонтальний)
        float yaw = (float) Math.toRadians(player.getYRot());

        double dx = -Math.sin(yaw) * LEAP_HORIZONTAL;
        double dy = LEAP_VERTICAL;
        double dz =  Math.cos(yaw) * LEAP_HORIZONTAL;

        // Додаємо до поточної швидкості (не замінюємо)
        Vec3 current = player.getDeltaMovement();
        player.setDeltaMovement(
            current.x + dx,
            dy,  // вертикаль замінюємо повністю (щоб не накопичувалась)
            current.z + dz
        );

        // Повідомляємо сервер про нову швидкість
        player.hasImpulse = true;
    }
}
