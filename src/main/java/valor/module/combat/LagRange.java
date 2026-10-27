package valor.module.combat;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import valor.Valor;
import valor.core.HudThemeManager;
import valor.event.EventTarget;
import valor.events.PacketEvent;
import valor.events.Render3DEvent;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.module.world.BedNuker;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;
import valor.util.BotCheckUtil;
import valor.util.ItemUtil;
import valor.util.ReflectionUtil;
import valor.util.Render3DUtil;
import valor.util.RotationUtil;

public class LagRange extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private int blinkTicks = -1;
   private long lastAttackTimeMs = 0L;
   private boolean isBlinking = false;
   private Vec3 renderPos = null;
   private Vec3 renderLastPos = null;
   public final IntProperty delayProp = new IntProperty("delay", 150, 0, 1000);
   public final FloatProperty rangeProp = new FloatProperty("range", 10.0F, 3.0F, 100.0F);
   public final BooleanProperty weaponsOnlyProp = new BooleanProperty("weapons-only", Boolean.TRUE);
   public final BooleanProperty allowToolsProp = new BooleanProperty("allow-tools", Boolean.FALSE, this.weaponsOnlyProp::getValue);
   public final BooleanProperty botCheckProp = new BooleanProperty("bot-check", Boolean.TRUE);
   public final BooleanProperty teamsProp = new BooleanProperty("teams", Boolean.TRUE);
   public final ModeProperty showPositionProp = new ModeProperty("show-position", 0, new String[]{"NONE", "DEFAULT", "THEME"});

@Override
   public String[] getSuffix() {
      return new String[]{String.format("%dms", this.delayProp.getValue())};
   }

   @EventTarget
   public void onPacket(PacketEvent var1) {
      Packet var2;
      ItemStack var3;
      if (this.isEnabled()
         && (
            (var2 = var1.getPacket()) instanceof C02PacketUseEntity
               ? true
               : (
                  var2 instanceof C07PacketPlayerDigging
                     ? ((C07PacketPlayerDigging)var2).getStatus() != Action.RELEASE_USE_ITEM
                     : (
                        var2 instanceof C08PacketPlayerBlockPlacement
                           ? (var3 = ((C08PacketPlayerBlockPlacement)var2).getStack()) == null || !(var3.getItem() instanceof ItemSword)
                           : false
                     )
               )
         )) {
         Valor.packetBlinkManager.delayTicks = 0;
         this.blinkTicks = -1;
      }
   }

   @EventTarget(1)
   public void onRender3D(Render3DEvent var1) {
      if (this.isEnabled()
         && (Integer)this.showPositionProp.getValue() != 0
         && mc.gameSettings.thirdPersonView != 0
         && this.isBlinking
         && this.renderPos != null
         && this.renderLastPos != null) {
         Color var2 = new Color(-1);
         switch (this.showPositionProp.getValue()) {
            case 1:
               var2 = BotCheckUtil.getTeamColor(mc.thePlayer, 1.0F);
               break;
            case 2:
               var2 = HudThemeManager.getInstance().getColor(System.currentTimeMillis(), 0L);
         }

         double var3 = Render3DUtil.interpolate(this.renderLastPos.xCoord, this.renderPos.xCoord, (double)var1.getPartialTicks());
         double var5 = Render3DUtil.interpolate(this.renderLastPos.yCoord, this.renderPos.yCoord, (double)var1.getPartialTicks());
         double var7 = Render3DUtil.interpolate(this.renderLastPos.zCoord, this.renderPos.zCoord, (double)var1.getPartialTicks());
         float var9 = mc.thePlayer.getCollisionBorderSize();
         AxisAlignedBB var10 = new AxisAlignedBB(
               var3 - (double)mc.thePlayer.width / 2.0,
               var5,
               var7 - (double)mc.thePlayer.width / 2.0,
               var3 + (double)mc.thePlayer.width / 2.0,
               var5 + (double)mc.thePlayer.height,
               var7 + (double)mc.thePlayer.width / 2.0
            )
            .expand((double)var9, (double)var9, (double)var9)
            .offset(-ReflectionUtil.getRenderPosX(mc.getRenderManager()), -ReflectionUtil.getRenderPosY(mc.getRenderManager()), -ReflectionUtil.getRenderPosZ(mc.getRenderManager()));
         Render3DUtil.setupRender3D();
         Render3DUtil.drawFilledBox(var10, var2.getRed(), var2.getGreen(), var2.getBlue());
         Render3DUtil.endRender3D();
      }
   }

   private boolean isValidTarget(EntityPlayer var1) {
      if (var1 == mc.thePlayer || var1 == mc.thePlayer.ridingEntity) {
         return false;
      } else if (var1 == mc.getRenderViewEntity() || var1 == mc.getRenderViewEntity().ridingEntity) {
         return false;
      } else if (var1.deathTime > 0) {
         return false;
      } else {
         return BotCheckUtil.isFriend(var1) ? false : (!(Boolean)this.teamsProp.getValue() || !BotCheckUtil.isTeammate(var1)) && (!(Boolean)this.botCheckProp.getValue() || !BotCheckUtil.isBot(var1));
      }
   }

   @Override
   public void onDisabled() {
      Valor.packetBlinkManager.delayTicks = 0;
      this.blinkTicks = -1;
      this.lastAttackTimeMs = 0L;
      this.isBlinking = false;
      this.renderPos = null;
      this.renderLastPos = null;
   }

   @EventTarget(3)
   public void onTick(TickEvent var1) {
      if (this.isEnabled()) {
         switch (var1.getType()) {
            case PRE:
               Valor.packetBlinkManager.delayTicks = 0;
               this.isBlinking = false;
               BedNuker var13;
               if ((!(var13 = (BedNuker)Valor.moduleManager.modules.get(BedNuker.class)).isEnabled() || !var13.isReady())
                  && !ReflectionUtil.isHittingBlock(mc.playerController)
                  && (!mc.thePlayer.isUsingItem() || mc.thePlayer.isBlocking())
                  && (!(Boolean)this.weaponsOnlyProp.getValue() || ItemUtil.isHoldingWeapon() || (Boolean)this.allowToolsProp.getValue() && ItemUtil.isHoldingTool())) {
                  List var14;
                  if ((var14 = mc.theWorld
                        .loadedEntityList
                        .stream()
                        .filter(var0 -> var0 instanceof EntityPlayer)
                        .map(var0 -> (EntityPlayer)var0)
                        .filter(this::isValidTarget)
                        .collect(Collectors.toList()))
                     .isEmpty()) {
                     this.blinkTicks = -1;
                     return;
                  }

                  double var4 = (double)mc.thePlayer.getEyeHeight();
                  Vec3 var2 = Valor.packetBlinkManager.startPos.addVector(0.0, var4, 0.0);
                  Vec3 var3 = new Vec3(mc.thePlayer.lastTickPosX, mc.thePlayer.lastTickPosY + var4, mc.thePlayer.lastTickPosZ);
                  Vec3 var16 = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + var4, mc.thePlayer.posZ);
                  Iterator var15 = var14.iterator();

                  while (var15.hasNext()) {
                     EntityPlayer var5;
                     double var6;
                     if (!((var6 = RotationUtil.getDistanceToEntityBox(var5 = (EntityPlayer)var15.next(), var16)) > (double)((Float)this.rangeProp.getValue()).floatValue())) {
                        double var8 = RotationUtil.getDistanceToEntityBox(var5, var3);
                        double var10 = RotationUtil.getDistanceToEntityBox(var5, var2);
                        if (var6 < var8 || var6 < var10) {
                           if (this.blinkTicks < 0) {
                              this.blinkTicks = 0;

                              for (this.lastAttackTimeMs = this.lastAttackTimeMs + (long)((Integer)this.delayProp.getValue()).intValue(); this.lastAttackTimeMs > 0L; this.lastAttackTimeMs -= 50L) {
                                 this.blinkTicks++;
                              }
                           }

                           Valor.packetBlinkManager.delayTicks = this.blinkTicks;
                           this.isBlinking = true;
                           return;
                        }
                     }
                  }

                  return;
               }

               this.blinkTicks = -1;
               return;
            case POST:
               Vec3 var12 = Valor.packetBlinkManager.startPos;
               if (this.renderLastPos == null) {
                  this.renderPos = var12;
               } else {
                  this.renderPos = this.renderLastPos;
               }

               this.renderLastPos = var12;
         }
      }
   }

   public LagRange() {
      super("LagRange", false, ModuleCategory.COMBAT);
   }
}
