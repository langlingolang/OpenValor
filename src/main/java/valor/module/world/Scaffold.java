package valor.module.world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.world.WorldSettings;
import org.lwjgl.input.Keyboard;
import valor.Valor;
import valor.core.BlinkType;
import valor.core.MovementManager;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.HitBlockEvent;
import valor.events.LeftClickMouseEvent;
import valor.events.LivingUpdateEvent;
import valor.events.MoveInputEvent;
import valor.events.PacketEvent;
import valor.events.Render2DEvent;
import valor.events.RightClickMouseEvent;
import valor.events.StrafeEvent;
import valor.events.SwapItemEvent;
import valor.events.UpdateEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.module.movement.LongJump;
import valor.module.world.BedNuker;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;
import valor.setting.PercentageProperty;
import valor.util.BlockUtil;
import valor.util.DraggableHudElement;
import valor.util.FloatInterpolator;
import valor.util.GlScaleUtil;
import valor.util.HudDragManager;
import valor.util.ItemUtil;
import valor.util.MoveUtil;
import valor.util.PacketUtil;
import valor.util.PlayerUtil;
import valor.util.RandomUtil;
import valor.util.RenderUtil;
import valor.util.RotationUtil;
import valor.util.font.Fonts;

public class Scaffold extends Module implements DraggableHudElement {
    public static class PlaceData {
        public final BlockPos blockPos;
        public final EnumFacing facing;

        public PlaceData(BlockPos blockPos, EnumFacing facing) {
            this.blockPos = blockPos;
            this.facing = facing;
        }

        public BlockPos getBlockPos() {
            return this.blockPos;
        }

        public EnumFacing getFacing() {
            return this.facing;
        }

    }

    static class SyntheticSwitchMap {
        static final int[] I = new int[EnumFacing.values().length];
        static {
            try { I[EnumFacing.NORTH.ordinal()] = 1; } catch (NoSuchFieldError ignored) {}
            try { I[EnumFacing.EAST.ordinal()] = 2; } catch (NoSuchFieldError ignored) {}
            try { I[EnumFacing.SOUTH.ordinal()] = 3; } catch (NoSuchFieldError ignored) {}
            try { I[EnumFacing.WEST.ordinal()] = 4; } catch (NoSuchFieldError ignored) {}
            try { I[EnumFacing.UP.ordinal()] = 5; } catch (NoSuchFieldError ignored) {}
            try { I[EnumFacing.DOWN.ordinal()] = 6; } catch (NoSuchFieldError ignored) {}
        }
    }

    public int blockSlot = -1;

    private static final Minecraft mc;
    private static final double[] BLOCK_OFFSETS;
private int tellyStartTicks= 0;

    private int blocksPlaced= -1;
    private float targetYaw = -180.0f;
    private float targetPitch = 0.0f;
    private boolean hasRotations = false;
    private int strafeStage= 0;
    private int groundDelayTicks= 0;
    private int keepYTicks= 0;
    private float prevYaw= 0.0f;
    private float prevDeltaYaw= 0.0f;
    private int targetY= 256;
    private boolean shouldUpdateY= false;
    private boolean tellyPlacing= false;
    private EnumFacing targetFacing = null;
    private int freezeMovementTicks= 0;
    private int placeDelayTicks= 0;
    private double prevFallDistance= 0.0;
    private double prevMotionX;
    private double prevMotionY;
    private double prevMotionZ;
    private boolean motionSaved= false;
    private boolean wasSneaking= false;
    private boolean placedThisTick= false;
    private float lastPlacedYaw= Float.NaN;
    private float lastPlacedPitch= Float.NaN;
    private BlockPos lastPlacedPos= null;
    private EnumFacing lastPlacedFacing= null;
    private int placeCooldownTicks= 0;
    private long lastPlacementTime= 0L;
    private int speedPotionTicks= 0;
    private float lockedYaw= Float.NaN;
    private float lockedPitch= Float.NaN;
    private BlockPos lockedPos= null;
    private EnumFacing lockedFacing= null;
    private float rotDeltaYaw= 0.0f;
    private float rotDeltaPitch= 0.0f;
    private float rotDistance= 0.0f;
    private boolean isRotationLocked= false;
    private float targetLookYaw= Float.NaN;
    private float targetLookPitch= Float.NaN;
    private boolean tellyFalling= false;
    private float basePitch= 62.0f;
    private boolean rotationsAligned= false;
    private int alignedTicks= 0;
    private int facingTicks= 0;
    private boolean hypixelTellyJumping= false;
    private boolean jumpKeyHandled= false;
    private int airTicks3FMC= 0;
    private int groundTicks3FMC= 0;
    private int delayTicks3FMC= 0;
    private boolean canPlaceOnGround= true;
    private float lastRotationStep= Float.NaN;
    private boolean strafeFlag= false;
    private boolean lagbackDetected= false;
    private int strafeCooldownTicks= 0;
    private int jumpCooldownTicks= 0;
    private boolean hypixelTellyInAir= false;
    private boolean hypixelTellyOnGround= false;
    private int hypixelTellyTicks= 0;
    private float hypixelTellyYaw= Float.NaN;
    private float hypixelTellyStrafeYaw= Float.NaN;
    private int tellyBlockCount= 1;
    private int tellyDelayTicks= 0;
    private BlockPos tellyTargetPos= null;
    private BlockPos tellyStartPos= null;
    private boolean tellyActive= false;
    private boolean tellyWaiting= false;
    private int tellyStage= 0;
    private float tellyMotionYaw= Float.NaN;
    private boolean tellyJumped= false;
    private boolean tellyRotating= false;
    private long lastUpdateTime= 0L;
    private boolean hypixelTellyFast= false;
    public final ModeProperty rotationsProp = new ModeProperty("rotations", Integer.valueOf(2), new String[]{"NONE", "DEFAULT", "BACKWARDS", "SIDEWAYS", "GODBIRGDE", "SMOOTH", "Hypixel", "SNAP", "3FMC"});
    public final FloatProperty tellyStartRotMinSpeedProp = new FloatProperty("telly-start-rotation-min-speed", Float.valueOf(90.0f), Float.valueOf(1.0f), Float.valueOf(180.0f), () -> this.isTellyMode() || (Integer)this.fastTellyProp.getValue() >= 3);
    public final FloatProperty tellyStartRotMaxSpeedProp = new FloatProperty("telly-start-rotation-max-speed", Float.valueOf(95.0f), Float.valueOf(1.0f), Float.valueOf(180.0f), () -> this.isTellyMode() || (Integer)this.fastTellyProp.getValue() >= 3);
    public final FloatProperty tellyNormalRotMinSpeedProp = new FloatProperty("telly-normal-rotation-min-speed", Float.valueOf(30.0f), Float.valueOf(1.0f), Float.valueOf(180.0f), () -> this.isTellyMode() || (Integer)this.fastTellyProp.getValue() >= 3);
    public final FloatProperty tellyNormalRotMaxSpeedProp = new FloatProperty("telly-normal-rotation-max-speed", Float.valueOf(35.0f), Float.valueOf(1.0f), Float.valueOf(180.0f), () -> this.isTellyMode() || (Integer)this.fastTellyProp.getValue() >= 3);
    public final ModeProperty moveFixProp = new ModeProperty("move-fix", Integer.valueOf(1), new String[]{"NONE", "SILENT"});
    public final ModeProperty sprintProp = new ModeProperty("sprint", Integer.valueOf(0), new String[]{"NONE", "VANILLA"});
    public final PercentageProperty groundMotionProp = new PercentageProperty("ground-motion", Integer.valueOf(100));
    public final PercentageProperty airMotionProp = new PercentageProperty("air-motion", Integer.valueOf(100));
    public final PercentageProperty speedMotionProp = new PercentageProperty("speed-motion", Integer.valueOf(100));
    public final ModeProperty towerProp = new ModeProperty("tower", Integer.valueOf(0), new String[]{"NONE", "VANILLA", "EXTRA", "TELLY"});
    public final BooleanProperty hypixelTowerProp = new BooleanProperty("hypixeltower", Boolean.FALSE, () -> (Integer)this.towerProp.getValue() == 3);
    public final BooleanProperty safeProp = new BooleanProperty("safe", Boolean.FALSE, () -> (Integer)this.towerProp.getValue() == 3);
    public final IntProperty safeDelayTicksProp = new IntProperty("safe-delay-ticks", Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(3), () -> (Integer)this.towerProp.getValue() == 3 && (Boolean)this.safeProp.getValue() != false);
    public final ModeProperty keepYProp = new ModeProperty("keep-y", Integer.valueOf(0), new String[]{"NONE", "VANILLA", "EXTRA", "TELLY", "EXTRATELLY"});
    public final BooleanProperty keepYOnPressProp = new BooleanProperty("keep-y-on-press", Boolean.FALSE, () -> (Integer)this.keepYProp.getValue() != 0);
    public final BooleanProperty noKeepYOnJumpPotionProp = new BooleanProperty("no-keep-y-on-jump-potion", Boolean.FALSE, () -> (Integer)this.keepYProp.getValue() != 0);
    public final ModeProperty fastTellyProp = new ModeProperty("fast-telly", Integer.valueOf(0), new String[]{"OFF", "GRIM", "POLAR", "CLUTCH", "HYPIXEL"}, () -> this.isTellyMode());
    public final FloatProperty minCpsProp = new FloatProperty("min-cps", Float.valueOf(14.0f), Float.valueOf(1.0f), Float.valueOf(20.0f));
    public final FloatProperty maxCpsProp = new FloatProperty("max-cps", Float.valueOf(18.0f), Float.valueOf(1.0f), Float.valueOf(20.0f));
    public final BooleanProperty multiPlaceProp = new BooleanProperty("multi-place", Boolean.TRUE);
    public final BooleanProperty swingProp = new BooleanProperty("swing", Boolean.TRUE);
    public final BooleanProperty itemSpoofProp = new BooleanProperty("item-spoof", Boolean.FALSE);

    public final BooleanProperty blockCounterProp = new BooleanProperty("block-counter", Boolean.TRUE);
    public final FloatProperty counterXProp = new FloatProperty("counter-x", Float.valueOf(-1.0f), Float.valueOf(-1.0f), Float.valueOf(4000.0f), () -> (Boolean)this.blockCounterProp.getValue());
    public final FloatProperty counterYProp = new FloatProperty("counter-y", Float.valueOf(-1.0f), Float.valueOf(-1.0f), Float.valueOf(4000.0f), () -> (Boolean)this.blockCounterProp.getValue());
    private float defaultPitch= 72.0f;
    private float defaultStrafeAngle= 18.0f;
    private boolean initFlag= false;
    private final FloatInterpolator counterInterpolator = new FloatInterpolator(0.0f);
    public final BooleanProperty eagleProp = new BooleanProperty("eagle", Boolean.FALSE);
    public final FloatProperty edgeDistanceProp = new FloatProperty("edge-distance", Float.valueOf(0.13f), Float.valueOf(0.0f), Float.valueOf(0.5f), () -> (Boolean)this.eagleProp.getValue());
    public final IntProperty sneakDelayProp = new IntProperty("sneak-delay", Integer.valueOf(80), Integer.valueOf(0), Integer.valueOf(500), () -> (Boolean)this.eagleProp.getValue());
    public final IntProperty blocksPerSneakProp = new IntProperty("blocks-per-sneak", Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(5), () -> (Boolean)this.eagleProp.getValue());
    private boolean eagleSneaking= false;
    private int eagleSneakTicks= 0;
    private long lastEagleSneakTime= 0L;
    private int eagleBlocksPlaced= 0;

    private static boolean isSteppingOffEdge() {
        return Scaffold.isOverAirAhead(1);
    }

    private static boolean isOverAirAhead(int n) {
        double d2 = mc.thePlayer.posX;
        double d3 = mc.thePlayer.posZ;
        double d4 = mc.thePlayer.motionX;
        double d5 = mc.thePlayer.motionZ;
        double d6 = mc.thePlayer.posY - 0.5;
        for (int i = 0; i < n; ++i) {
            AxisAlignedBB axisAlignedBB;
            if (!mc.theWorld.getCollidingBoundingBoxes((Entity)mc.thePlayer, axisAlignedBB = new AxisAlignedBB((d2 += d4) - 0.3, d6, (d3 += d5) - 0.3, d2 + 0.3, d6 + 1.8, d3 + 0.3)).isEmpty()) continue;
            return true;
        }
        return false;
    }

    private boolean isReplaceableAhead(int n) {
        int n2 = this.getScaffoldY();
        double d2 = mc.thePlayer.posX;
        double d3 = mc.thePlayer.posZ;
        double d4 = mc.thePlayer.motionX;
        double d5 = mc.thePlayer.motionZ;
        double d6 = d4;
        double d7 = d5;
        if (d6 * d6 + d7 * d7 < 0.0025) {
            float f = Scaffold.calculateInputMoveYaw() * ((float)Math.PI / 180);
            d4 = (double)(-MathHelper.sin((float)f)) * 0.45;
            d5 = (double)MathHelper.cos((float)f) * 0.45;
        }
        for (int i = 0; i <= n; ++i) {
            if (BlockUtil.isReplaceable((BlockPos)new BlockPos(MathHelper.floor_double((double)d2), n2, MathHelper.floor_double((double)d3)))) {
                return true;
            }
            d2 += d4;
            d3 += d5;
        }
        return false;
    }

    private boolean shouldPlaceBlock() {
        if (this.isTellyActive() || this.isHypixelRotationsActive()) {
            return this.placeCooldownTicks <= 0;
        }
        if (this.lastPlacementTime <= 0L) {
            return true;
        }
        return Scaffold.isBlockBelowReplaceable() || Scaffold.isOverAirAhead(1) || this.isReplaceableAhead(2);
    }

    private boolean isTellyMode() {
        int n = (Integer)this.keepYProp.getValue();
        return n == 3 || n == 4;
    }

    private boolean isTellyActive() {
        if (this.isJumping()) {
            return false;
        }
        return this.isTellyMode() || (Integer)this.towerProp.getValue() == 3;
    }

    private boolean isJumping() {
        boolean bl;
        boolean bl2 = bl = mc.gameSettings != null && mc.gameSettings.keyBindJump.isKeyDown();
        if (this.jumpKeyHandled) {
            return false;
        }
        return bl || this.hypixelTellyJumping && mc.thePlayer != null && !mc.thePlayer.onGround;
    }

    private void updateJumpState() {
        boolean bl;
        if (mc.gameSettings == null || mc.thePlayer == null) {
            this.jumpKeyHandled = false;
            return;
        }
        if (!mc.gameSettings.keyBindJump.isKeyDown()) {
            this.jumpKeyHandled = false;
            return;
        }
        if (this.jumpKeyHandled) {
            if (mc.thePlayer.onGround) {
                this.jumpKeyHandled = false;
            }
            return;
        }
        boolean bl2 = bl = this.isTellyMode() || (Integer)this.towerProp.getValue() == 3;
        if (!this.hypixelTellyJumping && bl && !mc.thePlayer.onGround) {
            this.jumpKeyHandled = true;
        }
    }

    private boolean isHypixelTelly() {
        return this.isTellyMode() && (Integer)this.fastTellyProp.getValue() == 4;
    }

    private static int getPing() {
        if (mc.thePlayer == null || mc.getNetHandler() == null) {
            return 0;
        }
        NetworkPlayerInfo networkPlayerInfo = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        if (networkPlayerInfo == null) {
            return 0;
        }
        return Math.max(0, networkPlayerInfo.getResponseTime());
    }

    private static int getPingAdjustedCount(int n) {
        int n2 = Scaffold.getPing();
        if (n2 >= 140) {
            return Math.min(n, 1);
        }
        if (n2 >= 100) {
            return Math.min(n, 2);
        }
        return n;
    }

    private static boolean isHighPing() {
        return Scaffold.getPing() >= 140;
    }

    private boolean isHypixelTellyActive() {
        return this.isHypixelTelly() && !this.isJumping() && mc.gameSettings != null && mc.gameSettings.keyBindJump.isKeyDown() && MoveUtil.isMoving();
    }

    private boolean isHypixelTellyReady() {
        boolean bl;
        if (!this.isHypixelTellyActive() || this.tellyDelayTicks > 0) {
            return false;
        }
        boolean bl2 = this.tellyJumped || this.tellyTargetPos == null && !this.tellyActive;
        boolean bl3 = bl = this.tellyWaiting && this.tellyStage >= 2;
        return (bl2 || bl) && this.getYawDiffFromMove() <= 8.0f;
    }

    private float getMaxPitch() {
        if (this.isHypixelTellyActive() && this.tellyActive) {
            return 88.0f;
        }
        return 80.0f;
    }

    private boolean isHypixelTellyTicking() {
        return this.hypixelTellyFast && this.isHypixelTelly() && !this.isJumping();
    }

    private boolean isHypixelRotations() {
        return (Integer)this.rotationsProp.getValue() == 6;
    }

    private boolean isHypixelRotationsActive() {
        if (this.isJumping()) {
            return false;
        }
        return this.isHypixelRotations() && !this.isTellyActive();
    }

    private boolean isClutchTelly() {
        return this.isTellyActive() && (Integer)this.fastTellyProp.getValue() == 3;
    }

    private boolean isHypixelFastTelly() {
        return this.isTellyActive() && (Integer)this.fastTellyProp.getValue() == 4;
    }

    private boolean isClutchOrHypixelFastTelly() {
        return this.isClutchTelly() || this.isHypixelFastTelly();
    }

    private boolean is3FMCRotations() {
        return (Integer)this.rotationsProp.getValue() == 8;
    }

    private boolean is3FMCTelly() {
        return this.is3FMCRotations() && this.isTellyActive();
    }

    private void update3FMCState() {
        if (!this.is3FMCRotations() || mc.thePlayer == null) {
            this.airTicks3FMC = 0;
            this.groundTicks3FMC = 0;
            this.delayTicks3FMC = 0;
            return;
        }
        if (mc.thePlayer.onGround) {
            ++this.groundTicks3FMC;
            this.airTicks3FMC = 0;
        } else {
            ++this.airTicks3FMC;
            this.groundTicks3FMC = 0;
        }
        if (this.delayTicks3FMC > 0) {
            --this.delayTicks3FMC;
        }
    }

    private void stopSprint3FMC() {
        if (!this.is3FMCRotations() || this.is3FMCTelly() || mc.thePlayer == null) {
            return;
        }
        mc.thePlayer.setSprinting(false);
        if (mc.gameSettings != null) {
            KeyBinding.setKeyBindState((int)mc.gameSettings.keyBindSprint.getKeyCode(), (boolean)false);
        }
    }

    private static float smoothstep(float f) {
        f = MathHelper.clamp_float((float)f, (float)0.0f, (float)1.0f);
        return f * f * (3.0f - f * 2.0f);
    }

    private float getRandomRotationSpeed() {
        if (this.tellyStartTicks >= 2) {
            return RandomUtil.nextFloat((float)((Float)this.tellyStartRotMinSpeedProp.getValue()).floatValue(), (float)((Float)this.tellyStartRotMaxSpeedProp.getValue()).floatValue());
        }
        return RandomUtil.nextFloat((float)((Float)this.tellyNormalRotMinSpeedProp.getValue()).floatValue(), (float)((Float)this.tellyNormalRotMaxSpeedProp.getValue()).floatValue());
    }

    private void initRotationDelta(float f, float f2, float f3, float f4) {
        this.rotDeltaYaw = f;
        this.rotDeltaPitch = f2;
        f = MathHelper.wrapAngleTo180_float((float)(f3 - f));
        f2 = f4 - f2;
        float f5 = f;
        float f6 = f2;
        this.rotDistance = Math.max(0.001f, (float)Math.sqrt(f5 * f5 + f6 * f6));
    }

    private float[] smoothRotations(float f, float f2, float f3, float f4, float f5) {
        float f6;
        float f7;
        float f8 = MathHelper.wrapAngleTo180_float((float)(f3 - f));
        float f9 = f4 - f2;
        float f10 = f8;
        float f11 = f9;
        float f12 = (float)Math.sqrt(f10 * f10 + f11 * f11);
        if (f12 < 0.001f) {
            return new float[]{f3, MathHelper.clamp_float((float)f4, (float)-90.0f, (float)90.0f)};
        }
        f3 = MathHelper.wrapAngleTo180_float((float)(f - this.rotDeltaYaw));
        f4 = f2 - this.rotDeltaPitch;
        float f13 = f3;
        float f14 = f4;
        f3 = MathHelper.clamp_float((float)((float)Math.sqrt(f13 * f13 + f14 * f14) / this.rotDistance), (float)0.0f, (float)1.0f);
        f3 = f3 < 0.18f ? 0.35f + 0.65f * Scaffold.smoothstep(f3 / 0.18f) : (f3 > 0.82f ? 0.35f + 0.65f * Scaffold.smoothstep((1.0f - f3) / 0.18f) : 1.0f);
        if ((f3 = Math.min(f5 * f3, f12)) < (f4 = RotationUtil.getMouseGCD()) && f12 > f4) {
            f3 = f4;
        }
        f2 = MathHelper.clamp_float((float)(f2 + f9 * f3), (float)-90.0f, (float)90.0f);
        return new float[]{f += f8 * (f3 /= f12), f2};
    }

    private static boolean isRotationClose(float f, float f2, float f3, float f4) {
        float f5 = Math.max(RotationUtil.getMouseGCD() * 2.0f, 0.15f);
        f = Math.abs(MathHelper.wrapAngleTo180_float((float)(f3 - f)));
        f2 = Math.abs(f4 - f2);
        return f <= f5 && f2 <= f5;
    }

    private boolean isRotationLocked(PlaceData o0, float f, float f2) {
        if (!this.isRotationLocked || this.lockedPos == null || this.lockedFacing == null) {
            return true;
        }
        if (!this.lockedPos.equals((Object)o0.blockPos) || this.lockedFacing != o0.facing) {
            return true;
        }
        if (Float.isNaN(this.lockedYaw) || Float.isNaN(this.lockedPitch)) {
            return true;
        }
        float f3 = Math.abs(MathHelper.wrapAngleTo180_float((float)(f - this.lockedYaw)));
        f = Math.abs(f2 - this.lockedPitch);
        return f3 > 1.5f || f > 1.5f;
    }

    private boolean isMovingDiagonally() {
        if (!this.isHypixelFastTelly() || mc.thePlayer == null) {
            return false;
        }
        int n = MoveUtil.getForwardInput();
        int n2 = MoveUtil.getStrafeInput();
        if (Math.abs(n) > 0 && Math.abs(n2) > 0) {
            return true;
        }
        if (Math.abs(n) > 0 || Math.abs(n2) > 0) {
            return Scaffold.isAngleDiagonal(Scaffold.calculateInputMoveYaw());
        }
        if (Math.abs(mc.thePlayer.motionX) > 0.04 && Math.abs(mc.thePlayer.motionZ) > 0.04) {
            return true;
        }
        if (Scaffold.isAngleDiagonal(Scaffold.calculateInputMoveYaw())) {
            return true;
        }
        return Math.abs(MoveUtil.getForwardInput()) > 0 && MoveUtil.getBaseSpeed() > 0.08 && Scaffold.isAngleDiagonal(MoveUtil.getMoveYaw());
    }

    private void updateDiagonalMoveYaw() {
        float f;
        if (mc.thePlayer == null) {
            return;
        }
        float f2 = Float.NaN;
        if (Math.abs(MoveUtil.getForwardInput()) > 0 && Math.abs(MoveUtil.getStrafeInput()) > 0) {
            f2 = Scaffold.calculateInputMoveYaw();
        } else if (MoveUtil.getBaseSpeed() > 0.1 && Math.abs(mc.thePlayer.motionX) > 0.05 && Math.abs(mc.thePlayer.motionZ) > 0.05) {
            f2 = MoveUtil.getMoveYaw();
        } else {
            f = MathHelper.wrapAngleTo180_float((float)mc.thePlayer.rotationYaw);
            if (Scaffold.isAngleDiagonal(f)) {
                f2 = f;
            } else if (Scaffold.isAngleDiagonal(Scaffold.calculateInputMoveYaw())) {
                f2 = Scaffold.calculateInputMoveYaw();
            }
        }
        if (!Float.isNaN(f2)) {
            if (Float.isNaN(this.hypixelTellyYaw) || Math.abs(MathHelper.wrapAngleTo180_float((float)(f2 - this.hypixelTellyYaw))) > 18.0f) {
                this.hypixelTellyYaw = f2;
                return;
            }
            f = MathHelper.wrapAngleTo180_float((float)(f2 - this.hypixelTellyYaw));
            this.hypixelTellyYaw = MathHelper.wrapAngleTo180_float((float)(this.hypixelTellyYaw + f * 0.3f));
            return;
        }
        if (Float.isNaN(this.hypixelTellyYaw)) {
            f2 = MathHelper.wrapAngleTo180_float((float)mc.thePlayer.rotationYaw);
            float[] fArray = new float[]{45.0f, 135.0f, -45.0f, -135.0f};
            float f3 = fArray[0];
            float f4 = Float.MAX_VALUE;
            int cfr_ignored_0 = fArray.length;
            for (int i = 0; i < 4; ++i) {
                float f5;
                float f6 = fArray[i];
                float f7 = Math.abs(MathHelper.wrapAngleTo180_float((float)(f2 - f6)));
                if (!(f7 < f4)) continue;
                f4 = f7;
                f3 = f6;
            }
            this.hypixelTellyYaw = f3;
        }
    }

    private boolean isTellyStrafeActive() {
        if (!this.isHypixelFastTelly()) {
            return false;
        }
        Scaffold ooOoOo = this;
        if (!this.isHypixelFastTelly()) {
            this.hypixelTellyOnGround = false;
            this.hypixelTellyInAir = false;
            this.hypixelTellyTicks = 0;
            this.hypixelTellyYaw = Float.NaN;
            this.hypixelTellyStrafeYaw = Float.NaN;
        } else if (this.isMovingDiagonally()) {
            this.hypixelTellyOnGround = true;
            this.hypixelTellyInAir = true;
            this.hypixelTellyTicks = 0;
            this.updateDiagonalMoveYaw();
        } else if (mc.thePlayer != null && !mc.thePlayer.onGround && this.hypixelTellyOnGround) {
            this.hypixelTellyInAir = true;
            this.updateDiagonalMoveYaw();
        } else if (mc.thePlayer != null && mc.thePlayer.onGround) {
            if ((Math.abs(MoveUtil.getForwardInput()) > 0 || Math.abs(MoveUtil.getStrafeInput()) > 0) && !this.isMovingDiagonally()) {
                this.hypixelTellyOnGround = false;
                this.hypixelTellyInAir = false;
                this.hypixelTellyTicks = 0;
                this.hypixelTellyYaw = Float.NaN;
                this.hypixelTellyStrafeYaw = Float.NaN;
            } else {
                ++this.hypixelTellyTicks;
                if (this.hypixelTellyTicks >= 20) {
                    this.hypixelTellyOnGround = false;
                    this.hypixelTellyInAir = false;
                    this.hypixelTellyTicks = 0;
                    this.hypixelTellyYaw = Float.NaN;
                    this.hypixelTellyStrafeYaw = Float.NaN;
                } else if (this.hypixelTellyOnGround) {
                    this.updateDiagonalMoveYaw();
                }
            }
        }
        if (this.hypixelTellyOnGround || this.hypixelTellyInAir) {
            return true;
        }
        return this.isMovingDiagonally();
    }

    private float getTellyStrafeYaw() {
        float f;
        float f2 = this.getBasePlacementYaw();
        if (!Float.isNaN(this.hypixelTellyStrafeYaw)) {
            float f3;
            f = Math.abs(MathHelper.wrapAngleTo180_float((float)(this.hypixelTellyStrafeYaw - f2)));
            if (f >= 110.0f && f <= 150.0f) {
                return this.hypixelTellyStrafeYaw;
            }
        }
        f = Float.isNaN(this.targetLookYaw) ? f2 : this.targetLookYaw;
        f = MathHelper.wrapAngleTo180_float((float)(f2 + 180.0f - f)) >= 0.0f ? 1.0f : -1.0f;
        this.hypixelTellyStrafeYaw = MathHelper.wrapAngleTo180_float((float)(f2 + f * RandomUtil.nextFloat((float)118.0f, (float)142.0f)));
        return this.hypixelTellyStrafeYaw;
    }

    private float[] adjustRotationsForFacing(float f, float f2, PlaceData o0) {
        if (o0 == null) {
            return new float[]{f, f2};
        }
        float f3 = this.getBasePlacementYaw();
        if (Math.abs(MathHelper.wrapAngleTo180_float((float)(f - f3))) <= 142.0f) {
            return new float[]{f, f2};
        }
        float f4 = MathHelper.wrapAngleTo180_float((float)(f - f3)) >= 0.0f ? 1.0f : -1.0f;
        for (float f5 = 120.0f; f5 <= 155.0f; f5 += 5.0f) {
            float f6 = MathHelper.wrapAngleTo180_float((float)(f3 + f4 * f5));
            if (Scaffold.rayTraceFacing(o0, f6, f2) != null) {
                return new float[]{f6, f2};
            }
            if (Scaffold.rayTraceFacing(o0, f6, 72.0f) != null) {
                return new float[]{f6, 72.0f};
            }
            float f7 = MathHelper.clamp_float((float)f2, (float)62.0f, (float)80.0f);
            if (Scaffold.rayTraceFacing(o0, f6, f7) == null) continue;
            return new float[]{f6, f7};
        }
        return new float[]{f, f2};
    }

    private float selectYawMode(float f, float f2) {
        if ((Integer)this.rotationsProp.getValue() == 3) {
            return f2;
        }
        return f;
    }

    private boolean canPlaceOnEdge() {
        double d2;
        boolean bl;
        boolean bl2 = bl = this.isHypixelTellyActive() && this.tellyActive && !this.tellyWaiting;
        if (mc.thePlayer.onGround && !bl) {
            return false;
        }
        if (this.isHypixelFastTelly()) {
            float f;
            if (this.isHypixelTellyActive()) {
                BlockPos blockPos;
                if (this.tellyWaiting) {
                    this.lagbackDetected = true;
                    return false;
                }
                if (this.tellyActive) {
                    this.lagbackDetected = false;
                    return true;
                }
                if (this.tellyTargetPos != null && this.strafeCooldownTicks > 0 && mc.thePlayer.motionY < (double)0.12f && (blockPos = this.findTellyBlockPos()) != null) {
                    this.tellyStartPos = blockPos;
                    this.tellyActive = true;
                    this.hasRotations = false;
                    this.lastPlacedPos = null;
                    this.lastPlacedFacing = null;
                    this.lagbackDetected = false;
                    return true;
                }
            }
            boolean bl3 = this.isTellyStrafeActive();
            int n = this.jumpCooldownTicks <= 2 || bl3 ? 1 : 0;
            float f2 = f = n != 0 ? 0.58f : 0.48f;
            if (mc.thePlayer.motionY >= (double)f) {
                return false;
            }
            boolean bl4 = this.isBlockUnderReplaceable();
            int n2 = n = n != 0 ? 3 : 2;
            if (this.isMovingDiagonally()) {
                this.hypixelTellyInAir = true;
            }
            if (bl3) {
                bl3 = this.isFallingBelowScaffold();
                n = this.jumpCooldownTicks <= 2 ? 3 : 2;
                boolean bl5 = this.hasLineOfSight(3);
                if (!(!this.lagbackDetected || this.strafeCooldownTicks < n || bl4 && bl3)) {
                    return false;
                }
                if (bl3 || bl5 && this.strafeCooldownTicks < n) {
                    this.lagbackDetected = false;
                    return true;
                }
                this.lagbackDetected = true;
                return false;
            }
            bl3 = this.isFallingBelowScaffold();
            if (this.lagbackDetected && !bl3) {
                return false;
            }
            if (bl4 || bl3) {
                this.lagbackDetected = false;
            }
            if (bl4 || bl3 || this.strafeCooldownTicks < n) {
                this.lagbackDetected = false;
                return true;
            }
            if (this.strafeCooldownTicks >= n && !bl4 && !bl3) {
                this.lagbackDetected = true;
                return false;
            }
            if (this.strafeCooldownTicks >= n - 1 && mc.thePlayer.motionY < -0.35 && !bl4 && !bl3) {
                this.lagbackDetected = true;
                return false;
            }
            if (this.strafeFlag && !bl4 && !bl3 && this.strafeCooldownTicks >= n) {
                this.lagbackDetected = true;
            }
            return false;
        }
        double d3 = d2 = (Integer)this.fastTellyProp.getValue() == 1 ? 0.28 : 0.15;
        return !(mc.thePlayer.motionY >= d2);
    }

    private boolean hasLineOfSight(int n) {
        double d2;
        double d3;
        double d4;
        double d5;
        int n2;
        block12: {
            double d6;
            block11: {
                n2 = this.getScaffoldY();
                d5 = mc.thePlayer.posX;
                d4 = mc.thePlayer.posZ;
                d3 = mc.thePlayer.motionX;
                d2 = mc.thePlayer.motionZ;
                if (this.isTellyStrafeActive()) {
                    double d7 = d3;
                    double d8 = d2;
                    double d9 = Math.sqrt(d7 * d7 + d8 * d8);
                    double d10 = Math.max(0.45, Math.min(0.72, d9 > 0.08 ? d9 : 0.55));
                    double d11 = Math.max(0.5, d10 * (double)Math.max(1, n));
                    Iterator<BlockPos> iterator = this.getCandidateBlockPositions(n2, d11).iterator();
                    while (iterator.hasNext()) {
                        if (!BlockUtil.isReplaceable((BlockPos)iterator.next())) continue;
                        return true;
                    }
                    return false;
                }
                if (this.isHypixelFastTelly()) break block11;
                double d12 = d3;
                double d13 = d2;
                if (!(d12 * d12 + d13 * d13 < 0.0025)) break block12;
            }
            float f = (this.isTellyStrafeActive() ? this.getMoveOrStrafeYaw() : this.getBasePlacementYaw()) * ((float)Math.PI / 180);
            if (this.isHypixelFastTelly()) {
                double d14;
                double d15 = d3;
                double d16 = d2;
                if (Math.sqrt(d15 * d15 + d16 * d16) > 0.08) {
                    double d17 = d3;
                    double d18 = d2;
                    d14 = Math.sqrt(d17 * d17 + d18 * d18);
                } else {
                    d14 = 0.55;
                }
                d6 = Math.max(0.45, Math.min(0.72, d14));
            } else {
                d6 = 0.55;
            }
            double d19 = d6;
            d3 = (double)(-MathHelper.sin((float)f)) * d19;
            d2 = (double)MathHelper.cos((float)f) * d19;
        }
        for (int i = 0; i <= n; ++i) {
            if (BlockUtil.isReplaceable((BlockPos)new BlockPos(MathHelper.floor_double((double)d5), n2, MathHelper.floor_double((double)d4)))) {
                return true;
            }
            d5 += d3;
            d4 += d2;
        }
        return false;
    }

    private static boolean willHitGroundSoon() {
        if (mc.thePlayer.onGround || mc.thePlayer.motionY >= 0.0) {
            return false;
        }
        AxisAlignedBB axisAlignedBB = mc.thePlayer.getEntityBoundingBox();
        double d2 = mc.thePlayer.motionY;
        double d3 = 0.0;
        int n = Math.max(1, 8);
        for (int i = 0; i < n; ++i) {
            if (!mc.theWorld.getCollidingBoundingBoxes((Entity)mc.thePlayer, axisAlignedBB.offset(0.0, d3 += d2, 0.0)).isEmpty()) {
                return true;
            }
            d2 = (d2 - 0.08) * 0.98;
        }
        return false;
    }

    private boolean isFallingBelowScaffold() {
        int n = this.getScaffoldY();
        boolean bl = this.isTellyStrafeActive();
        double d2 = (double)n + 1.0;
        double d3 = mc.thePlayer.posX;
        double d4 = mc.thePlayer.posY;
        double d5 = mc.thePlayer.posZ;
        double d6 = mc.thePlayer.motionX;
        double d7 = mc.thePlayer.motionY;
        double d8 = mc.thePlayer.motionZ;
        int n2 = Math.max(1, 12);
        for (int i = 0; i < n2; ++i) {
            d3 += d6;
            d4 += d7;
            d5 += d8;
            if (d7 <= 0.0 && d4 <= d2 + 0.05) {
                if (bl) {
                    AxisAlignedBB axisAlignedBB = new AxisAlignedBB(d3 - 0.28, d2 - 0.05, d5 - 0.28, d3 + 0.28, d2 + 0.05, d5 + 0.28);
                    return mc.theWorld.getCollidingBoundingBoxes((Entity)mc.thePlayer, axisAlignedBB).isEmpty();
                }
                return BlockUtil.isReplaceable((BlockPos)new BlockPos(MathHelper.floor_double((double)d3), n, MathHelper.floor_double((double)d5)));
            }
            d6 *= bl ? 0.98 : 0.91;
            d8 *= bl ? 0.98 : 0.91;
            d7 = (d7 - 0.08) * 0.98;
        }
        return true;
    }

    private static boolean isFalling() {
        return !mc.thePlayer.onGround && mc.thePlayer.motionY < 0.0;
    }

    private boolean canPlaceWhileFalling() {
        return Scaffold.isFalling() || (Integer)this.fastTellyProp.getValue() == 1 && this.isTellyActive() && !mc.thePlayer.onGround && mc.thePlayer.motionY < 0.1;
    }

    private int getScaffoldY() {
        int n = MathHelper.floor_double((double)mc.thePlayer.posY);
        if (this.isHypixelTellyActive()) {
            if (this.tellyActive) {
                return this.targetY;
            }
            return this.targetY - 1;
        }
        return (this.keepYTicks != 0 && !this.shouldUpdateY ? Math.min(n, this.targetY) : n) - 1;
    }

    private BlockPos findTellyBlockPos() {
        BlockPos blockPos = new BlockPos(MathHelper.floor_double((double)mc.thePlayer.posX), this.targetY - 1, MathHelper.floor_double((double)mc.thePlayer.posZ));
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
        if (block.getMaterial().isLiquid() || !block.getMaterial().isSolid()) {
            return null;
        }
        if (BlockUtil.isReplaceable((BlockPos)blockPos.up())) {
            return blockPos;
        }
        return null;
    }

    private boolean isBlockUnderReplaceable() {
        return BlockUtil.isReplaceable((BlockPos)new BlockPos(MathHelper.floor_double((double)mc.thePlayer.posX), this.getScaffoldY(), MathHelper.floor_double((double)mc.thePlayer.posZ)));
    }

    private static boolean isBlockBelowReplaceable() {
        return BlockUtil.isReplaceable((BlockPos)new BlockPos(MathHelper.floor_double((double)mc.thePlayer.posX), MathHelper.floor_double((double)(mc.thePlayer.posY - 0.2)), MathHelper.floor_double((double)mc.thePlayer.posZ)));
    }

    private float getBasePlacementYaw() {
        if (this.isHypixelTellyActive() && !Float.isNaN(this.tellyMotionYaw)) {
            return this.tellyMotionYaw;
        }
        if (this.isHypixelFastTelly() && (this.hypixelTellyOnGround || this.hypixelTellyInAir) && !Float.isNaN(this.hypixelTellyYaw)) {
            return this.hypixelTellyYaw;
        }
        return Scaffold.calculateInputMoveYaw();
    }

    private void resetRotationLock() {
        this.isRotationLocked = false;
        this.lockedPos = null;
        this.lockedFacing = null;
        this.lockedYaw = Float.NaN;
        this.lockedPitch = Float.NaN;
        this.rotDistance = 0.0f;
        this.rotationsAligned = false;
        this.alignedTicks = 0;
    }

    private void applyGCDJitter(float f) {
        float f2;
        f = Math.abs(f - this.prevYaw);
        if (f >= 2.0f) {
            if (Math.abs(f - this.prevDeltaYaw) < 0.002f) {
                f = RotationUtil.getMouseGCD();
                this.targetYaw += f;
            }
            this.prevDeltaYaw = Math.abs(this.targetYaw - this.prevYaw);
        }
        this.prevYaw = this.targetYaw;
    }

    private boolean canFastTellyJump() {
        Scaffold ooOoOo = this;
        int n = (Integer)ooOoOo.fastTellyProp.getValue();
        return (n == 1 || n == 2) && this.isTellyActive() && this.keepYTicks > 0 && MoveUtil.isMoving() && !mc.gameSettings.keyBindJump.isKeyDown() && mc.thePlayer.onGround;
    }

    private boolean shouldTellyJump() {
        if (!(this.isHypixelFastTelly() && this.keepYTicks > 0 && this.tellyDelayTicks <= 0 && MoveUtil.isMoving() && mc.thePlayer.onGround)) {
            return false;
        }
        float f = this.getYawDiffFromMove();
        if (this.isHypixelTellyActive()) {
            return this.isHypixelTellyReady();
        }
        if (!this.isMovingFastOnGround()) {
            return false;
        }
        boolean bl = Scaffold.isOverAirAhead(1);
        if (f <= 8.0f) {
            return true;
        }
        if (this.isHypixelTellyTicking()) {
            int n = Math.max(2, this.tellyBlockCount);
            if (f <= 14.0f && this.facingTicks >= n) {
                return true;
            }
            return bl && f <= 28.0f;
        }
        int n = Math.max(1, this.tellyBlockCount);
        if (f <= 16.0f && this.facingTicks >= n) {
            return true;
        }
        return bl && f <= 30.0f;
    }

    private boolean isMovingFastOnGround() {
        if (!mc.thePlayer.onGround) {
            return false;
        }
        if (this.isTellyStrafeActive()) {
            double d2 = mc.thePlayer.motionX;
            double d3 = mc.thePlayer.motionZ;
            double d4 = d2;
            double d5 = d3;
            if (Math.sqrt(d4 * d4 + d5 * d5) < 0.08) {
                float f = this.getMoveOrStrafeYaw() * ((float)Math.PI / 180);
                d2 = (double)(-MathHelper.sin((float)f)) * 0.28;
                d3 = (double)MathHelper.cos((float)f) * 0.28;
            }
            if (BlockUtil.isReplaceable((BlockPos)new BlockPos(MathHelper.floor_double((double)(mc.thePlayer.posX + d2)), this.getScaffoldY(), MathHelper.floor_double((double)(mc.thePlayer.posZ + d3))))) {
                return true;
            }
        }
        if (Scaffold.isOverAirAhead(1)) {
            return true;
        }
        float f = (this.isTellyStrafeActive() ? this.getMoveOrStrafeYaw() : this.getBasePlacementYaw()) * ((float)Math.PI / 180);
        double d6 = -MathHelper.sin((float)f);
        double d7 = MathHelper.cos((float)f);
        double[] dArray = new double[]{0.18};
        double d8 = mc.thePlayer.posY - 0.5;
        int cfr_ignored_0 = dArray.length;
        for (int i = 0; i <= 0; ++i) {
            double d9 = dArray[0];
            double d10 = mc.thePlayer.posX + d6 * d9;
            double d11 = mc.thePlayer.posZ + d7 * d9;
            AxisAlignedBB axisAlignedBB = new AxisAlignedBB(d10 - 0.3, d8, d11 - 0.3, d10 + 0.3, d8 + 1.8, d11 + 0.3);
            if (!mc.theWorld.getCollidingBoundingBoxes((Entity)mc.thePlayer, axisAlignedBB).isEmpty()) continue;
            return true;
        }
        return false;
    }

    private float getYawDiffFromMove() {
        if (Float.isNaN(this.targetLookYaw)) {
            return Math.abs(MathHelper.wrapAngleTo180_float((float)(mc.thePlayer.rotationYaw - this.getBasePlacementYaw())));
        }
        return Math.abs(MathHelper.wrapAngleTo180_float((float)(this.targetLookYaw - this.getBasePlacementYaw())));
    }

    private void updateFacingAlignment(float f) {
        float f2;
        if (!this.isHypixelFastTelly()) {
            this.facingTicks = 0;
            return;
        }
        f = Math.abs(MathHelper.wrapAngleTo180_float((float)(f - this.getBasePlacementYaw())));
        if (f <= 5.0f) {
            ++this.facingTicks;
            return;
        }
        if (f > 8.0f) {
            this.facingTicks = 0;
        }
    }

    private void updateHypixelTellyState() {
        if (!this.isHypixelFastTelly()) {
            return;
        }
        boolean bl = mc.thePlayer.onGround;
        if (bl && this.tellyWaiting) {
            ++this.tellyStage;
        } else if (!bl) {
            this.tellyStage = 0;
        }
        if (this.canPlaceOnGround && !bl) {
            if (this.tellyWaiting) {
                this.targetY = MathHelper.floor_double((double)mc.thePlayer.posY);
            }
            this.tellyJumped = false;
            this.tellyTargetPos = null;
            this.tellyStartPos = null;
            this.tellyActive = false;
            this.tellyWaiting = false;
            this.tellyStage = 0;
            this.strafeFlag = false;
            this.lagbackDetected = false;
            this.strafeCooldownTicks = 0;
            ++this.jumpCooldownTicks;
            this.hypixelTellyFast = false;
            boolean bl2 = this.hypixelTellyInAir = this.isMovingDiagonally() || this.hypixelTellyOnGround;
            if (this.hypixelTellyInAir) {
                this.hypixelTellyOnGround = true;
                this.hasRotations = false;
                this.lastPlacedPos = null;
                this.lastPlacedFacing = null;
                this.hypixelTellyStrafeYaw = Float.NaN;
                this.updateDiagonalMoveYaw();
            }
        } else if (!this.canPlaceOnGround && bl) {
            float f;
            if (this.isHypixelTellyActive() && !this.tellyWaiting) {
                this.tellyJumped = true;
                this.tellyTargetPos = null;
                this.tellyStartPos = null;
                this.tellyActive = false;
                this.lagbackDetected = true;
                this.resetRotationLock();
            }
            float f2 = f = Float.isNaN(this.targetLookYaw) ? 999.0f : Math.abs(MathHelper.wrapAngleTo180_float((float)(this.targetLookYaw - this.getBasePlacementYaw())));
            if (this.isTellyStrafeActive()) {
                if (f > 36.0f || this.facingTicks <= 0) {
                    this.facingTicks = 0;
                    this.tellyBlockCount = 2;
                } else {
                    this.tellyBlockCount = 1;
                }
            } else if (this.facingTicks <= 0) {
                this.facingTicks = 0;
                this.tellyBlockCount = 2;
            } else {
                this.tellyBlockCount = f > 35.0f ? 2 : 1;
            }
        }
        this.canPlaceOnGround = bl;
    }

    private static float[] interpolateRotations(float f, float f2, float f3, float f4, float f5, float f6) {
        float f7 = MathHelper.wrapAngleTo180_float((float)(f3 - f));
        float f8 = f4 - f2;
        f += RotationUtil.applySmoothing((float)f7, (float)f5);
        f2 = MathHelper.clamp_float((float)(f2 + RotationUtil.applySmoothing((float)f8, (float)f6)), (float)-90.0f, (float)90.0f);
        if (Math.abs(MathHelper.wrapAngleTo180_float((float)(f3 - f))) <= RotationUtil.getMouseGCD()) {
            f = f3;
        }
        if (Math.abs(f4 - f2) <= RotationUtil.getMouseGCD()) {
            f2 = f4;
        }
        return new float[]{f, f2};
    }

    private float getRotationStep(boolean bl) {
        if (this.isHypixelTellyActive() && this.tellyActive) {
            return RandomUtil.nextFloat((float)72.0f, (float)80.0f);
        }
        if (bl || this.tellyStartTicks >= 2) {
            return RandomUtil.nextFloat((float)52.0f, (float)60.0f);
        }
        return RandomUtil.nextFloat((float)40.0f, (float)50.0f);
    }

    private static boolean isBedNukerInactive() {
        BedNuker bedNuker = (BedNuker)Valor.moduleManager.modules.get(BedNuker.class);
        return bedNuker == null || !bedNuker.isEnabled() || !bedNuker.isReady();
    }

    private static EnumFacing getFacingBetween(BlockPos blockPos, BlockPos blockPos2) {
        double d2 = 0.0;
        EnumFacing enumFacing = null;
        EnumFacing[] enumFacingArray = EnumFacing.VALUES;
        int n = EnumFacing.VALUES.length;
        for (int i = 0; i < n; ++i) {
            BlockPos blockPos3;
            EnumFacing enumFacing2 = enumFacingArray[i];
            if (enumFacing2 == EnumFacing.DOWN || (blockPos3 = blockPos.offset(enumFacing2)).getY() > blockPos2.getY()) continue;
            double d3 = blockPos3.distanceSqToCenter((double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.5, (double)blockPos2.getZ() + 0.5);
            if (enumFacing != null && !(d3 < d2) && (d3 != d2 || enumFacing2 != EnumFacing.UP)) continue;
            d2 = d3;
            enumFacing = enumFacing2;
        }
        return enumFacing;
    }

    private float getMoveOrStrafeYaw() {
        if ((this.hypixelTellyOnGround || this.hypixelTellyInAir) && !Float.isNaN(this.hypixelTellyYaw)) {
            if (Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ) > 0.12) {
                float f;
                float f2 = MoveUtil.getMoveYaw();
                if (Math.abs(MathHelper.wrapAngleTo180_float((float)(f2 - this.hypixelTellyYaw))) <= 40.0f) {
                    return f2;
                }
            }
            return this.hypixelTellyYaw;
        }
        if (Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ) > 0.08) {
            return MoveUtil.getMoveYaw();
        }
        return this.getBasePlacementYaw();
    }

    private ArrayList<BlockPos> getCandidateBlockPositions(int n) {
        return this.getCandidateBlockPositions(n, 5.0);
    }

    private ArrayList<BlockPos> getCandidateBlockPositions(int n, double d2) {
        float f = this.getMoveOrStrafeYaw() * ((float)Math.PI / 180);
        double d3 = -MathHelper.sin((float)f);
        double d4 = MathHelper.cos((float)f);
        int n2 = MathHelper.floor_double((double)mc.thePlayer.posX);
        int n3 = MathHelper.floor_double((double)mc.thePlayer.posZ);
        int n4 = d3 >= 0.0 ? 1 : -1;
        int n5 = d4 >= 0.0 ? 1 : -1;
        double d5 = Math.max(1.0E-6, Math.abs(d3));
        double d6 = Math.max(1.0E-6, Math.abs(d4));
        double d7 = n4 > 0 ? (double)n2 + 1.0 - mc.thePlayer.posX : mc.thePlayer.posX - (double)n2;
        double d8 = n5 > 0 ? (double)n3 + 1.0 - mc.thePlayer.posZ : mc.thePlayer.posZ - (double)n3;
        double d9 = d7 / d5;
        double d10 = d8 / d6;
        double d11 = 1.0 / d5;
        double d12 = 1.0 / d6;
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        arrayList.add(new BlockPos(n2, n, n3));
        for (int i = 0; i < 12 && Math.min(d9, d10) <= d2; ++i) {
            if (Math.abs(d9 - d10) <= 0.025) {
                BlockPos blockPos = new BlockPos(n2 + n4, n, n3);
                BlockPos blockPos2 = new BlockPos(n2, n, n3 + n5);
                if (!BlockUtil.isReplaceable((BlockPos)blockPos) || !BlockUtil.isReplaceable((BlockPos)blockPos2)) {
                    d9 += d11;
                    d10 += d12;
                    arrayList.add(new BlockPos(n2 += n4, n, n3 += n5));
                    continue;
                }
                arrayList.add(blockPos);
                arrayList.add(blockPos2);
                break;
            }
            if (d9 < d10) {
                d9 += d11;
                arrayList.add(new BlockPos(n2 += n4, n, n3));
                continue;
            }
            d10 += d12;
            arrayList.add(new BlockPos(n2, n, n3 += n5));
        }
        return arrayList;
    }

    private boolean isValidPlacementPos(BlockPos blockPos) {
        int n;
        if (blockPos == null) return false;
        if (blockPos.getY() != this.getScaffoldY()) return false;
        if (!BlockUtil.isReplaceable((BlockPos)blockPos)) {
            return false;
        }
        BlockPos blockPos2 = blockPos;
        Scaffold ooOoOo = this;
        if (blockPos2 != null) {
            int n2 = this.getScaffoldY();
            if (blockPos2.getY() == n2) {
                for (BlockPos blockPos3 : this.getCandidateBlockPositions(n2)) {
                    if (!blockPos2.equals((Object)blockPos3)) continue;
                    return true;
                }
            }
        }
        boolean bl = false;
        if (bl) {
            return true;
        }
        int n3 = MathHelper.floor_double((double)mc.thePlayer.posX);
        int n4 = MathHelper.floor_double((double)mc.thePlayer.posZ);
        n3 = Math.abs(blockPos.getX() - n3);
        if (n3 + (n = Math.abs(blockPos.getZ() - n4)) > 2) return false;
        return true;
    }

    private PlaceData calculatePlaceDataForPos(BlockPos blockPos) {
        PlaceData closestData = null;
        PlaceData bestData = null;
        double d2 = Double.MAX_VALUE;
        double d3 = Double.MAX_VALUE;
        for (EnumFacing facing : EnumFacing.VALUES) {
            double d4;
            if (facing == EnumFacing.DOWN) continue;
            BlockPos neighborPos = blockPos.offset(facing);
            Block block = mc.theWorld.getBlockState(neighborPos).getBlock();
            if (BlockUtil.isReplaceable(neighborPos) || BlockUtil.isInteractableBlock(neighborPos) || block.getMaterial().isLiquid() || !block.getMaterial().isSolid() || (d4 = mc.thePlayer.getDistanceSq((double)neighborPos.getX() + 0.5, (double)neighborPos.getY() + 0.5, (double)neighborPos.getZ() + 0.5)) > d2) continue;
            PlaceData candidate = new PlaceData(neighborPos, facing.getOpposite());
            float dist = this.getPlacementScore(candidate);
            if (dist != Float.MAX_VALUE) {
                bestData = candidate;
                d2 = d4;
                continue;
            }
            if (d4 >= d3) continue;
            closestData = candidate;
            d3 = d4;
        }
        if (bestData != null) {
            return bestData;
        }
        return closestData;
    }

    private float getPlacementScore(PlaceData o0) {
        double[] dArray = new double[]{0.125, 0.375, 0.625, 0.875};
        double[] dArray2 = dArray;
        double[] dArray3 = dArray;
        switch (SyntheticSwitchMap.I[o0.facing.ordinal()]) {
            case 1: {
                dArray = new double[]{0.0};
                break;
            }
            case 2: {
                dArray2 = new double[]{1.0};
                break;
            }
            case 3: {
                dArray = new double[]{1.0};
                break;
            }
            case 4: {
                dArray2 = new double[]{0.0};
                break;
            }
            case 5: {
                dArray3 = new double[]{1.0};
                break;
            }
            default: {
                return Float.MAX_VALUE;
            }
        }
        float f = Float.isNaN(this.targetLookYaw) ? mc.thePlayer.rotationYaw : this.targetLookYaw;
        float f2 = Float.isNaN(this.targetLookPitch) ? mc.thePlayer.rotationPitch : this.targetLookPitch;
        float f3 = Float.MAX_VALUE;
        for (double d2 : dArray2) {
            double[] dArray4 = dArray3;
            int n = dArray3.length;
            for (int i = 0; i < n; ++i) {
                double d3 = dArray4[i];
                double[] dArray5 = dArray;
                int n2 = dArray.length;
                for (int j = 0; j < n2; ++j) {
                    float f4;
                    double d4;
                    double d5;
                    double d6 = dArray5[j];
                    double d7 = (double)o0.blockPos.getX() + d2 - mc.thePlayer.posX;
                    float[] fArray = RotationUtil.getRotations((double)d7, (double)(d5 = (double)o0.blockPos.getY() + d3 - mc.thePlayer.posY - (double)mc.thePlayer.getEyeHeight()), (double)(d4 = (double)o0.blockPos.getZ() + d6 - mc.thePlayer.posZ), (float)f, (float)f2);
                    if (fArray[1] > 80.0f || Scaffold.rayTraceFacing(o0, fArray[0], fArray[1]) == null) continue;
                    float f5 = Math.abs(MathHelper.wrapAngleTo180_float((float)(fArray[0] - f)));
                    float f6 = Math.abs(fArray[1] - 72.0f);
                    float f7 = Math.abs(MathHelper.wrapAngleTo180_float((float)(fArray[0] - this.getBasePlacementYaw())));
                    f7 = f7 > 142.0f ? (f7 - 142.0f) * 4.0f : 0.0f;
                    f3 = Math.min(f3, f5 + f6 * 1.5f + f7);
                }
            }
        }
        return f3;
    }

    private PlaceData calculatePlaceData() {
        BlockPos blockPos;
        int n;
        Block block;
        if (this.isHypixelTellyActive() && this.tellyWaiting) {
            return null;
        }
        if (this.isHypixelTellyActive() && this.tellyActive && !this.tellyWaiting) {
            BlockPos blockPos2 = this.findTellyBlockPos();
            if (blockPos2 != null) {
                this.tellyStartPos = blockPos2;
            } else {
                BlockPos blockPos3 = new BlockPos(MathHelper.floor_double(mc.thePlayer.posX), this.targetY - 1, MathHelper.floor_double(mc.thePlayer.posZ));
                Block block2 = mc.theWorld.getBlockState(blockPos3).getBlock();
                block = mc.theWorld.getBlockState(blockPos3.up()).getBlock();
                if (block2.getMaterial().isSolid() && !block.getMaterial().isLiquid() && block.getMaterial().isSolid()) {
                    this.tellyStartPos = blockPos3;
                    this.tellyWaiting = true;
                    return null;
                }
                this.tellyStartPos = null;
                this.tellyActive = false;
                this.resetRotationLock();
            }
            if (this.tellyActive && this.tellyStartPos != null) {
                if (BlockUtil.isReplaceable(this.tellyStartPos.up())) {
                    return new PlaceData(this.tellyStartPos, EnumFacing.UP);
                }
                this.tellyWaiting = true;
                return null;
            }
        }
        double d2 = mc.thePlayer.posX;
        double d3 = mc.thePlayer.posZ;
        int n2 = this.getScaffoldY();
        if (this.isHypixelFastTelly()) {
            int n3 = 0;
            if (this.isTellyStrafeActive()) {
                int n4 = n2;
                if (this.lastPlacedPos != null && this.lastPlacedFacing != null && this.lastPlacedFacing != EnumFacing.DOWN) {
                    BlockPos blockPos4 = this.lastPlacedPos.offset(this.lastPlacedFacing);
                    if (blockPos4.getY() == n4 && BlockUtil.isReplaceable(blockPos4)) {
                        Block block3 = mc.theWorld.getBlockState(this.lastPlacedPos).getBlock();
                        if (!BlockUtil.isReplaceable(this.lastPlacedPos) && !BlockUtil.isInteractableBlock(this.lastPlacedPos) && !block3.getMaterial().isLiquid() && block3.getMaterial().isSolid()) {
                            double d5 = (double)mc.playerController.getBlockReachDistance();
                            if (mc.thePlayer.getDistanceSq((double)this.lastPlacedPos.getX() + 0.5, (double)this.lastPlacedPos.getY() + 0.5, (double)this.lastPlacedPos.getZ() + 0.5) <= d5 * d5) {
                                if (!mc.thePlayer.onGround && mc.thePlayer.motionY > 0.0 && this.strafeCooldownTicks == 0) {
                                    return new PlaceData(this.lastPlacedPos, this.lastPlacedFacing);
                                }
                                int n6 = MathHelper.floor_double(mc.thePlayer.posX);
                                n3 = MathHelper.floor_double(mc.thePlayer.posZ);
                                if (Math.abs(blockPos4.getX() - n6) <= 2 && Math.abs(blockPos4.getZ() - n3) <= 2) {
                                    float f = this.getMoveOrStrafeYaw() * ((float)Math.PI / 180);
                                    if (!(((double)blockPos4.getX() + 0.5 - mc.thePlayer.posX) * (double)(-MathHelper.sin(f)) + ((double)blockPos4.getZ() + 0.5 - mc.thePlayer.posZ) * (double)MathHelper.cos(f) < -0.65 && (blockPos4.getX() != n6 || blockPos4.getZ() != n3))) {
                                        return new PlaceData(this.lastPlacedPos, this.lastPlacedFacing);
                                    }
                                }
                            }
                        }
                    }
                }
                PlaceData o02 = null;
                for (BlockPos blockPos5 : this.getCandidateBlockPositions(n4)) {
                    PlaceData o03;
                    if (!BlockUtil.isReplaceable(blockPos5) || (o03 = this.calculatePlaceDataForPos(blockPos5)) == null) continue;
                    if (this.getPlacementScore(o03) == Float.MAX_VALUE) {
                        if (o02 != null) continue;
                        o02 = o03;
                        continue;
                    }
                    this.lastPlacedPos = o03.blockPos;
                    this.lastPlacedFacing = o03.facing;
                    return o03;
                }
                if (o02 != null) {
                    return o02;
                }
                int n7 = MathHelper.floor_double(mc.thePlayer.posX);
                int n8 = MathHelper.floor_double(mc.thePlayer.posZ);
                double d6 = mc.thePlayer.posX + mc.thePlayer.motionX;
                double d7 = mc.thePlayer.posZ + mc.thePlayer.motionZ;
                double d8 = Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
                double d9 = Math.max(0.45, Math.min(0.72, d8 > 0.08 ? d8 : 0.55));
                double d10 = Math.max(0.5, d9 * (double)Math.max(1, this.keepYTicks));
                double d11 = d8 > 0.0 ? d10 : 0.0;
                PlaceData o04 = null;
                for (BlockPos blockPos6 : this.getCandidateBlockPositions(n4, d11)) {
                    PlaceData o05;
                    if (!BlockUtil.isReplaceable(blockPos6) || (o05 = this.calculatePlaceDataForPos(blockPos6)) == null) continue;
                    if (this.getPlacementScore(o05) == Float.MAX_VALUE) {
                        if (o04 != null) continue;
                        o04 = o05;
                        continue;
                    }
                    this.lastPlacedPos = o05.blockPos;
                    this.lastPlacedFacing = o05.facing;
                    return o05;
                }
                if (o04 != null) {
                    this.lastPlacedPos = o04.blockPos;
                    this.lastPlacedFacing = o04.facing;
                }
                return o04;
            }
            int n9 = n2;
            if (this.lastPlacedPos != null && this.lastPlacedFacing != null && this.lastPlacedFacing != EnumFacing.DOWN) {
                BlockPos blockPos7 = this.lastPlacedPos.offset(this.lastPlacedFacing);
                if (blockPos7.getY() == n9 && BlockUtil.isReplaceable(blockPos7)) {
                    Block block4 = mc.theWorld.getBlockState(this.lastPlacedPos).getBlock();
                    if (!BlockUtil.isReplaceable(this.lastPlacedPos) && !BlockUtil.isInteractableBlock(this.lastPlacedPos) && !block4.getMaterial().isLiquid() && block4.getMaterial().isSolid()) {
                        double d14 = (double)mc.playerController.getBlockReachDistance();
                        if (mc.thePlayer.getDistanceSq((double)this.lastPlacedPos.getX() + 0.5, (double)this.lastPlacedPos.getY() + 0.5, (double)this.lastPlacedPos.getZ() + 0.5) <= d14 * d14) {
                            float f = this.getBasePlacementYaw() * ((float)Math.PI / 180);
                            double d15 = -MathHelper.sin(f);
                            double d16 = MathHelper.cos(f);
                            double d17 = (double)blockPos7.getX() + 0.5 - mc.thePlayer.posX;
                            double d18 = (double)blockPos7.getZ() + 0.5 - mc.thePlayer.posZ;
                            double d19 = d17 * d15 + d18 * d16;
                            double d20 = Math.abs(d17 * d16 - d18 * d15);
                            if (!(d19 < -1.25 || d19 > 4.25 || d20 > 1.1)) {
                                return new PlaceData(this.lastPlacedPos, this.lastPlacedFacing);
                            }
                        }
                    }
                }
            }
            float f = this.getBasePlacementYaw() * ((float)Math.PI / 180);
            double d21 = Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
            double d22 = Math.max(0.45, Math.min(0.72, d21 > 0.08 ? d21 : 0.55));
            double d23 = (double)(-MathHelper.sin(f)) * d22;
            double d24 = (double)MathHelper.cos(f) * d22;
            d2 = mc.thePlayer.posX;
            d3 = mc.thePlayer.posZ;
            for (int i = 0; i < 8; ++i) {
                if (BlockUtil.isReplaceable(new BlockPos(MathHelper.floor_double(d2), n2, MathHelper.floor_double(d3)))) {
                    n3 = 1;
                    break;
                }
                d2 += d23;
                d3 += d24;
            }
            double d25 = (double)(-MathHelper.sin(f)) * 0.55;
            double d26 = (double)MathHelper.cos(f) * 0.55;
            d2 = mc.thePlayer.posX;
            d3 = mc.thePlayer.posZ;
            n = 0;
            for (int i = 0; i < 8; ++i) {
                if (BlockUtil.isReplaceable(new BlockPos(MathHelper.floor_double(d2), n2, MathHelper.floor_double(d3)))) {
                    n = 1;
                    break;
                }
                d2 += d25;
                d3 += d26;
            }
            if (n == 0) {
                return null;
            }
        } else if (this.isTellyActive() && !this.isClutchOrHypixelFastTelly() && Scaffold.isFalling() && !this.isBlockUnderReplaceable()) {
            d2 += mc.thePlayer.motionX;
            d3 += mc.thePlayer.motionZ;
        }
        blockPos = new BlockPos(MathHelper.floor_double(d2), n2, MathHelper.floor_double(d3));
        if (!BlockUtil.isReplaceable(blockPos)) {
            if (this.isTellyActive()) {
                blockPos = new BlockPos(MathHelper.floor_double(mc.thePlayer.posX), n2, MathHelper.floor_double(mc.thePlayer.posZ));
                if (!BlockUtil.isReplaceable(blockPos)) {
                    return null;
                }
            } else {
                return null;
            }
        }
        ArrayList<BlockPos> candidatePositions = new ArrayList<>();
        for (int i = -4; i <= 4; ++i) {
            for (int j = -4; j <= 0; ++j) {
                for (n = -4; n <= 4; n += 1) {
                    BlockPos blockPos8 = blockPos.add(i, j, n);
                    if (BlockUtil.isReplaceable(blockPos8) || BlockUtil.isInteractableBlock(blockPos8) || mc.thePlayer.getDistance((double)blockPos8.getX() + 0.5, (double)blockPos8.getY() + 0.5, (double)blockPos8.getZ() + 0.5) > (double)mc.playerController.getBlockReachDistance() || this.keepYTicks != 0 && !this.shouldUpdateY && blockPos8.getY() >= this.targetY) continue;
                    EnumFacing[] enumFacingArray = EnumFacing.VALUES;
                    int n10 = EnumFacing.VALUES.length;
                    for (int k = 0; k < n10; ++k) {
                        EnumFacing enumFacing = enumFacingArray[k];
                        if (enumFacing == EnumFacing.DOWN || !BlockUtil.isReplaceable(blockPos8.offset(enumFacing))) continue;
                        candidatePositions.add(blockPos8);
                    }
                }
            }
        }
        if (candidatePositions.isEmpty()) {
            return null;
        }
        BlockPos blockPos9 = blockPos;
        boolean bl = !this.isTellyActive();
        n = this.isHypixelFastTelly() && !this.isTellyStrafeActive() ? 1 : 0;
        boolean bl2 = this.isHypixelFastTelly() && this.isTellyStrafeActive();
        double d29 = mc.thePlayer.motionX;
        double d30 = mc.thePlayer.motionZ;
        double d31 = d29;
        double d32 = d30;
        double d33 = d31 * d31 + d32 * d32;
        double d34 = mc.thePlayer.posX;
        double d35 = mc.thePlayer.posZ;
        float f = (bl2 ? this.getMoveOrStrafeYaw() : this.getBasePlacementYaw()) * ((float)Math.PI / 180);
        double d36 = -MathHelper.sin(f);
        double d37 = MathHelper.cos(f);
        final boolean finalN = n != 0;
        candidatePositions.sort(Comparator.comparingDouble(arg_0 -> this.scoreCandidatePosition(blockPos9, bl, d33, d34, d29, d35, d30, finalN, bl2, d36, d37, (BlockPos)arg_0)));
        BlockPos bestNeighbor = candidatePositions.get(0);
        EnumFacing enumFacing = Scaffold.getFacingBetween(bestNeighbor, blockPos9);
        if (enumFacing == null) {
            return null;
        }
        PlaceData placeData = new PlaceData(bestNeighbor, enumFacing);
        if (n != 0) {
            this.lastPlacedPos = placeData.blockPos;
            this.lastPlacedFacing = placeData.facing;
        }
        return placeData;
    }

    private MovingObjectPosition rayTracePlaceData(PlaceData o0, float f, float f2) {
        MovingObjectPosition mop = Scaffold.rayTraceFacing(o0, f, f2);
        if (mop != null || o0 == null || !this.isTellyStrafeActive()) {
            return mop;
        }
        BlockPos offsetPos = o0.blockPos.offset(o0.facing);
        if (!BlockUtil.isReplaceable(offsetPos) || offsetPos.getY() != this.getScaffoldY()) {
            return null;
        }
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing == EnumFacing.DOWN) continue;
            BlockPos neighbor = offsetPos.offset(facing);
            if (neighbor.equals(o0.blockPos) && facing.getOpposite() == o0.facing) continue;
            Block block = mc.theWorld.getBlockState(neighbor).getBlock();
            if (block.getMaterial().isLiquid() || !block.getMaterial().isSolid()) continue;
            MovingObjectPosition targetMop = Scaffold.rayTraceFacing(new PlaceData(neighbor, facing.getOpposite()), f, f2);
            if (targetMop == null) continue;
            return targetMop;
        }
        return null;
    }

    private void placeBlock(BlockPos blockPos, EnumFacing enumFacing, Vec3 vec3) {
        if (!(!this.is3FMCRotations() ? true : (mc.thePlayer == null || this.placedThisTick || this.delayTicks3FMC > 0 ? false : (!this.is3FMCTelly() && mc.thePlayer.isSprinting() || mc.thePlayer.isCollidedHorizontally || mc.thePlayer.hurtTime > 0 ? false : (mc.thePlayer.onGround ? Math.abs(mc.thePlayer.motionY) < 1.0E-4 && this.groundTicks3FMC > 0 : (this.is3FMCTelly() ? this.airTicks3FMC > 1 : this.airTicks3FMC > 2)))))) {
            return;
        }
        if (ItemUtil.isHoldingValidBlock() && this.blocksPlaced > 0) {
            Block block = mc.theWorld.getBlockState(blockPos).getBlock();
            if (this.isTellyActive() && (block.getMaterial().isLiquid() || !block.getMaterial().isSolid())) {
                return;
            }
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), blockPos, enumFacing, vec3)) {
                if (mc.playerController.getCurrentGameType() != WorldSettings.GameType.CREATIVE) {
                    --this.blocksPlaced;
                }
                boolean bl = !this.placedThisTick;
                this.placedThisTick = true;
                if (this.isHypixelTellyActive()) {
                    blockPos = blockPos.offset(enumFacing);
                    if (this.tellyActive && this.tellyStartPos != null && blockPos.equals((Object)this.tellyStartPos.up())) {
                        this.tellyWaiting = true;
                    } else if (!this.tellyActive && blockPos.getY() == this.targetY - 1) {
                        this.tellyTargetPos = blockPos;
                        BlockPos playerFloorPos = new BlockPos(MathHelper.floor_double(mc.thePlayer.posX), this.targetY - 1, MathHelper.floor_double(mc.thePlayer.posZ));
                        if (mc.thePlayer.motionY < 0.0 && blockPos.equals((Object)playerFloorPos) && BlockUtil.isReplaceable(blockPos.up())) {
                            this.tellyStartPos = blockPos;
                            this.tellyActive = true;
                            this.lagbackDetected = false;
                            this.resetRotationLock();
                        }
                    }
                }
                if (this.is3FMCRotations()) {
                    this.delayTicks3FMC = 1;
                }
                if (this.isTellyActive()) {
                    this.placeCooldownTicks = 0;
                } else if (bl) {
                    float f2 = ((Float)this.minCpsProp.getValue()).floatValue();
                    float f3 = ((Float)this.maxCpsProp.getValue()).floatValue();
                    if (f3 < f2) {
                        f3 = f2;
                    }
                    float f4 = MathHelper.clamp_float(RandomUtil.nextFloat(f2, f3) * RandomUtil.nextFloat(0.95f, 1.05f), f2, f3);
                    this.lastPlacementTime += (long)(1000.0f / Math.max(f4, 0.1f));
                }
                ++this.eagleBlocksPlaced;
                if ((Boolean)this.swingProp.getValue()) {
                    mc.thePlayer.swingItem();
                    return;
                }
                PacketUtil.sendPacketNoEvent(new C0APacketAnimation());
            }
        }
    }

    private static MovingObjectPosition rayTraceFacing(PlaceData o0, float f, float f2) {
        MovingObjectPosition movingObjectPosition = RotationUtil.rayTrace((float)f, (float)f2, (double)mc.playerController.getBlockReachDistance());
        if (movingObjectPosition == null || movingObjectPosition.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !movingObjectPosition.getBlockPos().equals((Object)o0.blockPos) || movingObjectPosition.sideHit != o0.facing) {
            return null;
        }
        return movingObjectPosition;
    }

    private MovingObjectPosition rayTracePlacement(float f, float f2) {
        MovingObjectPosition movingObjectPosition = RotationUtil.rayTrace(f, f2, (double)mc.playerController.getBlockReachDistance());
        if (movingObjectPosition == null || movingObjectPosition.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || movingObjectPosition.sideHit == EnumFacing.DOWN) {
            return null;
        }
        Block block = mc.theWorld.getBlockState(movingObjectPosition.getBlockPos()).getBlock();
        if (block.getMaterial().isLiquid() || !block.getMaterial().isSolid()) {
            return null;
        }
        BlockPos offsetPos = movingObjectPosition.getBlockPos().offset(movingObjectPosition.sideHit);
        int n = this.getScaffoldY();
        if (!BlockUtil.isReplaceable(offsetPos) || offsetPos.getY() != n) {
            return null;
        }
        double d2 = mc.thePlayer.posX;
        double d3 = mc.thePlayer.posZ;
        double d4 = (double)offsetPos.getX() + 0.5 - d2;
        double d5 = (double)offsetPos.getZ() + 0.5 - d3;
        double d6 = d4;
        double d7 = d5;
        if (d6 * d6 + d7 * d7 > 12.25) {
            return null;
        }
        return movingObjectPosition;
    }

    private boolean isYawClose(float f) {
        return !Float.isNaN(this.lastPlacedYaw) && Math.abs(MathHelper.wrapAngleTo180_float((float)(f - this.lastPlacedYaw))) < 0.35f;
    }

    private float[] getGCDPlacementRotations(PlaceData o0, float f, float f2) {
        f = RotationUtil.roundRotation((float)f);
        f2 = RotationUtil.roundRotation((float)MathHelper.clamp_float((float)f2, (float)-90.0f, (float)90.0f));
        if (!this.isYawClose(f)) {
            return new float[]{f, f2};
        }
        for (int i = 0; i < 24; ++i) {
            float f3 = 0.35f + 0.075f * (float)(i / 2);
            float f4 = 0.025f + 0.01f * (float)(i / 3);
            f3 = RotationUtil.roundRotation((float)(f + (i % 2 == 0 ? f3 : -f3)));
            f4 = RotationUtil.roundRotation((float)MathHelper.clamp_float((float)(f2 + (i % 4 < 2 ? f4 : -f4)), (float)-90.0f, (float)90.0f));
            if (this.isYawClose(f3) || Scaffold.rayTraceFacing(o0, f3, f4) == null) continue;
            return new float[]{f3, f4};
        }
        return null;
    }

    private void saveLastRotations() {
        this.lastPlacedYaw = this.targetYaw;
        this.lastPlacedPitch = this.targetPitch;
    }

    private void handleEagleSneak() {
        if (!((Boolean)this.eagleProp.getValue()).booleanValue()) {
            this.eagleSneaking = false;
            this.eagleSneakTicks = 0;
            return;
        }
        if (this.eagleSneakTicks > 0) {
            --this.eagleSneakTicks;
            if (this.eagleSneakTicks != 0) return;
            this.eagleSneaking = false;
            return;
        }
        Scaffold ooOoOo = this;
        if ((Boolean)ooOoOo.eagleProp.getValue() == false) return;
        if (!mc.thePlayer.onGround) {
            return;
        }
        if (this.eagleBlocksPlaced < (Integer)ooOoOo.blocksPerSneakProp.getValue()) return;
        if (System.currentTimeMillis() - this.lastEagleSneakTime < (long)((Integer)ooOoOo.sneakDelayProp.getValue()).intValue()) return;
        if (!mc.thePlayer.onGround) return;
        double d2 = mc.thePlayer.posX - Math.floor(mc.thePlayer.posX);
        double d3 = mc.thePlayer.posZ - Math.floor(mc.thePlayer.posZ);
        double d4 = ((Float)ooOoOo.edgeDistanceProp.getValue()).floatValue();
        if (!(Math.min(Math.min(d2, 1.0 - d2), Math.min(d3, 1.0 - d3)) <= d4)) return;
        boolean bl = true;
        if (!bl) return;
        this.eagleSneaking = true;
        this.eagleSneakTicks = 2;
        this.lastEagleSneakTime = System.currentTimeMillis();
        this.eagleBlocksPlaced = 0;
    }

    private static float calculateInputMoveYaw() {
        return MoveUtil.calculateMoveYaw((float)mc.thePlayer.rotationYaw, (float)MoveUtil.getForwardInput(), (float)MoveUtil.getStrafeInput());
    }

    private static boolean isAngleDiagonal(float f) {
        float f2;
        f = Math.abs(MathHelper.wrapAngleTo180_float((float)f)) % 90.0f;
        return f > 20.0f && f < 70.0f;
    }

    private boolean shouldKeepY() {
        if (this.isJumping()) {
            return false;
        }
        if (mc.thePlayer.onGround && MoveUtil.isMoving() && !PlayerUtil.isBlockAbove()) {
            return this.isTellyMode() && this.keepYTicks > 0;
        }
        return false;
    }

    public Scaffold() {
        super("Scaffold", false, ModuleCategory.WORLD);

    }

    @EventTarget(value=1)
    public void onUpdate(UpdateEvent updateEvent) {
        block235: {
            boolean bl;
            boolean bl2;
            boolean bl3;
            float f;
            int n;
            Object object;
            boolean bl4;
            int n2;
            float f2;
            float f3;
            float f4;
            float f5;
            boolean bl5;
            boolean bl6;
            boolean bl7;
            int n3;
            float f6;
            float f7;
            block237: {
                block236: {
                    if (!this.isEnabled() || updateEvent.getType() != EventType.PRE) break block235;
                    this.updateJumpState();
                    this.placedThisTick = false;
                    long l = System.currentTimeMillis();
                    long l2 = this.lastUpdateTime == 0L ? 0L : Math.max(0L, Math.min(50L, l - this.lastUpdateTime));
                    this.lastUpdateTime = l;
                    boolean bl8 = this.isHypixelTellyActive();
                    if (bl8) {
                        if (!this.tellyRotating || Float.isNaN(this.tellyMotionYaw)) {
                            this.tellyMotionYaw = Scaffold.calculateInputMoveYaw();
                            if (mc.thePlayer.onGround) {
                                this.tellyJumped = true;
                                this.tellyTargetPos = null;
                                this.tellyStartPos = null;
                                this.tellyActive = false;
                                this.tellyWaiting = false;
                                this.tellyStage = 0;
                            }
                        }
                    } else if (mc.gameSettings == null || !mc.gameSettings.keyBindJump.isKeyDown()) {
                        this.tellyMotionYaw = Float.NaN;
                        this.tellyJumped = false;
                    }
                    this.tellyRotating = bl8;
                    if (this.tellyDelayTicks > 0 && mc.thePlayer.onGround) {
                        --this.tellyDelayTicks;
                    }
                    this.updateHypixelTellyState();
                    this.update3FMCState();
                    this.stopSprint3FMC();
                    if (this.placeDelayTicks > 0) {
                        --this.placeDelayTicks;
                        if (this.placeDelayTicks <= 0) {
                            this.freezeMovementTicks = 1;
                        }
                    }
                    if (this.freezeMovementTicks > 0) {
                        if (!this.motionSaved) {
                            this.prevMotionX = mc.thePlayer.motionX;
                            this.prevMotionY = mc.thePlayer.motionY;
                            this.prevMotionZ = mc.thePlayer.motionZ;
                            this.motionSaved = true;
                        }
                        Valor.blinkManager.setBlinking(true, BlinkType.SCAFFOLD);
                        mc.thePlayer.motionX = 0.0;
                        mc.thePlayer.motionY = 0.0;
                        mc.thePlayer.motionZ = 0.0;
                    } else if (this.motionSaved) {
                        Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
                        mc.thePlayer.motionX = this.prevMotionX;
                        mc.thePlayer.motionY = this.prevMotionY;
                        mc.thePlayer.motionZ = this.prevMotionZ;
                        this.motionSaved = false;
                    }
                    if (this.tellyStartTicks > 0) {
                        --this.tellyStartTicks;
                    }
                    if (this.placeCooldownTicks > 0) {
                        --this.placeCooldownTicks;
                    }
                    if (this.lastPlacementTime > 0L) {
                        this.lastPlacementTime -= l2;
                    }
                    this.handleEagleSneak();
                    if (this.isJumping()) {
                        this.keepYTicks = 0;
                        this.shouldUpdateY = false;
                        this.tellyPlacing = false;
                        this.strafeStage = 0;
                        this.groundDelayTicks = 0;
                        this.facingTicks = 0;
                        if (!this.hypixelTellyJumping) {
                            this.resetRotationLock();
                            this.tellyFalling = false;
                            this.tellyStartTicks = 0;
                            this.placeCooldownTicks = 0;
                            this.lastPlacementTime = 0L;
                        }
                        this.hypixelTellyJumping = true;
                    } else {
                        this.hypixelTellyJumping = false;
                        if (!this.isHypixelFastTelly()) {
                            this.facingTicks = 0;
                        }
                    }
                    if (((Boolean)this.hypixelTowerProp.getValue()).booleanValue() && !this.isTellyMode() && mc.thePlayer.motionY <= 0.0 && Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ) <= 0.02 && mc.thePlayer.motionY >= -0.09 && !Keyboard.isKeyDown((int)mc.gameSettings.keyBindForward.getKeyCode()) && !Keyboard.isKeyDown((int)mc.gameSettings.keyBindBack.getKeyCode()) && !Keyboard.isKeyDown((int)mc.gameSettings.keyBindLeft.getKeyCode()) && !Keyboard.isKeyDown((int)mc.gameSettings.keyBindRight.getKeyCode()) && Keyboard.isKeyDown((int)mc.gameSettings.keyBindJump.getKeyCode())) {
                        mc.thePlayer.motionY = -0.38;
                    }
                    if (mc.thePlayer.onGround) {
                        if (this.keepYTicks > 0) {
                            --this.keepYTicks;
                        }
                        if (this.keepYTicks < 0) {
                            ++this.keepYTicks;
                        }
                        if (!(this.keepYTicks != 0 || (Integer)this.keepYProp.getValue() == 0 || ((Boolean)this.keepYOnPressProp.getValue()).booleanValue() && !PlayerUtil.isUseItemPressed() || ((Boolean)this.noKeepYOnJumpPotionProp.getValue()).booleanValue() && mc.thePlayer.isPotionActive(Potion.jump) || mc.gameSettings.keyBindJump.isKeyDown() && !this.isHypixelTelly())) {
                            this.keepYTicks = 1;
                        }
                        this.targetY = this.shouldUpdateY ? this.targetY : MathHelper.floor_double((double)mc.thePlayer.posY);
                        this.shouldUpdateY = false;
                        this.tellyPlacing = false;
                        if (this.isTellyActive() && !this.isClutchOrHypixelFastTelly()) {
                            this.resetRotationLock();
                            this.tellyFalling = false;
                        }
                    }
                    if (!Scaffold.isBedNukerInactive()) break block235;
                    ItemStack itemStack = mc.thePlayer.getHeldItem();
                    int n4 = ItemUtil.isValidBlockItem((ItemStack)itemStack) ? itemStack.stackSize : 0;
                    this.blocksPlaced = Math.min(this.blocksPlaced, n4);
                    if (this.blocksPlaced <= 0) {
                        n4 = mc.thePlayer.inventory.currentItem;
                        if (this.blocksPlaced == 0) {
                            --n4;
                        }
                        for (int i = n4; i > n4 - 9; --i) {
                            int n5 = (i % 9 + 9) % 9;
                            ItemStack itemStack2 = mc.thePlayer.inventory.getStackInSlot(n5);
                            if (!ItemUtil.isValidBlockItem((ItemStack)itemStack2)) continue;
                            mc.thePlayer.inventory.currentItem = n5;
                            this.blocksPlaced = itemStack2.stackSize;
                            break;
                        }
                    }
                    float f8 = Scaffold.calculateInputMoveYaw();
                    f7 = RotationUtil.clampAngle((float)(f8 - 180.0f), (float)updateEvent.getYaw());
                    f6 = Scaffold.isAngleDiagonal(f8) ? f7 : RotationUtil.clampAngle((float)(f8 - 135.0f * ((f8 + 180.0f) % 90.0f < 45.0f ? 1.0f : -1.0f)), (float)updateEvent.getYaw());
                    n3 = (Integer)this.rotationsProp.getValue();
                    bl7 = n3 == 7;
                    bl6 = this.is3FMCRotations();
                    bl5 = this.is3FMCTelly();
                    this.wasSneaking = false;
                    if (this.hasRotations || this.isJumping()) break block236;
                    switch (n3) {
                        case 1: {
                            if (this.targetYaw == -180.0f && this.targetPitch == 0.0f) {
                                this.targetYaw = RotationUtil.roundRotation((float)f6);
                                this.targetPitch = RotationUtil.roundRotation((float)78.0f);
                                break;
                            }
                            this.targetYaw = RotationUtil.roundRotation((float)f6);
                            break;
                        }
                        case 2: {
                            if (this.targetYaw == -180.0f && this.targetPitch == 0.0f) {
                                this.targetYaw = RotationUtil.roundRotation((float)f7);
                                this.targetPitch = RotationUtil.roundRotation((float)78.0f);
                                break;
                            }
                            this.targetYaw = RotationUtil.roundRotation((float)f7);
                            break;
                        }
                        case 3: {
                            if (this.targetYaw == -180.0f && this.targetPitch == 0.0f) {
                                this.targetYaw = RotationUtil.roundRotation((float)f6);
                                this.targetPitch = RotationUtil.roundRotation((float)78.0f);
                                break;
                            }
                            this.targetYaw = RotationUtil.roundRotation((float)f6);
                            break;
                        }
                        case 4: {
                            f8 = (float)Math.round(f8 / 45.0f) * 45.0f;
                            this.targetYaw = RotationUtil.roundRotation((float)f8);
                            if (this.targetPitch == 0.0f || !this.hasRotations) {
                                this.targetPitch = RotationUtil.roundRotation((float)79.3f);
                                break;
                            }
                            break block237;
                        }
                        case 5: {
                            if (this.targetYaw == -180.0f && this.targetPitch == 0.0f) {
                                this.targetYaw = RotationUtil.roundRotation((float)f6);
                                this.targetPitch = RotationUtil.roundRotation((float)78.0f);
                                break;
                            }
                            float f9 = MathHelper.wrapAngleTo180_float((float)((Scaffold.isAngleDiagonal(f8) ? f6 : f7) - this.targetYaw));
                            float f10 = MathHelper.wrapAngleTo180_float((float)(78.0f - this.targetPitch));
                            float f11 = this.tellyStartTicks >= 2 ? RandomUtil.nextFloat((float)((Float)this.tellyStartRotMinSpeedProp.getValue()).floatValue(), (float)((Float)this.tellyStartRotMaxSpeedProp.getValue()).floatValue()) : RandomUtil.nextFloat((float)((Float)this.tellyNormalRotMinSpeedProp.getValue()).floatValue(), (float)((Float)this.tellyNormalRotMaxSpeedProp.getValue()).floatValue());
                            f5 = this.tellyStartTicks >= 2 ? RandomUtil.nextFloat((float)((Float)this.tellyStartRotMinSpeedProp.getValue()).floatValue(), (float)((Float)this.tellyStartRotMaxSpeedProp.getValue()).floatValue()) : RandomUtil.nextFloat((float)((Float)this.tellyNormalRotMinSpeedProp.getValue()).floatValue(), (float)((Float)this.tellyNormalRotMaxSpeedProp.getValue()).floatValue());
                            this.targetYaw = RotationUtil.roundRotation((float)(this.targetYaw + RotationUtil.applySmoothing((float)f9, (float)f11)));
                            this.targetPitch = RotationUtil.roundRotation((float)(this.targetPitch + RotationUtil.applySmoothing((float)f10, (float)f5)));
                            break;
                        }
                        case 6: {
                            if (this.targetYaw == -180.0f && this.targetPitch == 0.0f) {
                                this.targetYaw = RotationUtil.roundRotation((float)f6);
                                this.targetPitch = RotationUtil.roundRotation((float)(this.isClutchOrHypixelFastTelly() ? 72.0f : 78.0f));
                                break;
                            }
                            this.targetYaw = RotationUtil.roundRotation((float)f6);
                            break;
                        }
                        case 7: {
                            this.targetYaw = RotationUtil.roundRotation((float)f7);
                            this.targetPitch = RotationUtil.roundRotation((float)(this.isClutchOrHypixelFastTelly() ? 72.0f : 78.0f));
                            break;
                        }
                        case 8: {
                            if (this.targetYaw == -180.0f && this.targetPitch == 0.0f) {
                                this.targetYaw = RotationUtil.roundRotation((float)updateEvent.getYaw());
                                this.targetPitch = RotationUtil.roundRotation((float)updateEvent.getPitch());
                            } else {
                                break;
                            }
                        }
                    }
                    break block237;
                }
                if (!this.hasRotations && this.isJumping() && this.targetYaw == -180.0f && this.targetPitch == 0.0f) {
                    this.targetYaw = RotationUtil.roundRotation((float)updateEvent.getYaw());
                    this.targetPitch = RotationUtil.roundRotation((float)78.0f);
                }
            }
            PlaceData o0 = this.calculatePlaceData();
            Vec3 object2 = null;
            if (o0 != null) {
                if (!this.isTellyActive() && !this.isClutchOrHypixelFastTelly() && this.hasRotations && this.lastPlacedPos != null && this.lastPlacedPos.equals((Object)o0.getBlockPos()) && this.lastPlacedFacing == o0.getFacing() && Scaffold.rayTraceFacing(o0, this.targetYaw, this.targetPitch) != null) {
                    MovingObjectPosition movingObjectPosition = Scaffold.rayTraceFacing(o0, this.targetYaw, this.targetPitch);
                    object2 = movingObjectPosition != null ? movingObjectPosition.hitVec : null;
                } else {
                    int n6;
                    double[] dArray = BLOCK_OFFSETS;
                    double[] dArray2 = BLOCK_OFFSETS;
                    double[] dArray3 = BLOCK_OFFSETS;
                    switch (SyntheticSwitchMap.I[o0.getFacing().ordinal()]) {
                        case 1: {
                            dArray3 = new double[]{0.0};
                            break;
                        }
                        case 2: {
                            dArray = new double[]{1.0};
                            break;
                        }
                        case 3: {
                            dArray3 = new double[]{1.0};
                            break;
                        }
                        case 4: {
                            dArray = new double[]{0.0};
                            break;
                        }
                        case 6: {
                            dArray2 = new double[]{0.0};
                            break;
                        }
                        case 5: {
                            dArray2 = new double[]{1.0};
                        }
                    }
                    f4 = -180.0f;
                    float f12 = 0.0f;
                    f3 = Float.MAX_VALUE;
                    f2 = 0.0f;
                    n2 = 0;
                    bl4 = this.isClutchOrHypixelFastTelly();
                    int n7 = dArray.length;
                    for (n6 = 0; n6 < n7; ++n6) {
                        double d2 = dArray[n6];
                        double[] dArray4 = dArray2;
                        int n8 = dArray2.length;
                        for (int i = 0; i < n8; ++i) {
                            double d3 = dArray4[i];
                            double[] dArray5 = dArray3;
                            int n9 = dArray3.length;
                            for (n = 0; n < n9; ++n) {
                                float f13;
                                float f14;
                                double d4;
                                double d5;
                                double d6 = dArray5[n];
                                double d7 = (double)o0.getBlockPos().getX() + d2 - mc.thePlayer.posX;
                                float[] fArray = RotationUtil.getRotations((double)d7, (double)(d5 = (double)o0.getBlockPos().getY() + d3 - mc.thePlayer.posY - (double)mc.thePlayer.getEyeHeight()), (double)(d4 = (double)o0.getBlockPos().getZ() + d6 - mc.thePlayer.posZ), (float)(f14 = RotationUtil.clampAngle((float)this.targetYaw, (float)updateEvent.getYaw())), (float)this.targetPitch);
                                MovingObjectPosition movingObjectPosition = RotationUtil.rayTrace((float)fArray[0], (float)fArray[1], (double)mc.playerController.getBlockReachDistance());
                                if (movingObjectPosition == null || movingObjectPosition.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !movingObjectPosition.getBlockPos().equals((Object)o0.getBlockPos()) || movingObjectPosition.sideHit != o0.getFacing()) continue;
                                if (bl4) {
                                    f13 = fArray[1];
                                    if (this.isHypixelFastTelly() && f13 > this.getMaxPitch()) continue;
                                    f14 = Math.abs(fArray[0] - f14) + Math.abs(f13 - this.targetPitch);
                                    if (this.isHypixelFastTelly()) {
                                        f14 += Math.abs(f13 - 72.0f) * 2.5f;
                                    }
                                    if ((f4 != -180.0f || f12 != 0.0f) && !(f14 < f2)) continue;
                                    f4 = fArray[0];
                                    f12 = f13;
                                    f2 = f14;
                                    object2 = movingObjectPosition.hitVec;
                                    continue;
                                }
                                f13 = fArray[1];
                                f14 = Math.abs(MathHelper.wrapAngleTo180_float((float)(fArray[0] - f14)));
                                boolean bl9 = f13 >= 72.0f && f13 <= 82.0f;
                                float f15 = bl9 ? Math.abs(f13 - 78.0f) : (f13 < 72.0f ? 40.0f + (72.0f - f13) : (f13 >= 84.0f ? 200.0f + (f13 - 84.0f) : 20.0f + (f13 - 82.0f)));
                                f14 = f15 * 10.0f + f14;
                                if (bl9 && n2 == 0) {
                                    n2 = 1;
                                    f3 = Float.MAX_VALUE;
                                }
                                if (n2 != 0 && !bl9 || !(f14 < f3)) continue;
                                f4 = fArray[0];
                                f12 = f13;
                                f3 = f14;
                                object2 = movingObjectPosition.hitVec;
                            }
                        }
                    }
                    if (!bl4 && (f4 != -180.0f || f12 != 0.0f) && f12 > 82.0f && Scaffold.rayTraceFacing(o0, f4, 82.0f) != null) {
                        f12 = 82.0f;
                    }
                    if (bl4 && this.isHypixelFastTelly() && (f4 != -180.0f || f12 != 0.0f) && f12 > this.getMaxPitch()) {
                        float f16 = this.getMaxPitch();
                        if (Scaffold.rayTraceFacing(o0, f4, f16) != null) {
                            f12 = f16;
                        } else if (Scaffold.rayTraceFacing(o0, f4, 72.0f) != null) {
                            f12 = 72.0f;
                        }
                    }
                    if (f4 != -180.0f || f12 != 0.0f) {
                        if (bl4) {
                            if (!(this.isHypixelFastTelly() && this.hasRotations && this.targetPitch <= this.getMaxPitch() && Scaffold.rayTraceFacing(o0, this.targetYaw, this.targetPitch) != null && (!this.isTellyStrafeActive() && this.lastPlacedPos != null && this.lastPlacedPos.equals((Object)o0.getBlockPos()) && this.lastPlacedFacing == o0.getFacing() || this.isTellyStrafeActive()))) {
                                if (this.isTellyStrafeActive()) {
                                    float[] fArray = this.adjustRotationsForFacing(f4, f12, o0);
                                    this.targetYaw = fArray[0];
                                    this.targetPitch = fArray[1];
                                } else {
                                    this.targetYaw = f4;
                                    this.targetPitch = f12;
                                }
                                this.lastPlacedPos = o0.getBlockPos();
                                this.lastPlacedFacing = o0.getFacing();
                            } else {
                                MovingObjectPosition movingObjectPosition = Scaffold.rayTraceFacing(o0, this.targetYaw, this.targetPitch);
                                if (movingObjectPosition != null) {
                                    object2 = movingObjectPosition.hitVec;
                                }
                            }
                            this.hasRotations = true;
                        } else if (this.isTellyActive()) {
                            n7 = this.isRotationLocked && this.lockedPos != null && (!this.lockedPos.equals((Object)o0.getBlockPos()) || this.lockedFacing != o0.getFacing()) ? 1 : 0;
                            n6 = this.isRotationLocked && this.rayTracePlaceData(o0, this.lockedYaw, this.lockedPitch) == null ? 1 : 0;
                            if (!this.isRotationLocked || n6 != 0 || n7 != 0 || this.alignedTicks <= 1 && !Scaffold.isFalling() && this.isRotationLocked(o0, f4, f12)) {
                                f = Float.isNaN(this.targetLookYaw) ? updateEvent.getYaw() : this.targetLookYaw;
                                float f17 = Float.isNaN(this.targetLookPitch) ? updateEvent.getPitch() : this.targetLookPitch;
                                this.initRotationDelta(f, f17, f4, f12);
                                this.lockedYaw = f4;
                                this.lockedPitch = f12;
                                this.lockedPos = o0.getBlockPos();
                                this.lockedFacing = o0.getFacing();
                                this.isRotationLocked = true;
                                if (n7 != 0 || n6 != 0) {
                                    this.alignedTicks = 0;
                                    this.rotationsAligned = false;
                                }
                            }
                            if (this.isRotationLocked) {
                                this.targetYaw = this.lockedYaw;
                                this.targetPitch = this.lockedPitch;
                                this.hasRotations = true;
                            }
                        } else {
                            this.targetYaw = f4;
                            this.targetPitch = f12;
                            this.hasRotations = true;
                            this.lastPlacedPos = o0.getBlockPos();
                            this.lastPlacedFacing = o0.getFacing();
                        }
                    } else if (this.isTellyActive() && this.isRotationLocked && this.lockedPos != null && this.lockedPos.equals((Object)o0.getBlockPos()) && this.lockedFacing == o0.getFacing()) {
                        this.targetYaw = this.lockedYaw;
                        this.targetPitch = this.lockedPitch;
                        this.hasRotations = true;
                        MovingObjectPosition movingObjectPosition = this.rayTracePlaceData(o0, this.lockedYaw, this.lockedPitch);
                        if (movingObjectPosition != null) {
                            object2 = movingObjectPosition.hitVec;
                        }
                    } else if (this.isTellyActive() && this.alignedTicks == 0) {
                        this.resetRotationLock();
                    } else if (!this.isTellyActive()) {
                        this.lastPlacedPos = null;
                        this.lastPlacedFacing = null;
                    } else if (bl6) {
                        this.hasRotations = false;
                    }
                }
            } else if (this.isTellyActive() && !this.isClutchOrHypixelFastTelly() && this.alignedTicks == 0 && !this.tellyFalling) {
                this.resetRotationLock();
            } else if (!this.isTellyActive()) {
                this.lastPlacedPos = null;
                this.lastPlacedFacing = null;
            } else if (this.isHypixelFastTelly()) {
                this.hasRotations = false;
                this.lastPlacedPos = null;
                this.lastPlacedFacing = null;
            }
            boolean bl10 = !this.isJumping() && (this.tellyPlacing || (Integer)this.towerProp.getValue() == 3 && this.shouldKeepY());
            boolean bl11 = true;
            if (bl7 && !bl10 && o0 != null) {
                MovingObjectPosition movingObjectPosition = Scaffold.rayTraceFacing(o0, updateEvent.getYaw(), updateEvent.getPitch());
                if (movingObjectPosition != null) {
                    float[] fArray = this.getGCDPlacementRotations(o0, updateEvent.getYaw(), updateEvent.getPitch());
                    if (fArray == null) {
                        bl11 = false;
                        object2 = null;
                    } else {
                        this.targetYaw = fArray[0];
                        this.targetPitch = fArray[1];
                        this.hasRotations = true;
                        MovingObjectPosition movingObjectPosition2 = Scaffold.rayTraceFacing(o0, this.targetYaw, this.targetPitch);
                        object2 = movingObjectPosition2 != null ? movingObjectPosition2.hitVec : movingObjectPosition.hitVec;
                        this.wasSneaking = true;
                        if (this.tellyStartTicks > 1) {
                            this.tellyStartTicks = 1;
                        }
                    }
                } else if (object2 != null && this.hasRotations) {
                    float[] fArray = this.getGCDPlacementRotations(o0, this.targetYaw, this.targetPitch);
                    if (fArray == null) {
                        bl11 = false;
                        object2 = null;
                    } else {
                        this.targetYaw = fArray[0];
                        this.targetPitch = fArray[1];
                        MovingObjectPosition movingObjectPosition3 = Scaffold.rayTraceFacing(o0, this.targetYaw, this.targetPitch);
                        if (movingObjectPosition3 != null) {
                            object2 = movingObjectPosition3.hitVec;
                        }
                        this.wasSneaking = true;
                        if (this.tellyStartTicks > 1) {
                            this.tellyStartTicks = 1;
                        }
                    }
                }
            }
            if (!this.isJumping() && !this.isTellyActive() && !this.isClutchOrHypixelFastTelly() && this.hasRotations && MoveUtil.isMoving() && Math.abs(MathHelper.wrapAngleTo180_float((float)(f7 - this.targetYaw))) < 90.0f) {
                switch (n3) {
                    case 2: {
                        this.targetYaw = RotationUtil.roundRotation((float)f7);
                        break;
                    }
                    case 3: {
                        this.targetYaw = RotationUtil.roundRotation((float)f6);
                    }
                }
            }
            f5 = this.targetYaw;
            f4 = this.targetPitch;
            boolean bl12 = bl3 = !this.isTellyActive();
            if (n3 != 0 && (!bl7 || this.wasSneaking || bl10)) {
                f3 = this.targetYaw;
                f2 = this.targetPitch;
                if ((!bl6 || bl5) && this.tellyPlacing && (mc.thePlayer.motionY > 0.0 || mc.thePlayer.posY > (double)(this.targetY + 1))) {
                    float f18;
                    float f19 = MathHelper.wrapAngleTo180_float((float)(this.targetYaw - updateEvent.getYaw()));
                    float f20 = f18 = this.tellyStartTicks >= 2 ? RandomUtil.nextFloat((float)((Float)this.tellyStartRotMinSpeedProp.getValue()).floatValue(), (float)((Float)this.tellyStartRotMaxSpeedProp.getValue()).floatValue()) : RandomUtil.nextFloat((float)((Float)this.tellyNormalRotMinSpeedProp.getValue()).floatValue(), (float)((Float)this.tellyNormalRotMaxSpeedProp.getValue()).floatValue());
                    if (Math.abs(f19) > f18) {
                        float f21 = RotationUtil.applySmoothing((float)f19, (float)f18);
                        f3 = RotationUtil.roundRotation((float)(updateEvent.getYaw() + f21));
                        this.tellyStartTicks = Math.max(this.tellyStartTicks, 1);
                    }
                }
                if (bl10 && this.shouldKeepY()) {
                    if (!bl6 || bl5) {
                        float f22 = MathHelper.wrapAngleTo180_float((float)(mc.thePlayer.rotationYaw - updateEvent.getYaw()));
                        f3 = RotationUtil.roundRotation((float)(updateEvent.getYaw() + f22 * RandomUtil.nextFloat((float)0.98f, (float)0.99f)));
                        f2 = RotationUtil.roundRotation((float)RandomUtil.nextFloat((float)30.0f, (float)80.0f));
                    }
                    this.tellyStartTicks = 3;
                    this.tellyPlacing = true;
                }
                if (this.isClutchOrHypixelFastTelly() && !bl10) {
                    float f23 = updateEvent.getYaw();
                    float f24 = updateEvent.getPitch();
                    boolean bl13 = this.canPlaceOnEdge();
                    boolean bl14 = Scaffold.isFalling();
                    if (bl13) {
                        if (this.isHypixelTellyActive() && this.tellyActive && o0 != null && o0.getFacing() == EnumFacing.UP) {
                            f3 = this.getBasePlacementYaw();
                            f2 = 88.0f;
                        } else if (this.hasRotations && o0 != null) {
                            f3 = this.targetYaw;
                            f2 = Math.min(this.targetPitch, this.getMaxPitch());
                            if (this.isTellyStrafeActive()) {
                                float[] fArray = this.adjustRotationsForFacing(f3, f2, o0);
                                f3 = fArray[0];
                                f2 = fArray[1];
                            }
                        } else if (this.isTellyStrafeActive()) {
                            f3 = this.getTellyStrafeYaw();
                            f2 = 72.0f;
                        } else {
                            f3 = this.selectYawMode(f7, f6);
                            f2 = 72.0f;
                        }
                    } else {
                        f3 = this.getBasePlacementYaw();
                        f2 = this.basePitch;
                    }
                    boolean bl15 = bl13;
                    if (bl15 != this.tellyFalling || this.rotDistance <= 0.0f) {
                        this.initRotationDelta(f23, f24, f3, f2);
                        this.tellyFalling = bl15;
                    }
                    if (this.isClutchTelly()) {
                        if (this.keepYTicks > 0 && mc.thePlayer.motionY > 0.0) {
                            float f25 = MathHelper.wrapAngleTo180_float((float)(f3 - f23));
                            f = this.getRotationStep(false);
                            if (Math.abs(f25) > f) {
                                f3 = RotationUtil.roundRotation((float)(f23 + RotationUtil.applySmoothing((float)f25, (float)f)));
                            }
                        }
                        f5 = f3;
                        f4 = f2;
                        bl3 = true;
                    } else {
                        float[] fArray;
                        float f26;
                        boolean bl16;
                        float f27 = this.getRotationStep(bl15);
                        if (!bl15 && this.keepYTicks > 0 && (mc.thePlayer.onGround || this.lagbackDetected)) {
                            float f28;
                            boolean bl17 = Scaffold.willHitGroundSoon() || mc.thePlayer.onGround;
                            bl16 = mc.thePlayer.onGround && this.isMovingFastOnGround();
                            bl2 = this.isTellyStrafeActive();
                            f28 = bl2 && !mc.thePlayer.onGround ? RandomUtil.nextFloat((float)57.0f, (float)59.5f) : (bl16 ? (bl2 ? RandomUtil.nextFloat((float)54.0f, (float)60.0f) : RandomUtil.nextFloat((float)62.0f, (float)72.0f)) : (bl17 ? (bl2 ? RandomUtil.nextFloat((float)56.0f, (float)60.0f) : RandomUtil.nextFloat((float)56.0f, (float)66.0f)) : (bl2 ? RandomUtil.nextFloat((float)52.0f, (float)58.0f) : RandomUtil.nextFloat((float)50.0f, (float)58.0f))));
                            if (!Float.isNaN(this.lastRotationStep) && Math.abs(f28 - this.lastRotationStep) < 0.05f) {
                                f28 += RandomUtil.nextFloat((float)1.5f, (float)(bl2 ? 3.0f : 4.0f));
                            }
                            if (bl2) {
                                f28 = Math.min(f28, 60.0f);
                            }
                            float f30 = RandomUtil.nextFloat((float)34.0f, (float)46.0f);
                            f26 = f28;
                            fArray = Scaffold.interpolateRotations(f23, f24, f3, f2, f28, f30);
                            if (bl2 && mc.thePlayer.onGround && this.isMovingFastOnGround()) {
                                fArray[0] = f3;
                                fArray[1] = f2;
                                f26 = 180.0f;
                            }
                        } else {
                            float f31 = f27;
                            bl16 = bl15 && o0 != null && this.isTellyStrafeActive() && this.strafeCooldownTicks == 0;
                            if (bl16) {
                                f31 = RandomUtil.nextFloat((float)57.0f, (float)59.5f);
                            }
                            if (!Float.isNaN(this.lastRotationStep) && Math.abs(f31 - this.lastRotationStep) < 0.05f) {
                                f31 = Math.min(bl16 ? 59.5f : 56.0f, f31 + RandomUtil.nextFloat((float)1.5f, (float)4.0f));
                            }
                            f26 = f31;
                            fArray = bl16 ? Scaffold.interpolateRotations(f23, f24, f3, f2, f31, RandomUtil.nextFloat((float)34.0f, (float)46.0f)) : this.smoothRotations(f23, f24, f3, f2, f31);
                        }
                        float[] fArray2 = RotationUtil.applyGCD((float)f23, (float)f24, (float)fArray[0], (float)fArray[1]);
                        f5 = fArray2[0];
                        f4 = MathHelper.clamp_float((float)fArray2[1], (float)-90.0f, (float)90.0f);
                        if (bl15 && f4 > this.getMaxPitch()) {
                            f4 = this.getMaxPitch();
                        }
                        bl16 = Scaffold.isRotationClose(f5, f4, f3, f2);
                        bl2 = false;
                        boolean bl18 = bl13 && (bl14 || mc.thePlayer.motionY < 0.12);
                        if (bl18 && o0 != null) {
                            if (this.rayTracePlaceData(o0, f5, f4) != null) {
                                bl2 = true;
                            } else if (this.rayTracePlaceData(o0, f23, Math.min(f24, this.getMaxPitch())) != null) {
                                f5 = f23;
                                f4 = Math.min(f24, this.getMaxPitch());
                                bl2 = true;
                            }
                        }
                        if (bl16 && !bl2) {
                            float[] fArray3;
                            float[] fArray4 = RotationUtil.applyGCD((float)f23, (float)f24, (float)f3, (float)f2);
                            if (Math.abs(MathHelper.wrapAngleTo180_float((float)(fArray4[0] - f23))) <= f26 + 1.0f) {
                                f5 = fArray4[0];
                                f4 = MathHelper.clamp_float((float)fArray4[1], (float)-90.0f, (float)90.0f);
                                if (bl15 && f4 > this.getMaxPitch()) {
                                    f4 = this.getMaxPitch();
                                }
                                if (bl18 && o0 != null && this.rayTracePlaceData(o0, f5, f4) != null) {
                                    bl2 = true;
                                }
                            }
                        }
                        float f32 = Math.abs(MathHelper.wrapAngleTo180_float((float)(f5 - f23)));
                        n = bl15 && o0 != null && Math.abs(MathHelper.wrapAngleTo180_float((float)(f3 - f5))) <= 6.0f && Math.abs(f2 - f4) <= 6.0f && f32 <= 10.0f ? 1 : 0;
                        boolean bl19 = bl3 = bl2 && o0 != null && (bl16 || n != 0 || f32 <= 12.0f) && bl14 && bl13;
                        if (bl2 && bl15) {
                            this.hasRotations = true;
                            this.lastPlacedPos = o0.getBlockPos();
                            this.lastPlacedFacing = o0.getFacing();
                        }
                        this.tellyStartTicks = 0;
                        this.lastRotationStep = Math.abs(MathHelper.wrapAngleTo180_float((float)(f5 - f23)));
                    }
                    this.targetYaw = f5;
                    this.targetPitch = f4;
                    this.targetLookYaw = f5;
                    this.targetLookPitch = f4;
                    this.updateFacingAlignment(f5);
                    updateEvent.setRotation(f5, f4, 3);
                    if ((Integer)this.moveFixProp.getValue() == 1) {
                        updateEvent.setPervRotation(f5, 3);
                    }
                } else if (this.isTellyActive() && !bl10) {
                    float f33 = updateEvent.getYaw();
                    float f34 = updateEvent.getPitch();
                    boolean bl20 = this.canPlaceOnEdge();
                    boolean bl21 = this.canPlaceWhileFalling();
                    if (this.canFastTellyJump() && (Integer)this.fastTellyProp.getValue() == 1) {
                        f3 = this.getBasePlacementYaw();
                        f2 = this.basePitch;
                        this.tellyFalling = false;
                        this.rotationsAligned = false;
                        this.alignedTicks = 0;
                        this.initRotationDelta(f33, f34, f3, f2);
                        float[] fArray = RotationUtil.applyGCD((float)f33, (float)f34, (float)f3, (float)f2);
                        f5 = fArray[0];
                        f4 = MathHelper.clamp_float((float)fArray[1], (float)-90.0f, (float)90.0f);
                        this.targetYaw = f5;
                        this.targetPitch = f4;
                        this.targetLookYaw = f5;
                        this.targetLookPitch = f4;
                        bl3 = false;
                        updateEvent.setRotation(f5, f4, 3);
                        if ((Integer)this.moveFixProp.getValue() == 1) {
                            updateEvent.setPervRotation(f5, 3);
                        }
                    } else {
                        if (bl20) {
                            if (this.isRotationLocked) {
                                f3 = this.lockedYaw;
                                f2 = this.lockedPitch;
                            } else {
                                f3 = this.selectYawMode(f7, f6);
                                f2 = 85.0f;
                            }
                        } else {
                            f3 = this.getBasePlacementYaw();
                            f2 = this.basePitch;
                            this.rotationsAligned = false;
                            this.alignedTicks = 0;
                        }
                        if (bl20 != this.tellyFalling || this.rotDistance <= 0.0f) {
                            this.initRotationDelta(f33, f34, f3, f2);
                            if (bl20 != this.tellyFalling) {
                                this.alignedTicks = 0;
                            }
                            this.tellyFalling = bl20;
                        }
                        float[] fArray = RotationUtil.applyGCD((float)f33, (float)f34, (float)f3, (float)f2);
                        float[] fArray5 = fArray;
                        fArray[1] = MathHelper.clamp_float((float)fArray5[1], (float)-90.0f, (float)90.0f);
                        boolean bl22 = Scaffold.isRotationClose(f33, f34, fArray5[0], fArray5[1]);
                        if (bl21 && this.isRotationLocked && o0 != null && !bl22) {
                            float f35 = Math.max(this.getRandomRotationSpeed(), RandomUtil.nextFloat((float)((Float)this.tellyStartRotMinSpeedProp.getValue()).floatValue(), (float)((Float)this.tellyStartRotMaxSpeedProp.getValue()).floatValue()));
                            float[] fArray6 = this.smoothRotations(f33, f34, f3, f2, f35);
                            float[] fArray7 = RotationUtil.applyGCD((float)f33, (float)f34, (float)fArray6[0], (float)fArray6[1]);
                            f5 = fArray7[0];
                            if (Scaffold.isRotationClose(f5, f4 = MathHelper.clamp_float((float)fArray7[1], (float)-90.0f, (float)90.0f), fArray5[0], fArray5[1])) {
                                f5 = fArray5[0];
                                f4 = fArray5[1];
                                this.alignedTicks = 0;
                                this.rotationsAligned = true;
                                bl3 = false;
                            } else {
                                this.alignedTicks = 0;
                                bl3 = false;
                            }
                            this.tellyStartTicks = 0;
                        } else if (bl20 && this.isRotationLocked && (bl22 || this.alignedTicks > 0)) {
                            float f36;
                            if (this.alignedTicks == 0) {
                                f5 = fArray5[0];
                                f36 = fArray5[1];
                            } else {
                                f5 = Float.isNaN(this.targetLookYaw) ? fArray5[0] : this.targetLookYaw;
                                f36 = Float.isNaN(this.targetLookPitch) ? fArray5[1] : this.targetLookPitch;
                            }
                            f4 = f36;
                            ++this.alignedTicks;
                            this.rotationsAligned = true;
                            bl3 = bl21 && o0 != null && this.rayTracePlaceData(o0, f5, f4) != null && this.alignedTicks > 0;
                            this.tellyStartTicks = 0;
                        } else {
                            float f37 = this.getRandomRotationSpeed();
                            if (bl20 && this.isRotationLocked) {
                                f37 = Math.max(f37, RandomUtil.nextFloat((float)((Float)this.tellyStartRotMinSpeedProp.getValue()).floatValue(), (float)((Float)this.tellyStartRotMaxSpeedProp.getValue()).floatValue()));
                            }
                            float[] fArray8 = this.smoothRotations(f33, f34, f3, f2, f37);
                            float[] fArray9 = RotationUtil.applyGCD((float)f33, (float)f34, (float)fArray8[0], (float)fArray8[1]);
                            f5 = fArray9[0];
                            f4 = MathHelper.clamp_float((float)fArray9[1], (float)-90.0f, (float)90.0f);
                            if (bl20 && this.isRotationLocked && Scaffold.isRotationClose(f5, f4, fArray5[0], fArray5[1])) {
                                f5 = fArray5[0];
                                f4 = fArray5[1];
                                this.alignedTicks = 0;
                                this.rotationsAligned = true;
                                bl3 = false;
                                this.tellyStartTicks = 0;
                            } else {
                                this.alignedTicks = 0;
                                bl3 = false;
                            }
                        }
                        this.targetYaw = f5;
                        this.targetPitch = f4;
                        this.targetLookYaw = f5;
                        this.targetLookPitch = f4;
                        updateEvent.setRotation(f5, f4, 3);
                        if ((Integer)this.moveFixProp.getValue() == 1) {
                            updateEvent.setPervRotation(f5, 3);
                        }
                    }
                } else if (this.isHypixelRotationsActive()) {
                    updateEvent.setRotation(f3, f2, 3);
                    if ((Integer)this.moveFixProp.getValue() == 1) {
                        updateEvent.setPervRotation(f3, 3);
                    }
                } else if (this.isTellyActive()) {
                    float[] fArray = RotationUtil.applyGCD((float)updateEvent.getYaw(), (float)updateEvent.getPitch(), (float)f3, (float)f2);
                    f5 = fArray[0];
                    f4 = MathHelper.clamp_float((float)fArray[1], (float)-90.0f, (float)90.0f);
                    this.targetYaw = f5;
                    this.targetPitch = f4;
                    bl3 = true;
                    updateEvent.setRotation(f5, f4, 3);
                    if ((Integer)this.moveFixProp.getValue() == 1) {
                        updateEvent.setPervRotation(f5, 3);
                    }
                } else {
                    n2 = n3;
                    bl4 = this.isJumping() || n2 == 1 || n2 == 2 || n2 == 3 || n2 == 5;
                    float f38 = f3;
                    float f39 = f2;
                    if (bl4) {
                        float f40 = this.isJumping() ? 18.0f : 32.0f;
                        float f41 = MathHelper.wrapAngleTo180_float((float)(f3 - updateEvent.getYaw()));
                        f = f2 - updateEvent.getPitch();
                        float f42 = RotationUtil.applySmoothing((float)f41, (float)f40);
                        float f43 = RotationUtil.applySmoothing((float)f, (float)f40);
                        f38 = updateEvent.getYaw() + f42;
                        f39 = MathHelper.clamp_float((float)(updateEvent.getPitch() + f43), (float)-90.0f, (float)90.0f);
                    }
                    float[] fArray = RotationUtil.applyGCD((float)updateEvent.getYaw(), (float)updateEvent.getPitch(), (float)f38, (float)f39);
                    f5 = fArray[0];
                    f4 = MathHelper.clamp_float((float)fArray[1], (float)-90.0f, (float)90.0f);
                    this.targetYaw = f5;
                    this.targetPitch = f4;
                    updateEvent.setRotation(f5, f4, 3);
                    if ((Integer)this.moveFixProp.getValue() == 1) {
                        updateEvent.setPervRotation(f5, 3);
                    }
                    if (o0 != null) {
                        MovingObjectPosition movingObjectPosition = Scaffold.rayTraceFacing(o0, f5, f4);
                        Vec3 vec3 = object2 = movingObjectPosition != null ? movingObjectPosition.hitVec : null;
                        if (bl7) {
                            boolean bl23 = bl11 = movingObjectPosition != null;
                        }
                    }
                }
            }
            if (this.isTellyActive() && !this.isClutchOrHypixelFastTelly() && o0 != null && object2 != null && !this.isRotationLocked) {
                Scaffold ooOoOo = this;
                this.applyGCDJitter(ooOoOo.targetYaw);
            }
            boolean bl24 = bl = this.tellyStartTicks <= 0 && (this.isHypixelRotationsActive() || this.isClutchOrHypixelFastTelly() || this.shouldPlaceBlock());
            if (!this.isTellyActive() && !bl && this.tellyStartTicks <= 0 && (Scaffold.isBlockBelowReplaceable() || Scaffold.isSteppingOffEdge() || this.isReplaceableAhead(2))) {
                bl = true;
                this.lastPlacementTime = 0L;
            }
            if (this.isHypixelFastTelly()) {
                boolean bl25;
                boolean bl26;
                boolean bl27;
                bl = false;
                boolean bl28 = this.isTellyStrafeActive();
                n2 = this.isHypixelTellyActive() ? 1 : 0;
                boolean bl29 = bl4 = n2 != 0 && this.tellyWaiting;
                if (n2 != 0 && this.tellyActive && !bl4) {
                    o0 = this.calculatePlaceData();
                }
                if (bl28 && this.isMovingDiagonally()) {
                    this.hypixelTellyInAir = true;
                }
                boolean bl30 = this.jumpCooldownTicks <= 2 || bl28;
                int n10 = bl30 ? 8 : 5;
                boolean bl31 = this.hasLineOfSight(n10);
                boolean bl32 = this.isBlockUnderReplaceable();
                boolean bl33 = this.strafeCooldownTicks < (bl30 ? 3 : 2);
                boolean bl34 = Scaffold.willHitGroundSoon();
                boolean bl35 = mc.thePlayer.onGround && Scaffold.isOverAirAhead(2);
                boolean bl36 = !mc.thePlayer.onGround && (this.canPlaceOnEdge() || mc.thePlayer.motionY < 0.12 || bl32 || bl34);
                boolean bl37 = bl27 = mc.thePlayer.onGround && this.hasLineOfSight(3);
                bl2 = mc.thePlayer.onGround && ItemUtil.isHoldingValidBlock() && this.placeCooldownTicks <= 0 && !bl7 && (bl31 || bl27) && (bl28 ? bl32 || bl35 || Scaffold.isOverAirAhead(1) : this.isHypixelTellyTicking() || bl32 || bl35 || bl27 || bl30);
                boolean bl38 = bl32 || bl35 || bl34 || bl30 || bl2 && this.isHypixelTellyTicking() || bl28 && mc.thePlayer.onGround && (bl32 || Scaffold.isOverAirAhead(1));
                boolean bl39 = ItemUtil.isHoldingValidBlock() && !bl4 && this.placeCooldownTicks <= 0 && !bl7 && (bl32 || bl31 || bl27 || bl28 && bl33) && (bl2 || bl36 && (this.canPlaceOnEdge() || bl32 || bl33 || bl34));
                float f44 = Math.abs(MathHelper.wrapAngleTo180_float((float)(f5 - updateEvent.getYaw())));
                boolean bl40 = bl26 = f4 <= this.getMaxPitch() + 0.5f;
                boolean bl41 = bl25 = f44 <= (bl28 ? (this.strafeCooldownTicks < 5 ? 62.0f : 55.0f) : (bl38 || bl33 || bl2 ? 32.0f : 14.0f));
                if (bl39 && bl26 && bl25) {
                    int n11 = Scaffold.getPingAdjustedCount(bl28 || n2 != 0 ? 1 : (bl38 || bl30 ? 5 : (bl3 ? 3 : (bl33 || bl2 ? 2 : 1))));
                    for (int i = 0; i < n11 && (this.isBlockUnderReplaceable() || this.hasLineOfSight(n10) || bl28 && bl33); ++i) {
                        boolean bl42;
                        MovingObjectPosition movingObjectPosition;
                        PlaceData o02;
                        PlaceData o03 = i == 0 && o0 != null && (this.canPlaceOnEdge() || bl2 || bl32) ? o0 : this.calculatePlaceData();
                        MovingObjectPosition movingObjectPosition4 = o03 != null ? this.rayTracePlaceData(o03, f5, f4) : null;
                        if (movingObjectPosition4 == null && i == 0 && (o02 = this.calculatePlaceData()) != null) {
                            movingObjectPosition4 = this.rayTracePlaceData(o02, f5, f4);
                        }
                        if (movingObjectPosition4 == null && (n2 == 0 || !this.tellyActive) && (bl38 || bl2 || bl33 || bl3 || i == 0 || bl28) && (movingObjectPosition = this.rayTracePlacement(f5, f4)) != null && (!bl28 || this.isValidPlacementPos(movingObjectPosition.getBlockPos().offset(movingObjectPosition.sideHit)))) {
                            movingObjectPosition4 = movingObjectPosition;
                        }
                        if (bl28 && movingObjectPosition4 != null && movingObjectPosition4.sideHit == EnumFacing.DOWN) {
                            movingObjectPosition4 = null;
                        }
                        boolean bl43 = bl42 = movingObjectPosition4 != null && (bl3 || bl38 || bl2 || bl33 || i == 0 && f44 <= 16.0f);
                        if (!(i <= 0 || bl3 || bl38 || bl2)) {
                            bl42 = false;
                        }
                        if (!bl42) break;
                        int n12 = this.eagleBlocksPlaced;
                        this.placeBlock(movingObjectPosition4.getBlockPos(), movingObjectPosition4.sideHit, movingObjectPosition4.hitVec);
                        if (this.eagleBlocksPlaced <= n12) break;
                        this.strafeFlag = true;
                        ++this.strafeCooldownTicks;
                        if (bl28 && this.lagbackDetected) continue;
                        this.lagbackDetected = false;
                    }
                }
                if (!bl28 && n2 == 0 && !Scaffold.isHighPing() && ItemUtil.isHoldingValidBlock() && this.placeCooldownTicks <= 0 && !bl7 && (this.canPlaceOnEdge() || Scaffold.isFalling() || bl2 || bl32) && this.hasLineOfSight(n10 + 1)) {
                    PlaceData o04 = this.calculatePlaceData();
                    MovingObjectPosition movingObjectPosition = o04 != null ? this.rayTracePlaceData(o04, f5, f4) : null;
                    if (!(movingObjectPosition != null || n2 != 0 && this.tellyActive)) {
                        movingObjectPosition = this.rayTracePlacement(f5, f4);
                    }
                    if (movingObjectPosition != null) {
                        int n13 = this.eagleBlocksPlaced;
                        this.placeBlock(movingObjectPosition.getBlockPos(), movingObjectPosition.sideHit, movingObjectPosition.hitVec);
                        if (this.eagleBlocksPlaced > n13) {
                            this.strafeFlag = true;
                            ++this.strafeCooldownTicks;
                            if (!this.lagbackDetected) {
                                this.lagbackDetected = false;
                            }
                        }
                    }
                }
                if (!bl4 && !this.placedThisTick && ItemUtil.isHoldingValidBlock() && this.placeCooldownTicks <= 0 && !bl7 && (bl32 || !bl28 && Scaffold.isFalling() && (bl31 || bl30) || bl28 && !mc.thePlayer.onGround && (bl32 || bl31 || bl33) || mc.thePlayer.onGround && (bl32 || bl35 || !bl28 && (bl27 || this.isHypixelTellyTicking() || bl30)))) {
                    MovingObjectPosition movingObjectPosition;
                    PlaceData o05 = this.calculatePlaceData();
                    MovingObjectPosition movingObjectPosition5 = o05 != null ? this.rayTracePlaceData(o05, f5, f4) : null;
                    if (!(movingObjectPosition5 != null || n2 != 0 && this.tellyActive || (movingObjectPosition = this.rayTracePlacement(f5, f4)) == null || bl28 && !this.isValidPlacementPos(movingObjectPosition.getBlockPos().offset(movingObjectPosition.sideHit)))) {
                        movingObjectPosition5 = movingObjectPosition;
                    }
                    if (bl28 && movingObjectPosition5 != null && movingObjectPosition5.sideHit == EnumFacing.DOWN) {
                        movingObjectPosition5 = null;
                    }
                    if (movingObjectPosition5 != null && bl25) {
                        this.placeBlock(movingObjectPosition5.getBlockPos(), movingObjectPosition5.sideHit, movingObjectPosition5.hitVec);
                        if (this.placedThisTick) {
                            this.strafeFlag = true;
                            ++this.strafeCooldownTicks;
                            if (!bl28 || !this.lagbackDetected) {
                                this.lagbackDetected = false;
                            }
                        }
                    }
                }
            }
            if (this.isTellyActive() && !this.isClutchOrHypixelFastTelly() && !bl7 && !this.isHypixelFastTelly()) {
                MovingObjectPosition movingObjectPosition;
                PlaceData o06;
                bl = this.placeCooldownTicks <= 0 && this.canPlaceWhileFalling() && o0 != null && ItemUtil.isHoldingValidBlock();
                if (bl) {
                    MovingObjectPosition movingObjectPosition6 = this.rayTracePlaceData(o0, f5, f4);
                    if (movingObjectPosition6 != null && (bl3 || this.alignedTicks > 0 || this.isRotationLocked && Scaffold.isRotationClose(f5, f4, this.lockedYaw, this.lockedPitch))) {
                        this.placeBlock(movingObjectPosition6.getBlockPos(), movingObjectPosition6.sideHit, movingObjectPosition6.hitVec);
                    }
                    bl = false;
                }
                if (this.canPlaceWhileFalling() && !this.placedThisTick && this.isBlockUnderReplaceable() && ItemUtil.isHoldingValidBlock() && this.placeCooldownTicks <= 0 && (o06 = this.calculatePlaceData()) != null && (movingObjectPosition = this.rayTracePlaceData(o06, f5, f4)) != null) {
                    this.placeBlock(movingObjectPosition.getBlockPos(), movingObjectPosition.sideHit, movingObjectPosition.hitVec);
                }
            }
            if (bl6 && o0 != null && object2 != null) {
                MovingObjectPosition movingObjectPosition = Scaffold.rayTraceFacing(o0, f5, f4);
                object2 = movingObjectPosition == null ? null : movingObjectPosition.hitVec;
            }
            if (o0 != null && object2 != null && bl11 && bl) {
                this.placeBlock(o0.getBlockPos(), o0.getFacing(), (Vec3)object2);
                if (bl7) {
                    this.saveLastRotations();
                }
                if (!(!((Boolean)this.multiPlaceProp.getValue()).booleanValue() || bl7 || this.isJumping() || this.isTellyActive() && !this.isClutchTelly())) {
                    int n14 = !this.isTellyActive() && (Scaffold.isBlockBelowReplaceable() || Scaffold.isSteppingOffEdge()) ? 5 : 3;
                    for (int i = 0; i < n14 && (o0 = this.calculatePlaceData()) != null; ++i) {
                        float[] fArray;
                        MovingObjectPosition movingObjectPosition = Scaffold.rayTraceFacing(o0, f5, f4);
                        if (movingObjectPosition == null && !this.isTellyActive()) {
                            movingObjectPosition = this.rayTracePlacement(f5, f4);
                        }
                        if (movingObjectPosition != null) {
                            this.placeBlock(movingObjectPosition.getBlockPos(), movingObjectPosition.sideHit, movingObjectPosition.hitVec);
                            continue;
                        }
                        object2 = BlockUtil.getHitVec((BlockPos)o0.getBlockPos(), (EnumFacing)o0.getFacing());
                        double d8 = object2.xCoord - mc.thePlayer.posX;
                        double d9 = object2.yCoord - mc.thePlayer.posY - (double)mc.thePlayer.getEyeHeight();
                        double d10 = object2.zCoord - mc.thePlayer.posZ;
                        float[] fArray10 = RotationUtil.getRotations((double)d8, (double)d9, (double)d10, (float)updateEvent.getYaw(), (float)updateEvent.getPitch());
                        if (!(Math.abs(fArray10[0] - this.targetYaw) < 120.0f) || !(Math.abs(fArray10[1] - this.targetPitch) < 60.0f) || (movingObjectPosition = RotationUtil.rayTrace((float)fArray10[0], (float)fArray10[1], (double)mc.playerController.getBlockReachDistance())) == null || movingObjectPosition.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !movingObjectPosition.getBlockPos().equals((Object)o0.getBlockPos()) || movingObjectPosition.sideHit != o0.getFacing()) break;
                        this.placeBlock(o0.getBlockPos(), o0.getFacing(), movingObjectPosition.hitVec);
                    }
                }
            }
            if (!this.isTellyActive() && !bl7 && !this.isJumping() && ItemUtil.isHoldingValidBlock() && (Scaffold.isBlockBelowReplaceable() || Scaffold.isSteppingOffEdge() || this.isReplaceableAhead(3))) {
                this.lastPlacementTime = 0L;
                float f45 = Math.abs(MathHelper.wrapAngleTo180_float((float)(f5 - updateEvent.getYaw())));
                boolean bl44 = Scaffold.isBlockBelowReplaceable() || Scaffold.isSteppingOffEdge();
                float f46 = bl44 ? 28.0f : 20.0f;
                if (f45 <= f46) {
                    int n15 = bl44 ? 6 : 4;
                    for (int i = 0; i < n15 && (Scaffold.isBlockBelowReplaceable() || this.isReplaceableAhead(3) || Scaffold.isSteppingOffEdge()); ++i) {
                        PlaceData o07 = this.calculatePlaceData();
                        MovingObjectPosition movingObjectPosition = o07 != null ? Scaffold.rayTraceFacing(o07, f5, f4) : null;
                        if (movingObjectPosition == null) {
                            movingObjectPosition = this.rayTracePlacement(f5, f4);
                        }
                        if (movingObjectPosition == null) break;
                        int n16 = this.eagleBlocksPlaced;
                        this.placeBlock(movingObjectPosition.getBlockPos(), movingObjectPosition.sideHit, movingObjectPosition.hitVec);
                        if (this.eagleBlocksPlaced <= n16) break;
                    }
                }
            }
            if (this.targetFacing != null) {
                if (bl6) {
                    this.targetFacing = null;
                } else if (this.tellyStartTicks <= 0 && !this.placedThisTick) {
                    int n17 = MathHelper.floor_double((double)mc.thePlayer.posX);
                    int n18 = MathHelper.floor_double((double)mc.thePlayer.posY);
                    int n19 = MathHelper.floor_double((double)mc.thePlayer.posZ);
                    BlockPos blockPos = new BlockPos(n17, n18 - 1, n19);
                    object = blockPos;
                    object2 = BlockUtil.getHitVecOrRayTrace((BlockPos)blockPos, (EnumFacing)this.targetFacing, (float)this.targetYaw, (float)this.targetPitch);
                    this.placeBlock((BlockPos)object, this.targetFacing, (Vec3)object2);
                }
                this.targetFacing = null;
                return;
            }
            if (!((Integer)this.keepYProp.getValue() != 2 && !this.isTellyMode() || this.keepYTicks <= 0 || mc.thePlayer.onGround || this.isJumping() || this.isHypixelFastTelly() && !this.isBlockUnderReplaceable() || MathHelper.floor_double((double)(mc.thePlayer.posY + mc.thePlayer.motionY)) > this.targetY || !(mc.thePlayer.posY > (double)(this.targetY + 1)))) {
                this.shouldUpdateY = true;
                o0 = this.calculatePlaceData();
                if (!this.placedThisTick && this.placeCooldownTicks <= 0) {
                    MovingObjectPosition movingObjectPosition;
                    MovingObjectPosition movingObjectPosition7 = movingObjectPosition = o0 != null ? Scaffold.rayTraceFacing(o0, f5, f4) : null;
                    if (this.isHypixelFastTelly()) {
                        boolean bl45 = this.isBlockUnderReplaceable();
                        float f47 = Math.abs(MathHelper.wrapAngleTo180_float((float)(f5 - updateEvent.getYaw())));
                        boolean bl46 = this.isTellyStrafeActive();
                        if (bl46) {
                            movingObjectPosition = null;
                        } else if (movingObjectPosition != null && f47 > 18.0f && !bl45 && !bl3) {
                            movingObjectPosition = null;
                        }
                        if (movingObjectPosition == null && !bl46 && (bl45 || bl3 || f47 <= 18.0f)) {
                            movingObjectPosition = this.rayTracePlacement(f5, f4);
                        }
                    }
                    if (movingObjectPosition != null) {
                        this.placeBlock(movingObjectPosition.getBlockPos(), movingObjectPosition.sideHit, movingObjectPosition.hitVec);
                        if (this.isHypixelFastTelly() && this.placedThisTick) {
                            this.strafeFlag = true;
                            ++this.strafeCooldownTicks;
                            if (!this.isTellyStrafeActive() || !this.lagbackDetected) {
                                this.lagbackDetected = false;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent packetEvent) {
        if (!this.isEnabled() || packetEvent.getType() != EventType.RECEIVE) {
            return;
        }
        if (!(packetEvent.getPacket() instanceof S08PacketPlayerPosLook)) {
            return;
        }
        if (this.isHypixelFastTelly()) {
            this.lagbackDetected = true;
            this.facingTicks = 0;
            this.tellyBlockCount = 4;
            this.tellyDelayTicks = Math.min(14, 5 + Scaffold.getPing() / 50);
            this.hasRotations = false;
            this.lastPlacedPos = null;
            this.lastPlacedFacing = null;
            this.resetRotationLock();
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent strafeEvent) {
        if (this.isEnabled()) {
            if (this.freezeMovementTicks > 0) {
                strafeEvent.setForward(0.0f);
                strafeEvent.setStrafe(0.0f);
                return;
            }
            if (this.is3FMCRotations() && !this.is3FMCTelly()) {
                this.strafeStage = 0;
                this.groundDelayTicks = 0;
                return;
            }
            if (!mc.thePlayer.isCollidedHorizontally && mc.thePlayer.hurtTime <= 5 && !mc.thePlayer.isPotionActive(Potion.jump) && mc.gameSettings.keyBindJump.isKeyDown() && ItemUtil.isHoldingValidBlock() && !this.isTellyMode()) {
                int n = (int)(mc.thePlayer.posY % 1.0 * 100.0);
                switch ((Integer)this.towerProp.getValue()) {
                    case 1: {
                        switch (this.strafeStage) {
                            case 0: {
                                if (mc.thePlayer.onGround) {
                                    this.strafeStage = 1;
                                    mc.thePlayer.motionY = -0.0784000015258789;
                                }
                                return;
                            }
                            case 1: {
                                if (n == 0 && PlayerUtil.isBlockBelow()) {
                                    this.targetY = MathHelper.floor_double((double)mc.thePlayer.posY);
                                    this.strafeStage = 2;
                                    mc.thePlayer.motionY = 0.42f;
                                    if (MoveUtil.isMoving()) {
                                        MoveUtil.strafe((double)MoveUtil.getBaseSpeed(), (float)MoveUtil.getMotionYaw());
                                        return;
                                    }
                                    MoveUtil.setSpeed((double)0.0);
                                    strafeEvent.setForward(0.0f);
                                    strafeEvent.setStrafe(0.0f);
                                    return;
                                }
                                this.strafeStage = 0;
                                return;
                            }
                            case 2: {
                                this.strafeStage = 3;
                                mc.thePlayer.motionY = 0.75 - mc.thePlayer.posY % 1.0;
                                return;
                            }
                            case 3: {
                                this.strafeStage = 1;
                                mc.thePlayer.motionY = 1.0 - mc.thePlayer.posY % 1.0;
                                return;
                            }
                        }
                        this.strafeStage = 0;
                        return;
                    }
                    case 2: {
                        switch (this.strafeStage) {
                            case 0: {
                                if (mc.thePlayer.onGround) {
                                    this.strafeStage = 1;
                                    mc.thePlayer.motionY = -0.0784000015258789;
                                }
                                return;
                            }
                            case 1: {
                                if (n == 0 && PlayerUtil.isBlockBelow()) {
                                    this.targetY = MathHelper.floor_double((double)mc.thePlayer.posY);
                                    if (!MoveUtil.isMoving()) {
                                        double d2;
                                        double d3;
                                        this.groundDelayTicks = 2;
                                        MoveUtil.setSpeed((double)0.0);
                                        strafeEvent.setForward(0.0f);
                                        strafeEvent.setStrafe(0.0f);
                                        float f = MathHelper.wrapAngleTo180_float((float)(this.targetYaw - 180.0f));
                                        EnumFacing enumFacing = f < -135.0f || f > 135.0f ? EnumFacing.NORTH : (f < -45.0f ? EnumFacing.EAST : (f < 45.0f ? EnumFacing.SOUTH : EnumFacing.WEST));
                                        switch (SyntheticSwitchMap.I[enumFacing.ordinal()]) {
                                            case 1: {
                                                d3 = d2 = mc.thePlayer.posZ - Math.floor(mc.thePlayer.posZ);
                                                break;
                                            }
                                            case 2: {
                                                d3 = d2 = Math.ceil(mc.thePlayer.posX) - mc.thePlayer.posX;
                                                break;
                                            }
                                            case 3: {
                                                d3 = d2 = Math.ceil(mc.thePlayer.posZ) - mc.thePlayer.posZ;
                                                break;
                                            }
                                            default: {
                                                d3 = d2 = mc.thePlayer.posX - Math.floor(mc.thePlayer.posX);
                                            }
                                        }
                                        if (d3 > 0.1) {
                                            if (mc.thePlayer.onGround) {
                                                Vec3i dirVec = enumFacing.getDirectionVec();
                                                double d4 = Math.min(0.2155 - RandomUtil.nextDouble(1.0E-4, 9.0E-4), d2 - 0.05);
                                                double d5 = RandomUtil.nextDouble(0.02, 0.03);
                                                AxisAlignedBB offsetBB = mc.thePlayer.getEntityBoundingBox().offset((double)dirVec.getX() * (d4 - d5), 0.0, (double)dirVec.getZ() * (d4 - d5));
                                                if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, offsetBB).isEmpty()) {
                                                    mc.thePlayer.motionY = -0.0784000015258789;
                                                    mc.thePlayer.setPosition(offsetBB.minX + (offsetBB.maxX - offsetBB.minX) / 2.0, offsetBB.minY, offsetBB.minZ + (offsetBB.maxZ - offsetBB.minZ) / 2.0);
                                                }
                                                return;
                                            }
                                        } else {
                                            this.strafeStage = 2;
                                            this.targetFacing = enumFacing;
                                            mc.thePlayer.motionY = 0.42f;
                                        }
                                        return;
                                    }
                                    this.strafeStage = 2;
                                    ++this.groundDelayTicks;
                                    mc.thePlayer.motionY = 0.42f;
                                    MoveUtil.strafe((double)MoveUtil.getBaseSpeed(), (float)MoveUtil.getMotionYaw());
                                    return;
                                }
                                this.strafeStage = 0;
                                this.groundDelayTicks = 0;
                                return;
                            }
                            case 2: {
                                this.strafeStage = 3;
                                mc.thePlayer.motionY -= RandomUtil.nextDouble((double)0.00101, (double)0.00109);
                                return;
                            }
                            case 3: {
                                if (this.groundDelayTicks >= 4) {
                                    this.strafeStage = 4;
                                    this.groundDelayTicks = 0;
                                    return;
                                }
                                this.strafeStage = 1;
                                mc.thePlayer.motionY = 1.0 - mc.thePlayer.posY % 1.0;
                                return;
                            }
                            case 4: {
                                this.strafeStage = 5;
                                return;
                            }
                            case 5: {
                                if (!PlayerUtil.isBlockBelow()) {
                                    this.strafeStage = 0;
                                    return;
                                }
                                this.strafeStage = 1;
                                mc.thePlayer.motionY -= 0.08;
                                mc.thePlayer.motionY *= (double)0.98f;
                                mc.thePlayer.motionY -= 0.08;
                                mc.thePlayer.motionY *= (double)0.98f;
                                return;
                            }
                        }
                        this.strafeStage = 0;
                        this.groundDelayTicks = 0;
                        return;
                    }
                }
                this.strafeStage = 0;
                this.groundDelayTicks = 0;
                return;
            }
            this.strafeStage = 0;
            this.groundDelayTicks = 0;
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent moveInputEvent) {
        if (this.isEnabled()) {
            boolean bl;
            if (this.freezeMovementTicks > 0) {
                mc.thePlayer.movementInput.moveForward = 0.0f;
                mc.thePlayer.movementInput.moveStrafe = 0.0f;
                mc.thePlayer.movementInput.jump = false;
                mc.thePlayer.movementInput.sneak = false;
                return;
            }
            if (this.isHypixelTellyActive() && mc.thePlayer.onGround) {
                if (this.isHypixelTellyReady()) {
                    mc.thePlayer.movementInput.jump = true;
                } else {
                    mc.thePlayer.movementInput.jump = false;
                    mc.thePlayer.movementInput.moveForward = 0.0f;
                    mc.thePlayer.movementInput.moveStrafe = 0.0f;
                }
            }
            if ((Integer)this.moveFixProp.getValue() == 1 && MovementManager.isRotated() && MovementManager.getPriority() == 3.0f && MoveUtil.isMoving()) {
                MoveUtil.fixMovement((float)MovementManager.getMovementYaw());
            }
            this.stopSprint3FMC();
            boolean bl2 = bl = this.isHypixelFastTelly() && this.shouldTellyJump();
            if (mc.thePlayer.onGround && this.keepYTicks > 0 && MoveUtil.isMoving() && !mc.gameSettings.keyBindJump.isKeyDown() && (!this.isHypixelFastTelly() || bl)) {
                mc.thePlayer.movementInput.jump = true;
            }
            if (this.eagleSneaking && !mc.thePlayer.movementInput.sneak) {
                mc.thePlayer.movementInput.sneak = true;
                mc.thePlayer.movementInput.moveForward *= 0.3f;
                mc.thePlayer.movementInput.moveStrafe *= 0.3f;
            }
            if (!(this.isHypixelRotationsActive() || this.isClutchOrHypixelFastTelly() || this.isTellyActive() || this.isJumping() || !mc.thePlayer.onGround || mc.thePlayer.movementInput.sneak || this.eagleSneaking)) {
                ++this.speedPotionTicks;
                if (this.speedPotionTicks >= 22) {
                    mc.thePlayer.movementInput.sneak = true;
                    mc.thePlayer.movementInput.moveForward *= 0.3f;
                    mc.thePlayer.movementInput.moveStrafe *= 0.3f;
                    this.speedPotionTicks = 0;
                }
            }
        }
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent livingUpdateEvent) {
        if (this.isEnabled()) {
            int n;
            float f;
            float f2;
            if (this.freezeMovementTicks > 0) {
                mc.thePlayer.motionX = 0.0;
                mc.thePlayer.motionY = 0.0;
                mc.thePlayer.motionZ = 0.0;
                --this.freezeMovementTicks;
            }
            this.stopSprint3FMC();
            if (this.is3FMCRotations() && !this.is3FMCTelly()) {
                f2 = 1.0f;
            } else {
                f2 = !mc.thePlayer.onGround ? (float)((Integer)this.airMotionProp.getValue()).intValue() / 100.0f : (f = MoveUtil.getAirTicks() > 0 ? (float)((Integer)this.speedMotionProp.getValue()).intValue() / 100.0f : (float)((Integer)this.groundMotionProp.getValue()).intValue() / 100.0f);
            }
            if (f2 != 1.0f) {
                if (mc.thePlayer.movementInput.moveForward != 0.0f && mc.thePlayer.movementInput.moveStrafe != 0.0f) {
                    mc.thePlayer.movementInput.moveForward *= 1.0f / (float)Math.sqrt(2.0);
                    mc.thePlayer.movementInput.moveStrafe *= 1.0f / (float)Math.sqrt(2.0);
                }
                mc.thePlayer.movementInput.moveForward *= f2;
                mc.thePlayer.movementInput.moveStrafe *= f2;
            }
            if (this.is3FMCRotations() && !this.is3FMCTelly() ? true : !this.shouldKeepY() && (!((n = ((Integer)this.keepYProp.getValue()).intValue()) == 1 || n == 2 || n == 4) || this.keepYTicks <= 0) && (Integer)this.sprintProp.getValue() == 0) {
                mc.thePlayer.setSprinting(false);
            } else if (this.isJumping()) {
                mc.thePlayer.setSprinting(false);
            } else if (this.canFastTellyJump() || this.shouldTellyJump()) {
                mc.thePlayer.setSprinting(true);
            } else if (this.isHypixelFastTelly() && mc.thePlayer.onGround && this.keepYTicks > 0 && !(this.getYawDiffFromMove() <= 8.0f)) {
                mc.thePlayer.setSprinting(false);
            }
            if (((Boolean)this.safeProp.getValue()).booleanValue() && (Integer)this.towerProp.getValue() == 3 && mc.gameSettings.keyBindJump.isKeyDown() && !this.isTellyMode()) {
                if (Scaffold.isAngleDiagonal(Scaffold.calculateInputMoveYaw()) && !mc.thePlayer.onGround) {
                    double d2 = mc.thePlayer.motionY;
                    if (this.prevFallDistance > 0.0 && d2 <= 0.0) {
                        double d3 = Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ) * 20.0;
                        if (this.placeDelayTicks <= 0 && this.freezeMovementTicks <= 0 && d3 >= 4.67) {
                            this.placeDelayTicks = (Integer)this.safeDelayTicksProp.getValue();
                        }
                    }
                    this.prevFallDistance = d2;
                    return;
                }
                this.prevFallDistance = mc.thePlayer.motionY;
                return;
            }
            this.prevFallDistance = mc.thePlayer.motionY;
        }
    }

    @EventTarget
    public void onRender(Render2DEvent render2DEvent) {
        boolean bl = HudDragManager.isChatGuiOpen();
        if (mc.thePlayer == null) {
            this.counterInterpolator.reset(0.0f);
            return;
        }
        long l = System.currentTimeMillis();
        boolean visible = !(!this.isEnabled() && !bl || (Boolean)this.blockCounterProp.getValue() == false || mc.gameSettings.showDebugInfo && !bl);
        this.counterInterpolator.interpolateTo(visible ? 1.0f : 0.0f, visible ? 190L : 140L, l);
        float f2 = this.counterInterpolator.getValue(l);
        if (f2 <= 0.01f) {
            return;
        }
        int totalBlocks = 0;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack == null || stack.stackSize <= 0 || !(stack.getItem() instanceof ItemBlock)) continue;
            Block b = ((ItemBlock)stack.getItem()).getBlock();
            if (BlockUtil.isFullSolidBlock(b) || !BlockUtil.isReplaceable(b)) continue;
            totalBlocks += stack.stackSize;
        }
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        String label = "Blocks";
        String countStr = String.valueOf(totalBlocks);
        float f3 = RenderUtil.getStringWidth(label);
        float f4 = Fonts.get().interBold.getWidth(countStr);
        float f5 = RenderUtil.getFontHeight();
        float f6 = Fonts.get().interBold.getHeight();
        f4 = f3 + 8.0f + f4;
        float f7 = f4 + 16.0f;
        float f8 = Math.max(f5, f6) + 10.0f;
        if (((Float)this.counterXProp.getValue()).floatValue() < 0.0f || ((Float)this.counterYProp.getValue()).floatValue() < 0.0f) {
            this.counterXProp.setValue((float)scaledResolution.getScaledWidth() / 2.0f - f7 / 2.0f);
            this.counterYProp.setValue((float)scaledResolution.getScaledHeight() / 2.0f + 12.0f);
        }
        this.initFlag = true;
        float f9 = ((Float)this.counterXProp.getValue()).floatValue();
        float f10 = ((Float)this.counterYProp.getValue()).floatValue();
        this.defaultPitch = f7;
        this.defaultStrafeAngle = f8;
        GlScaleUtil.beginScale(f9, f10, f7, f8, f2);
        try {
            RenderUtil.drawHudBackground(f9, f10, f7, f8);
            float textX = f9 + (f7 - f4) / 2.0f;
            RenderUtil.drawString(label, textX, f10 + (f8 - f5) / 2.0f, RenderUtil.getDefaultTextColor(), false);
            Color color = totalBlocks > 0 ? RenderUtil.getAccentColor() : new Color(255, 95, 95);
            RenderUtil.drawString(countStr, textX + f3 + 8.0f, f10 + (f8 - f6) / 2.0f, color);
        } finally {
            GlScaleUtil.endScale();
        }
        HudDragManager.handleDragging(this);
    }

    @Override
    public final float getX() {
        return ((Float)this.counterXProp.getValue()).floatValue();
    }

    @Override
    public final float getY() {
        return ((Float)this.counterYProp.getValue()).floatValue();
    }

    @Override
    public final void setX(float f) {
        this.counterXProp.setValue(f);
        this.initFlag = true;
    }

    @Override
    public final void setY(float f) {
        this.counterYProp.setValue(f);
        this.initFlag = true;
    }

    @Override
    public final float getWidth() {
        return this.defaultPitch;
    }

    @Override
    public final float getHeight() {
        return this.defaultStrafeAngle;
    }

    @Override
    public final boolean isHudVisible() {
        return !(!this.isEnabled() && !HudDragManager.isChatGuiOpen() || (Boolean)this.blockCounterProp.getValue() == false || mc.thePlayer == null || mc.gameSettings.showDebugInfo && !HudDragManager.isChatGuiOpen());
    }

    @EventTarget
    public void onLeftClick(LeftClickMouseEvent leftClickMouseEvent) {
        if (this.isEnabled()) {
            leftClickMouseEvent.setCancelled(true);
        }
    }

    @EventTarget
    public void onRightClick(RightClickMouseEvent rightClickMouseEvent) {
        if (this.isEnabled()) {
            rightClickMouseEvent.setCancelled(true);
        }
    }

    @EventTarget
    public void onHitBlock(HitBlockEvent hitBlockEvent) {
        if (this.isEnabled()) {
            hitBlockEvent.setCancelled(true);
        }
    }

    @EventTarget
    public void onSwap(SwapItemEvent swapItemEvent) {
        if (this.isEnabled()) {
            this.blockSlot = swapItemEvent.setSlot(this.blockSlot);
            swapItemEvent.setCancelled(true);
        }
    }

    public void onEnabled() {
        this.blockSlot = mc.thePlayer != null ? mc.thePlayer.inventory.currentItem : -1;
        this.blocksPlaced = -1;
        this.tellyStartTicks = 3;
        this.targetYaw = -180.0f;
        this.targetPitch = 0.0f;
        this.hasRotations = false;
        this.strafeStage = 0;
        this.groundDelayTicks = 0;
        this.tellyPlacing = false;
        this.freezeMovementTicks = 0;
        this.placeDelayTicks = 0;
        this.prevFallDistance = 0.0;
        this.motionSaved = false;
        this.eagleSneaking = false;
        this.eagleSneakTicks = 0;
        this.eagleBlocksPlaced = 0;
        this.lastEagleSneakTime = 0L;
        this.wasSneaking = false;
        this.lastPlacedYaw = Float.NaN;
        this.lastPlacedPitch = Float.NaN;
        this.airTicks3FMC = 0;
        this.groundTicks3FMC = 0;
        this.delayTicks3FMC = 0;
        this.lastPlacedPos = null;
        this.lastPlacedFacing = null;
        this.placeCooldownTicks = 0;
        this.lastPlacementTime = 0L;
        this.speedPotionTicks = 0;
        this.resetRotationLock();
        this.targetLookYaw = Float.NaN;
        this.targetLookPitch = Float.NaN;
        this.tellyFalling = false;
        this.facingTicks = 0;
        this.hypixelTellyJumping = false;
        this.jumpKeyHandled = false;
        this.canPlaceOnGround = mc.thePlayer == null || mc.thePlayer.onGround;
        this.lastRotationStep = Float.NaN;
        this.strafeFlag = false;
        this.lagbackDetected = false;
        this.strafeCooldownTicks = 0;
        this.jumpCooldownTicks = 0;
        this.hypixelTellyInAir = false;
        this.hypixelTellyOnGround = false;
        this.hypixelTellyTicks = 0;
        this.hypixelTellyYaw = Float.NaN;
        this.hypixelTellyStrafeYaw = Float.NaN;
        this.tellyBlockCount = 1;
        this.tellyDelayTicks = 0;
        this.tellyTargetPos = null;
        this.tellyStartPos = null;
        this.tellyActive = false;
        this.tellyWaiting = false;
        this.tellyStage = 0;
        this.tellyMotionYaw = Float.NaN;
        this.tellyJumped = false;
        this.tellyRotating = false;
        this.lastUpdateTime = System.currentTimeMillis();
        if (this.isHypixelTelly()) {
            float f;
            this.hypixelTellyFast = true;
            this.keepYTicks = 1;
            this.tellyBlockCount = 2;
            this.facingTicks = 0;
            this.targetYaw = f = this.getBasePlacementYaw();
            this.targetPitch = this.basePitch;
            this.targetLookYaw = f;
            this.targetLookPitch = this.basePitch;
            this.initRotationDelta(f, this.basePitch, f, this.basePitch);
            this.tellyFalling = false;
            return;
        }
        this.hypixelTellyFast = false;
    }

    public void onDisabled() {
        if (mc.thePlayer != null && this.blockSlot != -1) {
            mc.thePlayer.inventory.currentItem = this.blockSlot;
        }
        Valor.blinkManager.setBlinking(false, BlinkType.SCAFFOLD);
        if (this.motionSaved && mc.thePlayer != null) {
            mc.thePlayer.motionX = this.prevMotionX;
            mc.thePlayer.motionY = this.prevMotionY;
            mc.thePlayer.motionZ = this.prevMotionZ;
        }
        this.freezeMovementTicks = 0;
        this.placeDelayTicks = 0;
        this.prevFallDistance = 0.0;
        this.motionSaved = false;
        this.eagleSneaking = false;
        this.eagleSneakTicks = 0;
        this.airTicks3FMC = 0;
        this.groundTicks3FMC = 0;
        this.delayTicks3FMC = 0;
        this.lastPlacedPos = null;
        this.lastPlacedFacing = null;
        this.placeCooldownTicks = 0;
        this.lastPlacementTime = 0L;
        this.lastUpdateTime = 0L;
        this.speedPotionTicks = 0;
        this.resetRotationLock();
        this.targetLookYaw = Float.NaN;
        this.targetLookPitch = Float.NaN;
        this.tellyFalling = false;
        this.facingTicks = 0;
        this.strafeFlag = false;
        this.lagbackDetected = false;
        this.strafeCooldownTicks = 0;
        this.jumpCooldownTicks = 0;
        this.hypixelTellyInAir = false;
        this.hypixelTellyOnGround = false;
        this.hypixelTellyTicks = 0;
        this.hypixelTellyYaw = Float.NaN;
        this.hypixelTellyStrafeYaw = Float.NaN;
        this.tellyBlockCount = 1;
        this.tellyDelayTicks = 0;
        this.tellyTargetPos = null;
        this.tellyStartPos = null;
        this.tellyActive = false;
        this.tellyWaiting = false;
        this.tellyStage = 0;
        this.tellyMotionYaw = Float.NaN;
        this.tellyJumped = false;
        this.tellyRotating = false;
        this.hypixelTellyFast = false;
        this.hypixelTellyJumping = false;
        this.jumpKeyHandled = false;
    }

    public void verifyValue(String string) {
        if (this.minCpsProp.getName().equals(string)) {
            if (((Float)this.minCpsProp.getValue()).floatValue() > ((Float)this.maxCpsProp.getValue()).floatValue()) {
                this.maxCpsProp.setValue((Float)this.minCpsProp.getValue());
                return;
            }
        } else if (this.maxCpsProp.getName().equals(string) && ((Float)this.minCpsProp.getValue()).floatValue() > ((Float)this.maxCpsProp.getValue()).floatValue()) {
            this.minCpsProp.setValue((Float)this.maxCpsProp.getValue());
        }
    }

    private  double scoreCandidatePosition(BlockPos blockPos, boolean bl, double d, double d2, double d3, double d4, double d5, boolean bl2, boolean bl3, double d6, double d7, BlockPos blockPos2) {
        double d8;
        double d9;
        double d10 = blockPos2.distanceSqToCenter((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5);
        if (bl && d > 1.0E-4) {
            double d11;
            d9 = 1.0 / Math.sqrt(d);
            d8 = ((double)blockPos2.getX() + 0.5 - d2) * (d3 * d9) + ((double)blockPos2.getZ() + 0.5 - d4) * (d5 * d9);
            if (d8 > 0.0) {
                double d12 = d8;
                d10 += d12 * d12 * 12.0 + d8 * 4.0;
            }
        }
        if (bl2 || bl3) {
            EnumFacing enumFacing;
            d9 = (double)blockPos2.getX() + 0.5 - d2;
            d8 = (double)blockPos2.getZ() + 0.5 - d4;
            double d13 = d9 * d6 + d8 * d7;
            double d14 = d9 - d13 * d6;
            double d15 = d8 - d13 * d7;
            double d16 = d14;
            double d17 = d15;
            d10 += (d16 * d16 + d17 * d17) * (bl3 ? 10.0 : 4.0);
            if (bl3 && (enumFacing = Scaffold.getFacingBetween(blockPos2, blockPos)) != null && blockPos2.offset(enumFacing).equals((Object)blockPos)) {
                d10 -= 6.0;
            }
        }
        return d10;
    }

    static {
        mc = Minecraft.getMinecraft();
        BLOCK_OFFSETS = new double[]{0.03125, 0.09375, 0.15625, 0.21875, 0.28125, 0.34375, 0.40625, 0.46875, 0.53125, 0.59375, 0.65625, 0.71875, 0.78125, 0.84375, 0.90625, 0.96875};
    }
}
