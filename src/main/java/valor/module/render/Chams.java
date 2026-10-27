package valor.module.render;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import valor.event.EventTarget;
import valor.events.RenderLivingEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.util.BotCheckUtil;

public class Chams extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();

   public final BooleanProperty playersProp = new BooleanProperty("players", Boolean.TRUE);
   public final BooleanProperty friendsProp = new BooleanProperty("friends", Boolean.TRUE);
   public final BooleanProperty enemiesProp = new BooleanProperty("enemies", Boolean.TRUE);
   public final BooleanProperty bossesProp = new BooleanProperty("bosses", Boolean.FALSE);
   public final BooleanProperty mobsProp = new BooleanProperty("mobs", Boolean.FALSE);
   public final BooleanProperty creepersProp = new BooleanProperty("creepers", Boolean.FALSE);
   public final BooleanProperty endermenProp = new BooleanProperty("endermen", Boolean.FALSE);
   public final BooleanProperty blazesProp = new BooleanProperty("blazes", Boolean.FALSE);
   public final BooleanProperty animalsProp = new BooleanProperty("animals", Boolean.FALSE);
   public final BooleanProperty selfProp = new BooleanProperty("self", Boolean.FALSE);
   public final BooleanProperty botsProp = new BooleanProperty("bots", Boolean.FALSE);

   @EventTarget
   public void onRenderLiving(RenderLivingEvent event) {
      if (this.isEnabled()) {
         EntityLivingBase entity = event.getEntity();
         if (entity.deathTime > 0) {
            return;
         }
         if (mc.getRenderViewEntity().getDistanceToEntity(entity) > 512.0F) {
            return;
         }

         boolean shouldRender = false;
         if (entity instanceof EntityPlayer) {
            if (entity != mc.thePlayer && entity != mc.getRenderViewEntity()) {
               if (BotCheckUtil.isBot((EntityPlayer)entity)) {
                  shouldRender = (Boolean)this.botsProp.getValue();
               } else if (BotCheckUtil.isFriend((EntityPlayer)entity)) {
                  shouldRender = (Boolean)this.friendsProp.getValue();
               } else if (BotCheckUtil.isTarget((EntityPlayer)entity)) {
                  shouldRender = (Boolean)this.enemiesProp.getValue();
               } else {
                  shouldRender = (Boolean)this.playersProp.getValue();
               }
            } else {
               shouldRender = (Boolean)this.selfProp.getValue() && mc.gameSettings.thirdPersonView != 0;
            }
         } else if (entity instanceof EntityDragon || entity instanceof EntityWither) {
            shouldRender = !entity.isInvisible() && (Boolean)this.bossesProp.getValue();
         } else if (entity instanceof EntityMob || entity instanceof EntitySlime) {
            if (entity instanceof EntityCreeper) {
               shouldRender = (Boolean)this.creepersProp.getValue();
            } else if (entity instanceof EntityEnderman) {
               shouldRender = (Boolean)this.endermenProp.getValue();
            } else if (entity instanceof EntityBlaze) {
               shouldRender = (Boolean)this.blazesProp.getValue();
            } else {
               shouldRender = (Boolean)this.mobsProp.getValue();
            }
         } else if (entity instanceof EntityAnimal || entity instanceof EntityBat || entity instanceof EntitySquid || entity instanceof EntityVillager) {
            shouldRender = (Boolean)this.animalsProp.getValue();
         }

         if (shouldRender) {
            switch (event.getType()) {
               case PRE:
                  GL11.glEnable(32823);
                  GL11.glPolygonOffset(1.0F, -2500000.0F);
                  return;
               case POST:
                  GL11.glPolygonOffset(1.0F, 2500000.0F);
                  GL11.glDisable(32823);
            }
         }
      }
   }

   public Chams() {
      super("Chams", false, ModuleCategory.VISUAL);
   }
}
