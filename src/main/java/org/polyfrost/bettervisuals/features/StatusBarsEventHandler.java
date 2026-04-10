package org.polyfrost.bettervisuals.features;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.polyfrost.bettervisuals.config.BetterVisualsConfig;

import java.util.EnumSet;

public class StatusBarsEventHandler {

    private static final EnumSet<RenderGameOverlayEvent.ElementType> SUPPRESSED = EnumSet.of(
            RenderGameOverlayEvent.ElementType.HEALTH,
            RenderGameOverlayEvent.ElementType.ARMOR,
            RenderGameOverlayEvent.ElementType.FOOD,
            RenderGameOverlayEvent.ElementType.AIR,
            RenderGameOverlayEvent.ElementType.EXPERIENCE
    );

    @SubscribeEvent
    public void onPre(RenderGameOverlayEvent.Pre event) {
        if (!BetterVisualsConfig.statusBarsEnabled) return;
        if (!SUPPRESSED.contains(event.type)) return;

        event.setCanceled(true);

        // Render our custom bars once (on HEALTH event, which fires first)
        if (event.type == RenderGameOverlayEvent.ElementType.HEALTH) {
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            StatusBarsRenderer.INSTANCE.render();
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
    }
}
