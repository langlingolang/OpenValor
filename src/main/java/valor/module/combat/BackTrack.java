package valor.module.combat;

import java.awt.Color;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import valor.core.HudThemeManager;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.PacketEvent;
import valor.events.Render3DEvent;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.util.RotationUtil;

public class BackTrack extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public final FloatProperty delayMinProp = new FloatProperty("delay-min", 80.0F, 0.0F, 400.0F);
   public final FloatProperty delayMaxProp = new FloatProperty("delay-max", 120.0F, 0.0F, 400.0F);
   public final FloatProperty maxReachProp = new FloatProperty("max-reach", 6.0F, 3.0F, 8.0F);
   public final BooleanProperty smartProp = new BooleanProperty("smart", Boolean.TRUE);
   public final BooleanProperty renderProp = new BooleanProperty("render", Boolean.TRUE);
   private final ConcurrentLinkedQueue<BackTrack.TimedPacket> packetQueue = new ConcurrentLinkedQueue<>();
   private volatile EntityLivingBase targetEntity;
   private volatile Vec3 targetRealPos;
   private volatile Vec3 targetTrackedPos;
   private long lastRespawnTimeMs;
   private boolean isBacktracking;
   private double targetPosX;
   private double targetPosY;
   private double targetPosZ;
   private double targetLastPosX;
   private double targetLastPosY;
   private double targetLastPosZ;
   private boolean hasTargetPosition;
   private float targetWidth;
   private static final long MAX_TRACK_TIME_MS = 500L;
   private static final int MAX_POSITION_HISTORY = 12;
   private static final int MIN_POSITION_HISTORY = 3;
   private final ConcurrentLinkedDeque<BackTrack.TrackPoint> positionHistoryQueue = new ConcurrentLinkedDeque<>();
   private long lastAttackTimeMs;

   @EventTarget
   public void onPacket(PacketEvent var1) {
      if (mc.thePlayer != null && mc.theWorld != null) {
         if (System.currentTimeMillis() - this.lastRespawnTimeMs >= 1000L) {
            if (this.isEnabled()) {
               Packet var2 = var1.getPacket();
               if (var1.getType() != EventType.SEND) {
                  if (var1.getType() == EventType.RECEIVE) {
                     if (var2 instanceof S08PacketPlayerPosLook || var2 instanceof S07PacketRespawn) {
                        this.flushPackets();
                        if (var2 instanceof S07PacketRespawn) {
                           this.lastRespawnTimeMs = System.currentTimeMillis();
                        }

                        return;
                     }

                     if (var2 instanceof S12PacketEntityVelocity && ((S12PacketEntityVelocity)var2).getEntityID() == mc.thePlayer.getEntityId()
                        || var2 instanceof S27PacketExplosion) {
                        this.flushPackets();
                        return;
                     }

                     EntityLivingBase var16 = this.targetEntity;
                     if (this.targetEntity == null) {
                        return;
                     }

                     if (!isTargetEntityPacket(var2, var16)) {
                        return;
                     }

                     this.targetTrackedPos = this.targetRealPos;
                     Vec3 var20 = this.targetRealPos;
                     if (this.targetRealPos != null) {
                        double var21 = var20.xCoord;
                        double var12 = var20.yCoord;
                        double var14 = var20.zCoord;
                        if (var2 instanceof S14PacketEntity) {
                           S14PacketEntity var17 = (S14PacketEntity)var2;
                           var21 += (double)var17.func_149062_c() / 32.0;
                           var12 += (double)var17.func_149061_d() / 32.0;
                           var14 += (double)var17.func_149064_e() / 32.0;
                        } else if (var2 instanceof S18PacketEntityTeleport) {
                           S18PacketEntityTeleport var18;
                           var21 = (double)(var18 = (S18PacketEntityTeleport)var2).getX() / 32.0;
                           var12 = (double)var18.getY() / 32.0;
                           var14 = (double)var18.getZ() / 32.0;
                        }

                        this.targetRealPos = new Vec3(var21, var12, var14);
                     }

                     var1.setCancelled(true);
                     long var19 = (long)((Float)this.delayMinProp.getValue()).doubleValue();
                     long var6 = (long)((Float)this.delayMaxProp.getValue()).doubleValue();
                     long var8 = var19 + (var6 > var19 ? ThreadLocalRandom.current().nextLong(var6 - var19) : 0L);
                     this.packetQueue.add(new BackTrack.TimedPacket(var2, System.currentTimeMillis() + var8));
                  }
               } else if (var2 instanceof C02PacketUseEntity) {
                  C02PacketUseEntity var3;
                  if ((var3 = (C02PacketUseEntity)var2).getAction() == Action.ATTACK) {
                     Entity var4;
                     if ((var4 = var3.getEntityFromWorld(mc.theWorld)) instanceof EntityLivingBase && var4 != mc.thePlayer) {
                        EntityLivingBase var5 = (EntityLivingBase)var4;
                        if (this.targetEntity != var5) {
                           this.flushPackets();
                           this.targetEntity = var5;
                           this.targetRealPos = new Vec3(var5.posX, var5.posY, var5.posZ);
                           this.targetTrackedPos = this.targetRealPos;
                           this.isBacktracking = false;
                           this.targetPosX = this.targetLastPosX = var5.posX;
                           this.targetPosY = this.targetLastPosY = var5.posY;
                           this.targetPosZ = this.targetLastPosZ = var5.posZ;
                           this.hasTargetPosition = true;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   private static void processPacket(Packet<?> var0) {
      if (mc.getNetHandler() != null) {
         try {
            ((Packet)var0).processPacket(mc.getNetHandler());
         } catch (Exception var1) {
         }
      }
   }

   @EventTarget
   public void onRender3D(Render3DEvent var1) {
      if ((Boolean)this.renderProp.getValue() && !(this.targetWidth < 0.01F) && this.hasTargetPosition && !this.positionHistoryQueue.isEmpty()) {
         EntityLivingBase var43 = this.targetEntity;
         double var3 = (this.targetEntity != null ? (double)var43.width : 0.6) / 2.0 + 0.05;
         double var5 = (var43 != null ? (double)var43.height : 1.8) + 0.1;
         HudThemeManager var44;
         Color var45 = (var44 = HudThemeManager.getInstance()) != null ? var44.getColor(System.currentTimeMillis(), 0L) : new Color(100, 60, 180);
         GL11.glPushAttrib(1048575);
         GL11.glPushMatrix();
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         GL11.glDisable(3553);
         GL11.glDisable(2929);
         GL11.glDisable(2884);
         GL11.glDisable(2896);
         GL11.glDepthMask(false);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         long var9 = System.currentTimeMillis();
         BackTrack.TrackPoint[] var2 = this.positionHistoryQueue.toArray(new BackTrack.TrackPoint[0]);

         for (int var7 = 0; var7 < var2.length; var7++) {
            int var8 = var7 < var2.length - 1 ? 3 : 1;

            for (int var11 = 0; var11 < var8; var11++) {
               float var12 = (float)var11 / 3.0F;
               BackTrack.TrackPoint var13 = var2[Math.max(0, var7 - 1)];
               BackTrack.TrackPoint var14 = var2[var7];
               BackTrack.TrackPoint var15 = var2[Math.min(var2.length - 1, var7 + 1)];
               BackTrack.TrackPoint var16 = var2[Math.min(var2.length - 1, var7 + 2)];
               double var17 = catmullRom(var13.posX, var14.posX, var15.posX, var16.posX, var12);
               double var19 = catmullRom(var13.posY, var14.posY, var15.posY, var16.posY, var12);
               double var21 = catmullRom(var13.posZ, var14.posZ, var15.posZ, var16.posZ, var12);
               long var23 = (long)((float)var14.timestamp + (float)(var15.timestamp - var14.timestamp) * var12);
               float var10000 = var12 = 1.0F - Math.min(1.0F, (float)(var9 - var23) / 500.0F);
               var12 = var10000 * var10000 * (3.0F - var12 * 2.0F);
               if (!((var12 = this.targetWidth * var12) < 0.015F)) {
                  double var25 = var17 - mc.getRenderManager().viewerPosX;
                  double var27 = var19 - mc.getRenderManager().viewerPosY;
                  double var29 = var21 - mc.getRenderManager().viewerPosZ;
                  AxisAlignedBB var49 = new AxisAlignedBB(var25 - var3, var27, var29 - var3, var25 + var3, var27 + var5, var29 + var3);
                  GL11.glColor4f((float)var45.getRed() / 255.0F, (float)var45.getGreen() / 255.0F, (float)var45.getBlue() / 255.0F, var12 * 0.03F);
                  double var31 = var49.minX;
                  double var33 = var49.minY;
                  double var35 = var49.minZ;
                  double var37 = var49.maxX;
                  double var39 = var49.maxY;
                  double var41 = var49.maxZ;
                  GL11.glBegin(7);
                  GL11.glVertex3d(var31, var33, var35);
                  GL11.glVertex3d(var37, var33, var35);
                  GL11.glVertex3d(var37, var33, var41);
                  GL11.glVertex3d(var31, var33, var41);
                  GL11.glVertex3d(var31, var39, var35);
                  GL11.glVertex3d(var31, var39, var41);
                  GL11.glVertex3d(var37, var39, var41);
                  GL11.glVertex3d(var37, var39, var35);
                  GL11.glVertex3d(var31, var33, var35);
                  GL11.glVertex3d(var31, var39, var35);
                  GL11.glVertex3d(var37, var39, var35);
                  GL11.glVertex3d(var37, var33, var35);
                  GL11.glVertex3d(var37, var33, var41);
                  GL11.glVertex3d(var37, var39, var41);
                  GL11.glVertex3d(var31, var39, var41);
                  GL11.glVertex3d(var31, var33, var41);
                  GL11.glVertex3d(var31, var33, var35);
                  GL11.glVertex3d(var31, var33, var41);
                  GL11.glVertex3d(var31, var39, var41);
                  GL11.glVertex3d(var31, var39, var35);
                  GL11.glVertex3d(var37, var33, var35);
                  GL11.glVertex3d(var37, var39, var35);
                  GL11.glVertex3d(var37, var39, var41);
                  GL11.glVertex3d(var37, var33, var41);
                  GL11.glEnd();
                  GL11.glLineWidth(3.0F);
                  GL11.glColor4f((float)var45.getRed() / 255.0F, (float)var45.getGreen() / 255.0F, (float)var45.getBlue() / 255.0F, var12 * 0.055F);
                  RenderGlobal.drawSelectionBoundingBox(var49);
                  GL11.glLineWidth(1.0F);
                  GL11.glColor4f((float)var45.getRed() / 255.0F, (float)var45.getGreen() / 255.0F, (float)var45.getBlue() / 255.0F, var12 * 0.24F);
                  RenderGlobal.drawSelectionBoundingBox(var49);
               }
            }
         }

         GL11.glDepthMask(true);
         GL11.glPopMatrix();
         GL11.glPopAttrib();
         GlStateManager.enableTexture2D();
         GlStateManager.enableDepth();
         GlStateManager.disableBlend();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private static void sendPacketDirect(Packet<?> packet) {
      if (mc.getNetHandler() != null && packet != null) {
         try {
            ((Packet)packet).processPacket(mc.getNetHandler());
         } catch (Exception ignored) {
         }
      }
   }
private static double catmullRom(double var0, double var2, double var4, double var6, float var8) {
      double var9;
      double var11 = (var9 = (double)(var8 * var8)) * (double)var8;
      return 0.5 * (var2 * 2.0 + (-var0 + var4) * (double)var8 + (var0 * 2.0 - var2 * 5.0 + var4 * 4.0 - var6) * var9 + (-var0 + var2 * 3.0 - var4 * 3.0 + var6) * var11);
   }

   public final boolean isTargetInReach(EntityLivingBase var1, float var2, float var3, float var4) {
      if (this.isEnabled() && var1 != null && this.targetEntity == var1 && !this.packetQueue.isEmpty()) {
         Vec3 var5 = this.targetRealPos;
         if (this.targetRealPos == null) {
            this.flushPackets();
            return false;
         } else {
            AxisAlignedBB var8 = var1.getEntityBoundingBox().offset(var5.xCoord - var1.posX, var5.yCoord - var1.posY, var5.zCoord - var1.posZ);
            double var6 = Math.max(0.1, (double)var4);
            boolean var10 = RotationUtil.getDistanceToBoxEyes(var8) <= var6;
            boolean var9 = RotationUtil.calculateBoxIntercept(var8, var2, var3, var6 + 0.05) != null;
            if (var10 && var9) {
               return true;
            } else {
               this.flushPackets();
               return false;
            }
         }
      } else {
         return true;
      }
   }
@Override
   public void onDisabled() {
      this.flushPackets();
   }

   @Override
   public void onEnabled() {
      this.resetBacktrackState();
   }

   private void resetBacktrackState() {
      this.targetEntity = null;
      this.targetRealPos = null;
      this.targetTrackedPos = null;
      this.isBacktracking = false;
      this.hasTargetPosition = false;
      this.targetWidth = 0.0F;
      this.positionHistoryQueue.clear();
      this.lastAttackTimeMs = 0L;
   }

   @EventTarget
   public void onTick(TickEvent var1) {
      if (var1.getType() == EventType.PRE && mc.thePlayer != null && mc.theWorld != null) {
         if (mc.theWorld.getWorldInfo() != null && mc.getNetHandler() != null) {
            EntityLivingBase var15 = this.targetEntity;
            Vec3 var2 = this.targetRealPos;
            if (var15 != null) {
               if ((Boolean)this.smartProp.getValue() && mc.objectMouseOver != null && mc.objectMouseOver.entityHit == var15) {
                  this.isBacktracking = true;
               }

               if (!var15.isEntityAlive() || (double)mc.thePlayer.getDistanceToEntity(var15) > 7.0) {
                  this.isBacktracking = true;
                  this.targetEntity = null;
               } else if (var2 != null
                  && mc.thePlayer.getDistance(var2.xCoord, var2.yCoord, var2.zCoord) > (double)((Float)this.maxReachProp.getValue()).floatValue()) {
                  this.isBacktracking = true;
               }
            } else if (!this.packetQueue.isEmpty()) {
               this.isBacktracking = true;
            }

            BackTrack var16 = this;
            if (this.packetQueue.isEmpty()) {
               if (this.isBacktracking && this.targetEntity == null) {
                  this.isBacktracking = false;
               }
            } else {
               long var11 = System.currentTimeMillis();
               int var20 = this.isBacktracking ? 50 : Math.max(8, this.packetQueue.size() / 2);

               for (int var13 = 0; !var16.packetQueue.isEmpty() && var13 < var20; var13++) {
                  BackTrack.TimedPacket var14 = var16.packetQueue.peek();
                  if (!var16.isBacktracking && var11 < var14.timestamp) {
                     break;
                  }

                  var16.packetQueue.poll();
                  sendPacketDirect(var14.packet);
               }
            }

            EntityLivingBase var17 = this.targetEntity;
            var2 = this.targetRealPos;
            if (var17 != null && var2 != null) {
               double var3 = !this.packetQueue.isEmpty() ? var2.xCoord : var17.posX;
               double var5 = !this.packetQueue.isEmpty() ? var2.yCoord : var17.posY;
               double var7 = !this.packetQueue.isEmpty() ? var2.zCoord : var17.posZ;
               if (!this.hasTargetPosition) {
                  this.targetPosX = this.targetLastPosX = var3;
                  this.targetPosY = this.targetLastPosY = var5;
                  this.targetPosZ = this.targetLastPosZ = var7;
                  this.hasTargetPosition = true;
               } else {
                  this.targetLastPosX = this.targetPosX;
                  this.targetLastPosY = this.targetPosY;
                  this.targetLastPosZ = this.targetPosZ;
                  double var9 = 0.18 + 0.16 * (1.0 - Math.min(1.0, (double)this.packetQueue.size() / 25.0));
                  this.targetPosX = this.targetPosX + (var3 - this.targetPosX) * var9;
                  this.targetPosY = this.targetPosY + (var5 - this.targetPosY) * var9;
                  this.targetPosZ = this.targetPosZ + (var7 - this.targetPosZ) * var9;
               }

               BackTrack var18 = this;
               long var24 = System.currentTimeMillis();
               BackTrack.TrackPoint var22;
               double var26 = (var22 = this.positionHistoryQueue.peekLast()) == null
                  ? Double.MAX_VALUE
                  : (this.targetPosX - var22.posX) * (this.targetPosX - var22.posX)
                     + (this.targetPosY - var22.posY) * (this.targetPosY - var22.posY)
                     + (this.targetPosZ - var22.posZ) * (this.targetPosZ - var22.posZ);
               if (var22 == null || var26 > 1.0E-4 && var24 - this.lastAttackTimeMs >= 32L) {
                  this.positionHistoryQueue.addLast(new BackTrack.TrackPoint(this.targetPosX, this.targetPosY, this.targetPosZ, var24));
                  this.lastAttackTimeMs = var24;

                  while (var18.positionHistoryQueue.size() > 12) {
                     var18.positionHistoryQueue.pollFirst();
                  }
               }
            }

            long var25 = System.currentTimeMillis();
            BackTrack var19 = this;
            BackTrack.TrackPoint var23;

            while ((var23 = var19.positionHistoryQueue.peekFirst()) != null && var25 - var23.timestamp > 500L) {
               var19.positionHistoryQueue.pollFirst();
            }

            this.targetWidth = this.targetEntity != null ? Math.min(1.0F, this.targetWidth + 0.12F) : Math.max(0.0F, this.targetWidth - 0.08F);
         } else {
            this.packetQueue.clear();
            this.resetBacktrackState();
         }
      }
   }

   public BackTrack() {
      super("BackTrack", false, ModuleCategory.COMBAT);
   }

   private void flushPackets() {
      BackTrack.TimedPacket var1;
      while ((var1 = this.packetQueue.poll()) != null) {
         processPacket(var1.packet);
      }
      this.resetBacktrackState();
   }

      static class TimedPacket {
      final Packet<?> packet;
      final long timestamp;

      TimedPacket(Packet<?> var1, long var2) {
         this.packet = var1;
         this.timestamp = var2;
      }
   }

   static class TrackPoint {
      final double posX;
      final double posY;
      final double posZ;
      final long timestamp;

      TrackPoint(double var1, double var3, double var5, long var7) {
         this.posX = var1;
         this.posY = var3;
         this.posZ = var5;
         this.timestamp = var7;
      }
   }
   private static boolean isTargetEntityPacket(Packet<?> packet, EntityLivingBase target) {
      if (target == null) return false;
      int targetId = target.getEntityId();
      if (packet instanceof S14PacketEntity) {
         return ((S14PacketEntity)packet).getEntity(mc.theWorld) == target;
      }
      if (packet instanceof S18PacketEntityTeleport) {
         return ((S18PacketEntityTeleport)packet).getEntityId() == targetId;
      }
      if (packet instanceof S19PacketEntityStatus) {
         return ((S19PacketEntityStatus)packet).getEntity(mc.theWorld) == target;
      }
      if (packet instanceof S12PacketEntityVelocity) {
         return ((S12PacketEntityVelocity)packet).getEntityID() == targetId;
      }
      if (packet instanceof S0BPacketAnimation) {
         return ((S0BPacketAnimation)packet).getEntityID() == targetId;
      }
      if (packet instanceof S1CPacketEntityMetadata) {
         return ((S1CPacketEntityMetadata)packet).getEntityId() == targetId;
      }
      return false;
   }

}
