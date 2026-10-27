package valor.module.world;

import com.google.common.base.CaseFormat;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBed.EnumPartType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import valor.Valor;
import valor.core.HudThemeManager;
import valor.core.LagType;
import valor.core.MovementManager;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.HitBlockEvent;
import valor.events.KnockbackEvent;
import valor.events.LeftClickMouseEvent;
import valor.events.LoadWorldEvent;
import valor.events.MoveInputEvent;
import valor.events.PacketEvent;
import valor.events.PlayerUpdateEvent;
import valor.events.Render2DEvent;
import valor.events.Render3DEvent;
import valor.events.RightClickMouseEvent;
import valor.events.SwapItemEvent;
import valor.events.TickEvent;
import valor.events.UpdateEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.module.combat.KillAura;
import valor.module.render.BedESP;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.ModeProperty;
import valor.setting.PercentageProperty;
import valor.util.BlockUtil;
import valor.util.ChatColor;
import valor.util.ColorUtil;
import valor.util.DraggableHudElement;
import valor.util.EaseAnimation;
import valor.util.FloatInterpolator;
import valor.util.GlScaleUtil;
import valor.util.HudDragManager;
import valor.util.ItemUtil;
import valor.util.MoveUtil;
import valor.util.PacketUtil;
import valor.util.PlayerUtil;
import valor.util.ReflectionUtil;
import valor.util.Render3DUtil;
import valor.util.RenderUtil;
import valor.util.RotationUtil;
import valor.util.TimerUtil;

public class BedNuker extends Module implements DraggableHudElement {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
   private final TimerUtil scanDelayTimer = new TimerUtil();
   private final ArrayList<BlockPos> scannedBedList = new ArrayList<>();
   private final Color PROGRESS_COLOR_RED = new Color(ChatColor.RED.rgb);
   private final Color PROGRESS_COLOR_YELLOW = new Color(ChatColor.YELLOW.rgb);
   private final Color PROGRESS_COLOR_GREEN = new Color(ChatColor.GREEN.rgb);
   private BlockPos targetBedPos = null;
   private int breakStage = 0;
   private int breakTicks = 0;
   private float breakProgress = 0.0F;
   private boolean isBedBlock = false;
   private int originalSlot = -1;
   private boolean aimingAtBed = false;
   private boolean isBreaking = false;
   private boolean bedRespawnPending = false;
   private EnumFacing breakFacing = null;
   private long lastBreakTimeMs = 0L;
   private int breakCooldownTicks = 0;
   private float hudWidth = 106.0F;
   private float hudHeight = 29.0F;
   private boolean hudInitialized = false;
   private final EaseAnimation easeAnimation = new EaseAnimation();
   private final FloatInterpolator progressInterpolator = new FloatInterpolator(0.0F);
   private float lastProgress = 0.64F;
   public final ModeProperty modeProp = new ModeProperty("mode", 0, new String[]{"LEGIT", "SWAP"});
   public final FloatProperty rangeProp = new FloatProperty("range", 4.5F, 3.0F, 6.0F);
   public final PercentageProperty speedProp = new PercentageProperty("speed", 0);
   public final BooleanProperty groundSpoofProp = new BooleanProperty("ground-spoof", Boolean.FALSE);
   public final ModeProperty ignoreVelocityProp = new ModeProperty("ignore-velocity", 0, new String[]{"NONE", "CANCEL", "DELAY"});
   public final BooleanProperty surroundingsProp = new BooleanProperty("surroundings", Boolean.TRUE);
   public final BooleanProperty toolCheckProp = new BooleanProperty("tool-check", Boolean.TRUE);
   public final BooleanProperty whitelistProp = new BooleanProperty("whitelist", Boolean.TRUE);
   public final BooleanProperty swingProp = new BooleanProperty("swing", Boolean.TRUE);
   public final ModeProperty moveFixProp = new ModeProperty("move-fix", 1, new String[]{"NONE", "SILENT", "STRICT"});
   public final ModeProperty showTargetProp = new ModeProperty("show-target", 1, new String[]{"NONE", "DEFAULT", "THEME"});
   public final ModeProperty showProgressProp = new ModeProperty("show-progress", 1, new String[]{"NONE", "DEFAULT", "THEME"});
   public final FloatProperty progressXProp = new FloatProperty("progress-x", -1.0F, -1.0F, 4000.0F, () -> (Integer)this.showProgressProp.getValue() != 0);
   public final FloatProperty progressYProp = new FloatProperty("progress-y", -1.0F, -1.0F, 4000.0F, () -> (Integer)this.showProgressProp.getValue() != 0);

   @Override
   public String[] getSuffix() {
      return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.modeProp.getModeName())};
   }

   @EventTarget(4)
   public void onUpdate(UpdateEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.PRE) {
         if (!((AutoBlockIn)Valor.moduleManager.modules.get(AutoBlockIn.class)).isEnabled()) {
            if (isKillAuraAttacking()) {
               this.stopBreaking();
            } else {
               if (this.isReady()) {
                  double var3 = (double)this.targetBedPos.getX() + 0.5 - mc.thePlayer.posX;
                  double var5 = (double)this.targetBedPos.getY() + 0.5 - mc.thePlayer.posY - (double)mc.thePlayer.getEyeHeight();
                  double var7 = (double)this.targetBedPos.getZ() + 0.5 - mc.thePlayer.posZ;
                  float[] var2 = RotationUtil.getRotations(var3, var5, var7, var1.getYaw(), var1.getPitch());
                  var1.setRotation(var2[0], var2[1], 5);
                  var1.setPervRotation(this.moveFixProp.getValue() != 0 ? var2[0] : mc.thePlayer.rotationYaw, 5);
               }
            }
         }
      }
   }

   @EventTarget
   public void onPacket(PacketEvent var1) {
      if (!var1.isCancelled()) {
         String var2;
         if (var1.getPacket() instanceof S02PacketChat
            && (
               (var2 = ((S02PacketChat)var1.getPacket()).getChatComponent().getFormattedText())
                     .contains("\u00a7e\u00a7lProtect your bed and destroy the enemy bed")
                  || var2.contains("\u00a7e\u00a7lDestroy the enemy bed and then eliminate them")
            )) {
            this.bedRespawnPending = true;
         }

         if (var1.getPacket() instanceof S08PacketPlayerPosLook && this.bedRespawnPending) {
            this.bedRespawnPending = false;
            this.scannedBedList.clear();
            this.executor.schedule(() -> {
               int var1x = MathHelper.floor_double(mc.thePlayer.posX);
               int var2x = MathHelper.floor_double(mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight());
               int var3x = MathHelper.floor_double(mc.thePlayer.posZ);

               for (int var4x = var1x - 25; var4x <= var1x + 25; var4x++) {
                  for (int var5 = var2x - 25; var5 <= var2x + 25; var5++) {
                     for (int var6 = var3x - 25; var6 <= var3x + 25; var6++) {
                        BlockPos var7 = new BlockPos(var4x, var5, var6);
                        if (mc.theWorld.getBlockState(var7).getBlock() instanceof BlockBed) {
                           this.scannedBedList.add(var7);
                        }
                     }
                  }
               }
            }, 1L, TimeUnit.SECONDS);
         }

         if (this.isEnabled() && this.targetBedPos != null && (Integer)this.ignoreVelocityProp.getValue() == 2 && Valor.lagManager.currentType != LagType.TIMER_RANGE) {
            S12PacketEntityVelocity var3;
            if (var1.getPacket() instanceof S12PacketEntityVelocity
               && (var3 = (S12PacketEntityVelocity)var1.getPacket()).getEntityID() == mc.thePlayer.getEntityId()
               && var3.getMotionY() > 0) {
               Valor.lagManager.currentType = LagType.TIMER_RANGE;
               Valor.lagManager.packetQueue.offer(var3);
               var1.setCancelled(true);
            }

            S27PacketExplosion var4;
            if (var1.getPacket() instanceof S27PacketExplosion
               && ((var4 = (S27PacketExplosion)var1.getPacket()).func_149149_c() != 0.0F || var4.func_149144_d() != 0.0F || var4.func_149147_e() != 0.0F)) {
               Valor.lagManager.currentType = LagType.TIMER_RANGE;
               Valor.lagManager.packetQueue.offer(var4);
               var1.setCancelled(true);
            }
         }
      }
   }

   private void resetBreak() {
      if (this.targetBedPos != null) {
         mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), this.targetBedPos, -1);
         if (this.breakStage == 1) {
            PacketUtil.sendPacket(new C07PacketPlayerDigging(Action.ABORT_DESTROY_BLOCK, this.targetBedPos, this.breakFacing != null ? this.breakFacing : EnumFacing.UP));
         }
      }

      this.targetBedPos = null;
      this.breakStage = 0;
      this.breakTicks = 0;
      this.breakProgress = 0.0F;
      this.isBedBlock = false;
      this.aimingAtBed = false;
      this.isBreaking = false;
      this.breakFacing = null;
      this.lastBreakTimeMs = 0L;
      this.breakCooldownTicks = 0;
      this.easeAnimation.reset();
   }

   private void restoreSlot() {
      if (this.originalSlot != -1) {
         mc.thePlayer.inventory.currentItem = this.originalSlot;
         syncHeldItem();
         this.originalSlot = -1;
      }
   }

   private static void syncHeldItem() {
      int var0 = ReflectionUtil.getCurrentPlayerItem(mc.playerController);
      if (mc.thePlayer.inventory.currentItem != var0) {
         mc.thePlayer.stopUsingItem();
      }

      ReflectionUtil.syncCurrentPlayItemCompat(mc.playerController);
   }

   private void swingHand() {
      if ((Boolean)this.swingProp.getValue()) {
         mc.thePlayer.swingItem();
      } else {
         PacketUtil.sendPacket(new C0APacketAnimation());
      }
   }

   @EventTarget(3)
   public void onRender3D(Render3DEvent var1) {
      if (this.isEnabled() && this.targetBedPos != null && !mc.theWorld.isAirBlock(this.targetBedPos)) {
         mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), this.targetBedPos, (int)(this.calculateBreakProgress() * 10.0F) - 1);
         if ((Integer)this.showTargetProp.getValue() != 0) {
            BedESP var6 = (BedESP)Valor.moduleManager.modules.get(BedESP.class);
            int var3 = (Integer)this.showTargetProp.getValue();
            Color var2 = this.getProgressColor(var3, this.calculateBreakProgress());
            Render3DUtil.setupRender3D();
            BlockPos var7 = this.targetBedPos;
            double var4 = this.isBedBlock && var6 != null ? var6.getBedHeight() : 1.0;
            Render3DUtil.drawBlockESP(var7, var4, var2.getRed(), var2.getBlue(), var2.getGreen());
            Render3DUtil.endRender3D();
         }
      }
   }

   public final boolean isReady() {
      return this.targetBedPos != null && this.aimingAtBed;
   }

   public float calculateBreakProgress() {
      if (this.targetBedPos == null) {
         return 0.0F;
      } else {
         float var1 = this.breakProgress;
         if ((Boolean)this.groundSpoofProp.getValue()) {
            int var2 = ItemUtil.findBestToolForBlock(mc.thePlayer.inventory.currentItem, mc.theWorld.getBlockState(this.targetBedPos).getBlock());
            var1 = (float)this.breakTicks * getBlockHardnessPerTick(mc.theWorld.getBlockState(this.targetBedPos), this.targetBedPos, var2, true);
         }

         return Math.min(1.0F, var1 / (1.0F - 0.3F * ((float)((Integer)this.speedProp.getValue()).intValue() / 100.0F)));
      }
   }

   @EventTarget
   public void onPlayerUpdate(PlayerUpdateEvent var1) {
      if (this.isEnabled()) {
         if (!isKillAuraAttacking()) {
            if (this.targetBedPos != null && this.isBreaking && !Valor.stateTracker.hasUsedEntity && !Valor.stateTracker.hasDug && !Valor.stateTracker.hasPlacedBlock && !Valor.stateTracker.hasAnimated) {
               this.swingHand();
            }
         }
      }
   }

   public BlockPos findTargetBed() {
      double var10001 = mc.thePlayer.posX;
      double var10002 = mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight();
      double var6 = mc.thePlayer.posZ;
      double var4 = var10002;
      double var2 = var10001;
      BedNuker var1 = this;
      ArrayList<BlockPos> var8 = new ArrayList<>();
      int var9 = MathHelper.floor_double(var2);
      int var10 = MathHelper.floor_double(var4);
      int var11 = MathHelper.floor_double(var6);

      for (int var12 = var9 - 6; var12 <= var9 + 6; var12++) {
         for (int var13 = var10 - 6; var13 <= var10 + 6; var13++) {
            for (int var14 = var11 - 6; var14 <= var11 + 6; var14++) {
               BlockPos var15 = new BlockPos(var12, var13, var14);
               if ((!(Boolean)var1.whitelistProp.getValue() || !var1.scannedBedList.contains(var15))
                  && mc.theWorld.getBlockState(var15).getBlock() instanceof BlockBed
                  && PlayerUtil.isBlockInRange(var15, var2, var4, var6, ((Float)var1.rangeProp.getValue()).doubleValue())) {
                  var8.add(var15);
               }
            }
         }
      }

      if (!var8.isEmpty()) {
         var8.sort(
            Comparator.comparingDouble(
               var0 -> var0.distanceSqToCenter(mc.thePlayer.posX, mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ)
            )
         );

         for (BlockPos var26 : var8) {
            if (!(Boolean)var1.surroundingsProp.getValue()) {
               return var26;
            }

            BlockPos var27 = findBedSurroundingBlock(var26);
            if (var27 == null) {
               return var26;
            }

            Block var28 = mc.theWorld.getBlockState(var27).getBlock();
            if (!(Boolean)var1.toolCheckProp.getValue()) {
               return var27;
            }

            Material var19;
            boolean var29;
            if ((var19 = var28.getMaterial()) != Material.iron && var19 != Material.anvil && var19 != Material.rock) {
               var29 = true;
            } else {
               int var20 = 0;

               while (true) {
                  if (var20 >= 9) {
                     var29 = false;
                     break;
                  }

                  ItemStack var23;
                  if ((var23 = mc.thePlayer.inventory.getStackInSlot(var20)) != null && var23.getItem() instanceof ItemPickaxe) {
                     var29 = true;
                     break;
                  }

                  var20++;
               }
            }

            if (var29) {
               return var27;
            }
         }
      }

      return null;
   }

   public static float getBlockHardnessPerTick(IBlockState blockState, BlockPos pos, int slot, boolean onGround) {
      Block block;
      float var5 = (block = blockState.getBlock()).getBlockHardness(mc.theWorld, pos);
      float var6 = canHarvestBlock(block, slot) ? 30.0F : 100.0F;
      return var5 < 0.0F ? 0.0F : getDigSpeed(blockState, slot, onGround) / var5 / var6;
   }

   public static float getBlockBreakSpeed(BlockPos pos) {
      IBlockState var1 = mc.theWorld.getBlockState(pos);
      int var2 = ItemUtil.findBestToolForBlock(mc.thePlayer.inventory.currentItem, var1.getBlock());
      return getBlockHardnessPerTick(var1, pos, var2, mc.thePlayer.onGround);
   }

   public Color getProgressColor(int themeMode, float progress) {
      switch (themeMode) {
         case 1:
            if (progress <= 0.5F) {
               return ColorUtil.lerp(progress / 0.5F, this.PROGRESS_COLOR_RED, this.PROGRESS_COLOR_YELLOW);
            }

            return ColorUtil.lerp((progress - 0.5F) / 0.5F, this.PROGRESS_COLOR_YELLOW, this.PROGRESS_COLOR_GREEN);
         case 2:
            return HudThemeManager.getInstance().getColor(System.currentTimeMillis(), 0L);
         default:
            return new Color(-1);
      }
   }

   public static float getDigSpeed(IBlockState blockState, int slot, boolean onGround) {
      ItemStack var5 = mc.thePlayer.inventory.getStackInSlot(slot);
      float var3 = 1.0F;
      if (var5 != null) {
         var3 = var5.getStrVsBlock(blockState.getBlock());
      }

      int var4;
      if (var3 > 1.0F && (var4 = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, var5)) > 0) {
         var3 += (float)(var4 * var4 + 1);
      }

      if (mc.thePlayer.isPotionActive(Potion.digSpeed)) {
         var3 *= 1.0F + (float)(mc.thePlayer.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2F;
      }

      if (mc.thePlayer.isPotionActive(Potion.digSlowdown)) {
         switch (mc.thePlayer.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) {
            case 0:
               var3 *= 0.3F;
               break;
            case 1:
               var3 *= 0.09F;
               break;
            case 2:
               var3 *= 0.0027F;
               break;
            default:
               var3 *= 8.1E-4F;
         }
      }

      if (mc.thePlayer.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(mc.thePlayer)) {
         var3 /= 5.0F;
      }

      if (!onGround) {
         var3 /= 5.0F;
      }

      return var3;
   }

   public static boolean canHarvestBlock(Block block, int slot) {
      ItemStack var2;
      return block.getMaterial().isToolNotRequired() ? true : (var2 = mc.thePlayer.inventory.getStackInSlot(slot)) != null && var2.canHarvestBlock(block);
   }

   @Override
   public void onDisabled() {
      this.resetBreak();
      this.originalSlot = -1;
      Valor.lagManager.setLagging(false, LagType.TIMER_RANGE);
   }

   private void stopBreaking() {
      if (this.targetBedPos != null) {
         this.restoreSlot();
         this.resetBreak();
      }

      Valor.lagManager.setLagging(false, LagType.TIMER_RANGE);
   }

   @EventTarget(1)
   public void onTick(TickEvent var1) {
      if (this.isEnabled() && var1.getType() == EventType.PRE) {
         if (!((AutoBlockIn)Valor.moduleManager.modules.get(AutoBlockIn.class)).isEnabled()) {
            if (isKillAuraAttacking()) {
               this.stopBreaking();
            } else {
               if (this.targetBedPos != null) {
                  if (!mc.theWorld.isAirBlock(this.targetBedPos) && PlayerUtil.isBlockInRange(this.targetBedPos, ((Float)this.rangeProp.getValue()).doubleValue())) {
                     BlockPos var12;
                     if (!this.isBedBlock && (var12 = this.findTargetBed()) != null && mc.theWorld.getBlockState(var12).getBlock() instanceof BlockBed) {
                        this.resetBreak();
                     }
                  } else {
                     if (mc.theWorld.isAirBlock(this.targetBedPos) && this.isBedBlock) {
                        this.scanDelayTimer.reset();
                     }

                     this.restoreSlot();
                     this.resetBreak();
                  }
               }

               if (this.targetBedPos != null) {
                  int var13 = ItemUtil.findBestToolForBlock(mc.thePlayer.inventory.currentItem, mc.theWorld.getBlockState(this.targetBedPos).getBlock());
                  if ((Integer)this.modeProp.getValue() == 0 && this.originalSlot == -1) {
                     this.originalSlot = mc.thePlayer.inventory.currentItem;
                     mc.thePlayer.inventory.currentItem = var13;
                     syncHeldItem();
                  }

                  switch (this.breakStage) {
                     case 0:
                        if (!mc.thePlayer.isUsingItem() && !Valor.stateTracker.hasPlacedBlock) {
                           BlockPos var14 = this.targetBedPos;
                           double var6 = (double)this.targetBedPos.getX() + 0.5 - mc.thePlayer.posX;
                           double var8 = (double)var14.getY() + 0.25 - mc.thePlayer.posY - (double)mc.thePlayer.getEyeHeight();
                           double var10 = (double)var14.getZ() + 0.5 - mc.thePlayer.posZ;
                           float[] var15;
                           MovingObjectPosition var16;
                           this.breakFacing = (
                                    var16 = RotationUtil.rayTrace(
                                       (var15 = RotationUtil.getRotations(var6, var8, var10, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch))[0], var15[1], 8.0
                                    )
                                 )
                                 == null
                              ? EnumFacing.UP
                              : var16.sideHit;
                           PacketUtil.sendPacket(new C07PacketPlayerDigging(Action.START_DESTROY_BLOCK, this.targetBedPos, this.breakFacing));
                           this.swingHand();
                           mc.effectRenderer.addBlockHitEffects(this.targetBedPos, this.breakFacing);
                           this.breakStage = 1;
                           this.lastBreakTimeMs = System.currentTimeMillis();
                        }
                        break;
                     case 1:
                        if ((Integer)this.modeProp.getValue() == 1) {
                           this.aimingAtBed = false;
                        }

                        this.isBreaking = true;
                        this.breakTicks++;
                        long var2 = System.currentTimeMillis();
                        long var4 = Math.max(0L, Math.min(50L, var2 - this.lastBreakTimeMs));
                        this.lastBreakTimeMs = var2;
                        this.breakProgress = this.breakProgress + getBlockHardnessPerTick(mc.theWorld.getBlockState(this.targetBedPos), this.targetBedPos, var13, mc.thePlayer.onGround) * ((float)var4 / 50.0F);
                        mc.effectRenderer.addBlockHitEffects(this.targetBedPos, this.breakFacing);
                        if (this.breakProgress >= 1.0F - 0.3F * ((float)((Integer)this.speedProp.getValue()).intValue() / 100.0F)) {
                           if ((Integer)this.modeProp.getValue() == 1) {
                              this.aimingAtBed = true;
                              this.originalSlot = mc.thePlayer.inventory.currentItem;
                              mc.thePlayer.inventory.currentItem = var13;
                              syncHeldItem();
                              if (mc.thePlayer.isUsingItem()) {
                                 this.originalSlot = mc.thePlayer.inventory.currentItem;
                                 mc.thePlayer.inventory.currentItem = (mc.thePlayer.inventory.currentItem + 1) % 9;
                                 syncHeldItem();
                              }
                           }

                           this.isBreaking = false;
                           PacketUtil.sendPacket(new C07PacketPlayerDigging(Action.STOP_DESTROY_BLOCK, this.targetBedPos, this.breakFacing));
                           this.swingHand();
                           this.breakCooldownTicks = 0;
                           this.breakStage = 2;
                        }
                        break;
                     case 2:
                        this.isBreaking = false;
                        if (++this.breakCooldownTicks >= 10) {
                           this.restoreSlot();
                           this.resetBreak();
                        }
                  }

                  if (this.targetBedPos != null) {
                     return;
                  }
               }

               if (mc.thePlayer.capabilities.allowEdit && this.scanDelayTimer.hasTimeElapsed(500L)) {
                  this.targetBedPos = this.findTargetBed();
                  this.breakStage = 0;
                  this.breakTicks = 0;
                  this.breakProgress = 0.0F;
                  this.easeAnimation.reset();
                  this.isBedBlock = this.targetBedPos != null && mc.theWorld.getBlockState(this.targetBedPos).getBlock() instanceof BlockBed;
                  this.restoreSlot();
                  if (this.targetBedPos != null) {
                     this.aimingAtBed = true;
                  }
               }

               if (this.targetBedPos == null) {
                  Valor.lagManager.setLagging(false, LagType.TIMER_RANGE);
               }
            }
         }
      }
   }

   @Override
   public float getWidth() {
      return this.hudWidth;
   }

   @Override
   public float getX() {
      return (Float)this.progressXProp.getValue();
   }

   @Override
   public void setX(float var1) {
      this.progressXProp.setValue(var1);
      this.hudInitialized = true;
   }

   @Override
   public void setY(float var1) {
      this.progressYProp.setValue(var1);
      this.hudInitialized = true;
   }

   @Override
   public float getY() {
      return (Float)this.progressYProp.getValue();
   }

   @Override
   public float getHeight() {
      return this.hudHeight;
   }

   @Override
   public boolean isHudVisible() {
      return (Integer)this.showProgressProp.getValue() != 0 && (this.isEnabled() && this.targetBedPos != null || HudDragManager.isChatGuiOpen());
   }

   @EventTarget
   public void onLoadWorld(LoadWorldEvent var1) {
      this.bedRespawnPending = false;
   }

   @EventTarget
   public void onMoveInput(MoveInputEvent var1) {
      if (this.isEnabled()) {
         if ((Integer)this.moveFixProp.getValue() == 1 && MovementManager.isRotated() && MovementManager.getPriority() == 5.0F && MoveUtil.isMoving()) {
            MoveUtil.fixMovement(MovementManager.getMovementYaw());
         }
      }
   }

   @EventTarget
   public void onLeftClick(LeftClickMouseEvent var1) {
      if (this.isEnabled() && (this.isReady() || this.targetBedPos != null && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK)) {
         var1.setCancelled(true);
      }
   }

   @EventTarget
   public void onHitBlock(HitBlockEvent var1) {
      if (this.isEnabled() && (this.isReady() || this.targetBedPos != null && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK)) {
         var1.setCancelled(true);
      }
   }

   @EventTarget
   public void onRightClick(RightClickMouseEvent var1) {
      if (this.isEnabled() && this.isReady()) {
         var1.setCancelled(true);
      }
   }

   private static boolean isKillAuraAttacking() {
      KillAura var0;
      return (var0 = (KillAura)Valor.moduleManager.modules.get(KillAura.class)) != null && var0.canAttackTarget();
   }

   @EventTarget(1)
   public void onKnockback(KnockbackEvent var1) {
      if (this.isEnabled() && !var1.isCancelled() && !(var1.getY() <= 0.0)) {
         if ((Integer)this.ignoreVelocityProp.getValue() == 1 && this.targetBedPos != null) {
            var1.setCancelled(true);
            var1.setX(mc.thePlayer.motionX);
            var1.setY(mc.thePlayer.motionY);
            var1.setZ(mc.thePlayer.motionZ);
         }
      }
   }

   @EventTarget
   public void onSwap(SwapItemEvent var1) {
      if (this.isEnabled() && this.originalSlot != -1) {
         var1.setCancelled(true);
      }
   }

   @EventTarget
   public void onRender2D(Render2DEvent var1) {
      boolean var8 = HudDragManager.isChatGuiOpen();
      long var3 = System.currentTimeMillis();
      boolean var2 = (Integer)this.showProgressProp.getValue() != 0 && (this.isEnabled() && this.targetBedPos != null || var8);
      this.progressInterpolator.interpolateTo(var2 ? 1.0F : 0.0F, var2 ? 190L : 140L, var3);
      float var10;
      if (!((var10 = this.progressInterpolator.getValue(var3)) <= 0.01F)) {
         ScaledResolution var12 = new ScaledResolution(mc);
         if (!this.hudInitialized || (Float)this.progressXProp.getValue() < 0.0F || (Float)this.progressYProp.getValue() < 0.0F) {
            this.progressXProp.setValue((float)var12.getScaledWidth() / 2.0F - this.hudWidth / 2.0F);
            this.progressYProp.setValue((float)var12.getScaledHeight() / 5.0F * 2.0F - this.hudHeight / 2.0F);
            this.hudInitialized = true;
         }

         float var13 = (Float)this.progressXProp.getValue();
         float var4 = (Float)this.progressYProp.getValue();
         float var9;
         if (this.targetBedPos != null) {
            this.easeAnimation.start(this.calculateBreakProgress(), 180.0F, EaseAnimation.Easing.OUT_CUBIC);
            var9 = this.easeAnimation.update();
            this.lastProgress = var9;
         } else {
            var9 = var8 ? 0.64F : this.lastProgress;
         }

         String var5 = "BedNuker";
         this.getProgressColor((Integer)this.showProgressProp.getValue(), var9);
         GlScaleUtil.beginScale(var13, var4, this.hudWidth, this.hudHeight, var10);

         try {
            RenderUtil.drawPanel(var13, var4, this.hudWidth, this.hudHeight);
            RenderUtil.drawString(var5, var13 + 9.0F, var4 + 6.0F, RenderUtil.getLightTextColor(), false);
            String var11;
            RenderUtil.drawString(
               var11 = Math.round(Math.max(0.0F, Math.min(1.0F, var9)) * 100.0F) + "%",
               var13 + this.hudWidth - 9.0F - RenderUtil.getStringWidth(var11),
               var4 + 6.0F,
               RenderUtil.getSecondaryTextColor(),
               false
            );
            RenderUtil.drawProgressBar(var13 + 9.0F, var4 + this.hudHeight - 9.0F, this.hudWidth - 18.0F, 3.0F, var9, RenderUtil.getThemeColor());
         } finally {
            GlScaleUtil.endScale();
         }

         HudDragManager.handleDragging(this);
      }
   }

   public BedNuker() {
      super("BedNuker", false, ModuleCategory.WORLD);
   }
   private static BlockPos findBedSurroundingBlock(BlockPos bedPos) {
      IBlockState state = mc.theWorld.getBlockState(bedPos);
      if (!(state.getBlock() instanceof BlockBed)) {
         return null;
      }
      List<BlockPos> candidates = new ArrayList<>();
      EnumPartType part = state.getValue(BlockBed.PART);
      EnumFacing facing = state.getValue(BlockBed.FACING);
      List<BlockPos> bedParts = Arrays.asList(bedPos, bedPos.offset(part == EnumPartType.HEAD ? facing.getOpposite() : facing));

      for (BlockPos partPos : bedParts) {
         for (EnumFacing side : Arrays.asList(EnumFacing.UP, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST)) {
            Block neighbor = mc.theWorld.getBlockState(partPos.offset(side)).getBlock();
            if (BlockUtil.isReplaceable(neighbor)) {
               return null;
            }
            if (!(neighbor instanceof BlockBed)) {
               candidates.add(partPos.offset(side));
            }
         }
      }

      if (candidates.isEmpty()) {
         return null;
      }

      candidates.sort((p1, p2) -> {
         int cmp = Float.compare(getBlockBreakSpeed(p2), getBlockBreakSpeed(p1));
         if (cmp != 0) return cmp;
         return Double.compare(
            p1.distanceSqToCenter(mc.thePlayer.posX, mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ),
            p2.distanceSqToCenter(mc.thePlayer.posX, mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ)
         );
      });
      return candidates.get(0);
   }

}
