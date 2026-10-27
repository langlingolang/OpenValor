package valor.module.render;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import valor.Valor;
import valor.core.HudThemeManager;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.PacketEvent;
import valor.events.Render2DEvent;
import valor.events.Render3DEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.module.combat.KillAura;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.setting.IntProperty;
import valor.setting.ModeProperty;
import valor.setting.PercentageProperty;
import valor.ui.impl.clickgui.material.MaterialTheme;
import valor.util.BotCheckUtil;
import valor.util.ColorUtil;
import valor.util.DraggableHudElement;
import valor.util.FloatInterpolator;
import valor.util.GlScaleUtil;
import valor.util.HudDragManager;
import valor.util.Render3DUtil;
import valor.util.RenderUtil;
import valor.util.TimerUtil;
import valor.util.render.BlurPipeline;
import valor.util.render.GreenRender;

public class TargetHUD extends Module implements DraggableHudElement {
   private static final Minecraft mc = Minecraft.getMinecraft();
   public static final DecimalFormat FORMAT_DECIMAL = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
   public static final DecimalFormat FORMAT_DIFF = new DecimalFormat("+0.0;-0.0", new DecimalFormatSymbols(Locale.US));

   private final TimerUtil attackTimeoutTimer = new TimerUtil();
   private final TimerUtil healthAnimTimer = new TimerUtil();
   private final FloatInterpolator scaleFadeInterpolator = new FloatInterpolator(0.0F);
   private EntityLivingBase attackedEntity = null;
   private EntityLivingBase targetEntity = null;
   private ResourceLocation skinTexture = null;
   private float lastHealth = 0.0F;
   private float animatedHealth = 0.0F;
   private float maxHealth = 0.0F;
   private float prevHealth = -1.0F;
   private long damageTimeMs = 0L;
   private float hudWidth = 120.0F;
   private float hudHeight = 46.0F;
   private boolean hasInitializedPos = false;

   public final ModeProperty styleProp = new ModeProperty("style", 0, new String[]{"CLASSIC", "COMPACT", "RISE"});
   public final FloatProperty posXProp = new FloatProperty("pos-x", 0.0F, 0.0F, 4000.0F);
   public final FloatProperty posYProp = new FloatProperty("pos-y", 0.0F, 0.0F, 4000.0F);
   public final FloatProperty scaleProp = new FloatProperty("scale", 1.0F, 0.5F, 1.5F);
   public final PercentageProperty backgroundProp = new PercentageProperty("background", 85);
   public final BooleanProperty headProp = new BooleanProperty("head", Boolean.TRUE);
   public final BooleanProperty indicatorProp = new BooleanProperty("indicator", Boolean.TRUE);
   public final BooleanProperty outlineProp = new BooleanProperty("outline", Boolean.FALSE);
   public final BooleanProperty animationsProp = new BooleanProperty("animations", Boolean.TRUE);
   public final BooleanProperty shadowProp = new BooleanProperty("shadow", Boolean.TRUE);
   public final BooleanProperty kaOnlyProp = new BooleanProperty("ka-only", Boolean.TRUE);
   public final BooleanProperty chatPreviewProp = new BooleanProperty("chat-preview", Boolean.TRUE);
   public final BooleanProperty attackEffectProp = new BooleanProperty("attack-effect", Boolean.FALSE);
   public final FloatProperty attackEffectSpeedProp = new FloatProperty("attack-effect-speed", 1.0F, 0.25F, 3.0F, this.attackEffectProp::getValue);
   public final FloatProperty intensityProp = new FloatProperty("intensity", 1.0F, 0.5F, 3.0F, this.attackEffectProp::getValue);
   public final IntProperty attackEffectTrailProp = new IntProperty("attack-effect-trail", 6, 2, 12, this.attackEffectProp::getValue);

   private static int shaderProgram = -1;
   private static int uniformTex = -1;
   private static int uniformRect = -1;
   private static int uniformRadius = -1;
   private static int uniformUv = -1;
   private static boolean shaderFailed = false;

   public static final String VERTEX_SHADER_SOURCE = "#version 120\nvarying vec2 vPos;\nvoid main(){\n  vPos=gl_Vertex.xy;\n  gl_Position=ftransform();\n}\n";
   public static final String FRAGMENT_SHADER_SOURCE = "#version 120\nuniform sampler2D tex;\nuniform vec4 uRect;\nuniform float uRadius;\nuniform vec4 uUv;\nvarying vec2 vPos;\nfloat sdf(vec2 p, vec2 b, float r){\n  vec2 d=abs(p)-b+vec2(r);\n  return length(max(d,0.0))+min(max(d.x,d.y),0.0)-r;\n}\nvoid main(){\n  vec2 center=(uRect.xy+uRect.zw)*0.5;\n  vec2 halfSize=(uRect.zw-uRect.xy)*0.5;\n  float dist=sdf(vPos-center, halfSize, uRadius);\n  float a=1.0-smoothstep(-0.75,0.75,dist);\n  if(a<0.01) discard;\n  vec2 t=(vPos-uRect.xy)/(uRect.zw-uRect.xy);\n  t=clamp(t,0.0,1.0);\n  vec2 uv=mix(uUv.xy,uUv.zw,t);\n  vec4 c=texture2D(tex,uv);\n  gl_FragColor=vec4(c.rgb,c.a*a);\n}\n";

   @EventTarget
   public void onPacket(PacketEvent var1) {
      if (var1.getType() == EventType.SEND && var1.getPacket() instanceof C02PacketUseEntity) {
         C02PacketUseEntity var2;
         if ((var2 = (C02PacketUseEntity)var1.getPacket()).getAction() != Action.ATTACK) {
            return;
         }

         Entity var3;
         if ((var3 = var2.getEntityFromWorld(mc.theWorld)) instanceof EntityLivingBase) {
            if (var3 instanceof EntityArmorStand) {
               return;
            }

            this.attackTimeoutTimer.reset();
            this.attackedEntity = (EntityLivingBase)var3;
         }
      }
   }

   public void renderBackground(float x, float y, float width, float height) {
      int var5 = (Integer)this.backgroundProp.getValue();
      var5 = 70 + Math.round((float)Math.max(0, Math.min(100, var5)) * 0.25F);
      boolean var10004 = (Boolean)this.shadowProp.getValue();
      this.outlineProp.getValue();
      RenderUtil.drawPanel(x, y, width, height, var10004, var5);
   }

   public EntityLivingBase getTargetEntity() {
      KillAura var1;
      if ((var1 = (KillAura)Valor.moduleManager.modules.get(KillAura.class)) != null && var1.isEnabled() && var1.canAttackWithItem() && BotCheckUtil.isEntityLoaded((Entity)var1.getCurrentTargetEntity())) {
         return var1.getCurrentTargetEntity();
      } else if (!(Boolean)this.kaOnlyProp.getValue() && !this.attackTimeoutTimer.hasTimeElapsed(1500L) && BotCheckUtil.isEntityLoaded((Entity)this.attackedEntity)) {
         return this.attackedEntity;
      } else {
         return this.chatPreviewProp.getValue() && mc.currentScreen instanceof GuiChat ? mc.thePlayer : null;
      }
   }

   @Override
   public float getWidth() {
      return this.hudWidth;
   }

   @EventTarget
   public void onRender3D(Render3DEvent var1) {
      if (this.isEnabled() && (Boolean)this.attackEffectProp.getValue() && mc.thePlayer != null && mc.theWorld != null) {
         EntityLivingBase var2;
         if ((var2 = this.getTargetEntity()) != null && var2 != mc.thePlayer && !var2.isDead && var2.deathTime <= 0) {
            float var43 = var1.getPartialTicks();
            double var4 = var2.lastTickPosX + (var2.posX - var2.lastTickPosX) * (double)var43 - mc.getRenderManager().viewerPosX;
            double var6 = var2.lastTickPosY + (var2.posY - var2.lastTickPosY) * (double)var43 - mc.getRenderManager().viewerPosY;
            double var8 = var2.lastTickPosZ + (var2.posZ - var2.lastTickPosZ) * (double)var43 - mc.getRenderManager().viewerPosZ;
            double var10 = 1800.0 / (double)Math.max(0.25F, (Float)this.attackEffectSpeedProp.getValue());
            double var12;
            boolean var44;
            double var15 = (var44 = (var12 = (double)(System.currentTimeMillis() % (long)var10) / var10) < 0.5) ? var12 * 2.0 : (1.0 - var12) * 2.0;
            double var17 = (double)var2.height + 0.12;
            double var19 = 0.03 + var15 * var17;
            int var3 = (Integer)this.attackEffectTrailProp.getValue();
            float var50 = (Float)this.intensityProp.getValue();
            double var23 = var17 / Math.max(12.0, (double)var3 * 2.2);
            double var25 = Math.max(0.34, (double)var2.width * 0.72);
            Color var45 = getTargetColor(var2);
            boolean var11 = GL11.glIsEnabled(3553);
            boolean var51 = GL11.glIsEnabled(2896);
            boolean var13 = GL11.glIsEnabled(2929);
            boolean var14 = GL11.glIsEnabled(3042);
            boolean var52 = GL11.glIsEnabled(2848);
            boolean var16 = GL11.glGetBoolean(2930);
            int var21 = GL11.glGetInteger(32969);
            int var22 = GL11.glGetInteger(32968);
            int var27 = GL11.glGetInteger(32971);
            int var28 = GL11.glGetInteger(32970);
            int var29 = GL11.glGetInteger(3154);
            float var30 = GL11.glGetFloat(2849);
            FloatBuffer var31 = BufferUtils.createFloatBuffer(4);
            GL11.glGetFloat(2816, var31);
            int var32 = GL11.glGetInteger(2976);
            GlStateManager.matrixMode(5888);
            GlStateManager.pushMatrix();

            try {
               GlStateManager.translate(var4, var6, var8);
               GlStateManager.enableTexture2D();
               GlStateManager.disableTexture2D();
               GlStateManager.enableLighting();
               GlStateManager.disableLighting();
               GlStateManager.enableDepth();
               GlStateManager.disableDepth();
               GlStateManager.depthMask(true);
               GlStateManager.depthMask(false);
               GlStateManager.disableBlend();
               GlStateManager.enableBlend();
               GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
               GL11.glEnable(2848);
               GL11.glHint(3154, 4354);

               for (int var46 = var3 - 1; var46 >= 0; var46--) {
                  double var33;
                  if (!((var33 = var19 + (var44 ? -1.0 : 1.0) * var23 * (double)var46) < 0.0) && !(var33 > var17 + 0.08)) {
                     float var5 = 1.0F - (float)var46 / (float)var3;
                     float var49 = Math.min(1.0F, (0.08F + 0.82F * var5 * var5) * var50);
                     var5 = (var46 == 0 ? 2.2F : var5 + 0.7F) * (0.6F + 0.4F * var50);
                     double var37 = var25;
                     double var35 = var33;
                     GL11.glLineWidth(var5);
                     GL11.glColor4f((float)var45.getRed() / 255.0F, (float)var45.getGreen() / 255.0F, (float)var45.getBlue() / 255.0F, var49);
                     GL11.glBegin(2);

                     for (int var48 = 0; var48 < 64; var48++) {
                        double var39;
                        GL11.glVertex3d(Math.cos(var39 = (Math.PI * 2) * (double)var48 / 64.0) * var37, var35, Math.sin(var39) * var37);
                     }

                     GL11.glEnd();
                  }
               }
            } finally {
               GlStateManager.popMatrix();
               GlStateManager.matrixMode(var32);
               GL11.glLineWidth(var30);
               GL11.glHint(3154, var29);
               if (var52) {
                  GL11.glEnable(2848);
               } else {
                  GL11.glDisable(2848);
               }

               GlStateManager.tryBlendFuncSeparate(var21, var22, var27, var28);
               restoreGlStates(var11, var51, var13, var14, var16);
               GlStateManager.resetColor();
               GlStateManager.color(var31.get(0), var31.get(1), var31.get(2), var31.get(3));
            }
         }
      }
   }

   @Override
   public float getX() {
      return (Float)this.posXProp.getValue();
   }

   @Override
   public void setX(float x) {
      this.posXProp.setValue(x);
      this.hasInitializedPos = true;
   }

   public static void restoreGlStates(boolean tex, boolean light, boolean depth, boolean blend, boolean depthMask) {
      if (tex) {
         GlStateManager.disableTexture2D();
         GlStateManager.enableTexture2D();
      } else {
         GlStateManager.enableTexture2D();
         GlStateManager.disableTexture2D();
      }

      if (light) {
         GlStateManager.disableLighting();
         GlStateManager.enableLighting();
      } else {
         GlStateManager.enableLighting();
         GlStateManager.disableLighting();
      }

      if (depth) {
         GlStateManager.disableDepth();
         GlStateManager.enableDepth();
      } else {
         GlStateManager.enableDepth();
         GlStateManager.disableDepth();
      }

      if (blend) {
         GlStateManager.disableBlend();
         GlStateManager.enableBlend();
      } else {
         GlStateManager.enableBlend();
         GlStateManager.disableBlend();
      }

      GlStateManager.depthMask(!depthMask);
      GlStateManager.depthMask(depthMask);
   }

   public static void renderCustomTextureRect(float x, float y, float size, float radius, float uOffset) {
      if (!initShader()) {
         Gui.drawScaledCustomSizeModalRect(
            Math.round(x), Math.round(y), uOffset, 8.0F, 8, 8, Math.max(1, Math.round(size)), Math.max(1, Math.round(size)), 64.0F, 64.0F
         );
      } else {
         float var5 = uOffset / 64.0F;
         uOffset = (uOffset + 8.0F) / 64.0F;
         GL20.glUseProgram(shaderProgram);
         GL20.glUniform1i(uniformTex, 0);
         GL20.glUniform4f(uniformRect, x, y, x + size, y + size);
         GL20.glUniform1f(uniformRadius, radius);
         GL20.glUniform4f(uniformUv, var5, 0.125F, uOffset, 0.25F);
         Tessellator var6;
         WorldRenderer var8;
         (var8 = (var6 = Tessellator.getInstance()).getWorldRenderer()).begin(7, DefaultVertexFormats.POSITION);
         var8.pos((double)x, (double)(y + size), 0.0).endVertex();
         var8.pos((double)(x + size), (double)(y + size), 0.0).endVertex();
         var8.pos((double)(x + size), (double)y, 0.0).endVertex();
         var8.pos((double)x, (double)y, 0.0).endVertex();
         var6.draw();
         GL20.glUseProgram(0);
      }
   }

   public void renderHead(float x, float y, float size) {
      if ((Boolean)this.headProp.getValue() && !(size <= 0.0F)) {
         float var4 = Math.max(0.0F, 1.0F - (float)(System.currentTimeMillis() - this.damageTimeMs) / 260.0F);
         float var5 = 1.0F - 0.11F * var4;
         GlStateManager.pushMatrix();
         GlStateManager.translate(x + size / 2.0F, y + size / 2.0F, 0.0F);
         GlStateManager.scale(var5, var5, 1.0F);
         GlStateManager.translate(-x - size / 2.0F, -y - size / 2.0F, 0.0F);
         var5 = Math.min(4.0F, size * 0.22F);
         GreenRender.fillRR(x + 1.0F, y + 1.5F, size, size, var5, new Color(0, 0, 0, 55));
         GreenRender.fillRR(x, y, size, size, var5, MaterialTheme.SETTINGS_BG);
         Render3DUtil.setupRender2D();
         if (this.skinTexture == null) {
            GlStateManager.popMatrix();
         } else {
            GL20.glUseProgram(0);
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(this.skinTexture);
            renderCustomTextureRect(x, y, size, var5, 8.0F);
            renderCustomTextureRect(x, y, size, var5, 40.0F);
            if (var4 > 0.0F) {
               GreenRender.fillRR(x, y, size, size, var5, new Color(255, 65, 82, Math.round(105.0F * var4)));
            }

            Render3DUtil.setupRender2D();
            GlStateManager.popMatrix();
         }
      }
   }

   public static Color getTargetColor(EntityLivingBase entity) {
      if (entity instanceof EntityPlayer) {
         if (BotCheckUtil.isFriend((EntityPlayer)entity)) {
            return Valor.friendManager.color;
         }

         if (BotCheckUtil.isTarget((EntityPlayer)entity)) {
            return Valor.targetManager.color;
         }
      }

      HudThemeManager var1;
      return (var1 = HudThemeManager.getInstance()) != null ? var1.getColor(0, 2, System.currentTimeMillis()) : RenderUtil.getThemeColor();
   }

   @Override
   public void setY(float y) {
      this.posYProp.setValue(y);
      this.hasInitializedPos = true;
   }

   @Override
   public float getY() {
      return (Float)this.posYProp.getValue();
   }

   @Override
   public float getHeight() {
      return this.hudHeight;
   }

   @EventTarget
   public void onRender(Render2DEvent var1) {
      if (mc.thePlayer != null && mc.theWorld != null) {
         ScaledResolution var24 = new ScaledResolution(mc);
         if (!this.hasInitializedPos) {
            if ((Float)this.posXProp.getValue() == 0.0F && (Float)this.posYProp.getValue() == 0.0F) {
               this.posXProp.setValue((float)var24.getScaledWidth() / 2.0F - 60.0F);
               this.posYProp.setValue((float)var24.getScaledHeight() / 2.0F + 40.0F);
            }

            this.hasInitializedPos = true;
         }

         EntityLivingBase var25 = this.targetEntity;
         Object var2;
         if ((var2 = this.isEnabled() ? this.getTargetEntity() : null) == null && this.isEnabled() && HudDragManager.isChatGuiOpen() && (Boolean)this.chatPreviewProp.getValue()) {
            var2 = mc.thePlayer;
         }

         long var5 = System.currentTimeMillis();
         if ((Boolean)this.animationsProp.getValue()) {
            this.scaleFadeInterpolator.interpolateTo(var2 == null ? 0.0F : 1.0F, var2 == null ? 140L : 200L, var5);
         } else {
            this.scaleFadeInterpolator.reset(var2 == null ? 0.0F : 1.0F);
         }

         float var3 = this.scaleFadeInterpolator.getValue(var5);
         if (var2 != null) {
            this.targetEntity = (EntityLivingBase)var2;
         }

         if (this.targetEntity != null && !(var3 <= 0.01F)) {
            float var30 = (mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount()) / 2.0F;
            float var4 = this.targetEntity.getAbsorptionAmount() / 2.0F;
            var4 = this.targetEntity.getHealth() / 2.0F + var4;
            if (this.targetEntity != var25) {
               this.prevHealth = var4;
               this.damageTimeMs = 0L;
            } else if (this.prevHealth >= 0.0F && var4 < this.prevHealth - 0.01F) {
               this.damageTimeMs = var5;
            }

            this.prevHealth = var4;
            if (this.targetEntity != var25) {
               this.skinTexture = null;
               this.healthAnimTimer.lastTimeMs = 0L;
               this.lastHealth = var4;
               this.animatedHealth = var4;
            }

            if (!(Boolean)this.animationsProp.getValue() || this.healthAnimTimer.hasTimeElapsed(150L)) {
               this.lastHealth = this.animatedHealth;
               this.animatedHealth = var4;
               this.maxHealth = this.targetEntity.getMaxHealth() / 2.0F;
               if (this.lastHealth != this.animatedHealth) {
                  this.healthAnimTimer.reset();
               }
            }

            EntityLivingBase var26 = this.targetEntity;
            ResourceLocation var10001 = new ResourceLocation("textures/entity/steve.png");
            if (this.targetEntity instanceof AbstractClientPlayer) {
               var10001 = ((AbstractClientPlayer)var26).getLocationSkin();
            } else if (var26 instanceof EntityPlayer && mc.getNetHandler() != null) {
               EntityPlayer var43 = (EntityPlayer)var26;
               NetworkPlayerInfo var6 = mc.getNetHandler().getPlayerInfo(var43.getUniqueID());
               if (var6 == null) {
                  var6 = mc.getNetHandler().getPlayerInfo(var43.getName());
               }
               if (var6 != null) {
                  var10001 = var6.getLocationSkin();
               }
            }

            this.skinTexture = var10001;
            float var27 = (float)Math.min(Math.max(this.healthAnimTimer.getElapsedTimeMs(), 0L), 150L);
            float var28 = Math.min(Math.max(Render3DUtil.lerpFloat(this.animatedHealth, this.lastHealth, var27 / 150.0F) / Math.max(0.1F, this.maxHealth), 0.0F), 1.0F);
            Color var44;
            Color var48 = var44 = getTargetColor(this.targetEntity);
            Color var51 = ColorUtil.getHealthColor(Math.min(Math.max((var30 - var4 + 1.0F) / 2.0F, 0.0F), 1.0F));
            float var8 = (Float)this.scaleProp.getValue();
            float var9 = (Float)this.posXProp.getValue();
            float var10 = (Float)this.posYProp.getValue();
            BlurPipeline.prepare();
            GlScaleUtil.beginScale(var9, var10, this.hudWidth, this.hudHeight, var3);

            try {
               switch (this.styleProp.getValue()) {
                  case 1:
                     String var54 = BotCheckUtil.getFormattedCleanName(this.targetEntity);
                     String var56 = String.valueOf(
                        (int)Math.ceil((double)(this.targetEntity.getHealth() + this.targetEntity.getAbsorptionAmount()))
                     );
                     var10 = mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount();
                     float var61 = this.targetEntity.getHealth() + this.targetEntity.getAbsorptionAmount();
                     String var60;
                     if (Math.abs(var10 - var61) < 0.25F) {
                        var60 = "D";
                     } else if (var10 > var61) {
                        var60 = "W";
                     } else {
                        var60 = "L";
                     }

                     float var64 = this.headProp.getValue() ? 18.0F : 0.0F;
                     var4 = RenderUtil.getStringWidth(var54);
                     float var65 = RenderUtil.getStringWidth(var56);
                     float var66 = this.indicatorProp.getValue() ? RenderUtil.getStringWidth(var60) : 0.0F;
                     float var67 = Math.max(
                        100.0F, var64 + 8.0F + (var64 > 0.0F ? 6.0F : 0.0F) + var4 + 10.0F + var65 + (var66 > 0.0F ? var66 + 8.0F : 0.0F) + 8.0F
                     );
                     this.hudWidth = var67 * var8;
                     this.hudHeight = 34.0F * var8;
                     Color var68 = RenderUtil.getAccentColor();
                     this.renderBackground(var9, var10, var67 * var8, 34.0F * var8);
                     var4 = 8.0F;
                     if (var64 > 0.0F) {
                        var4 = var64 + 8.0F + 6.0F;
                     }

                     RenderUtil.drawProgressBar(var9 + var4 * var8, var10 + 21.0F * var8, (var67 - var4 - 8.0F) * var8, 5.0F * var8, var28, var68);
                     GlStateManager.pushMatrix();
                     GlStateManager.translate(var9, var10, 0.0F);
                     GlStateManager.scale(var8, var8, 1.0F);
                     if (var64 > 0.0F) {
                        this.renderHead(8.0F, 8.0F, var64);
                     }

                     RenderUtil.drawString(var54, var4, 8.0F, RenderUtil.getLightTextColor(), (Boolean)this.shadowProp.getValue());
                     float var34 = var67 - 8.0F;
                     if ((Boolean)this.indicatorProp.getValue()) {
                        RenderUtil.drawString(var60, var34 - var66, 8.0F, var51, (Boolean)this.shadowProp.getValue());
                        var34 -= var66 + 8.0F;
                     }

                     RenderUtil.drawString(var56, var34 - var65, 8.0F, var68, (Boolean)this.shadowProp.getValue());
                     GlStateManager.popMatrix();
                     break;
                  case 2:
                     String var40 = BotCheckUtil.getFormattedCleanName(this.targetEntity);
                     float var55 = this.headProp.getValue() ? 28.0F : 0.0F;
                     float var57 = RenderUtil.getStringWidth(var40);
                     float var58 = Math.max(120.0F, var55 + 8.0F + (var55 > 0.0F ? 8.0F : 0.0F) + var57 + 8.0F);
                     this.hudWidth = var58 * var8;
                     this.hudHeight = 40.0F * var8;
                     Color var62 = var44 != null ? var44 : RenderUtil.getAccentColor();
                     this.renderBackground(var9, var10, var58 * var8, 40.0F * var8);
                     float var63 = 8.0F;
                     if (var55 > 0.0F) {
                        var63 = 44.0F;
                     }

                     RenderUtil.drawProgressBar(
                        var9 + var63 * var8, var10 + (8.0F + RenderUtil.getFontHeight() + 8.0F) * var8, (var58 - var63 - 8.0F) * var8, 5.0F * var8, var28, var62
                     );
                     GlStateManager.pushMatrix();
                     GlStateManager.translate(var9, var10, 0.0F);
                     GlStateManager.scale(var8, var8, 1.0F);
                     if (var55 > 0.0F) {
                        this.renderHead(8.0F, 8.0F, 28.0F);
                     }

                     RenderUtil.drawString(var40, var63, 9.0F, RenderUtil.getLightTextColor(), (Boolean)this.shadowProp.getValue());
                     GlStateManager.popMatrix();
                     break;
                  default:
                     float var49 = var8;
                     float var45 = var10;
                     float var31 = var9;
                     TargetHUD var29 = this;
                     String var11 = BotCheckUtil.getFormattedCleanName(this.targetEntity);
                     String var12 = FORMAT_DECIMAL.format((double)var4) + " HP";
                     String var13 = Math.abs(var4 - var30) < 0.25F
                        ? "Even"
                        : (var4 < var30 ? "Ahead " : "Behind ") + FORMAT_DECIMAL.format((double)Math.abs(var30 - var4));
                     float var14 = this.headProp.getValue() ? 30.0F : 0.0F;
                     float var15 = 10.0F + (var14 > 0.0F ? var14 + 10.0F : 0.0F);
                     float var16 = this.indicatorProp.getValue() ? RenderUtil.getStringWidth(var13) + 12.0F : 0.0F;
                     var3 = RenderUtil.getBoldStringWidth(var11);
                     float var17 = Math.max(148.0F, var15 + Math.max(var3, RenderUtil.getStringWidth(var12) + var16) + 10.0F);
                     this.hudWidth = var17 * var8;
                     this.hudHeight = 54.0F * var8;
                     this.renderBackground(var9, var10, var17 * var8, 54.0F * var8);
                     RenderUtil.drawProgressBar(var9 + var15 * var8, var10 + 42.0F * var8, (var17 - var15 - 10.0F) * var8, 3.0F * var8, var28, var48);
                     GlStateManager.pushMatrix();

                     try {
                        GlStateManager.translate(var31, var45, 0.0F);
                        GlStateManager.scale(var49, var49, 1.0F);
                        if (var14 > 0.0F) {
                           var29.renderHead(10.0F, (54.0F - var14) / 2.0F, var14);
                        }

                        RenderUtil.drawBoldString(var11, var15, 10.0F, RenderUtil.getLightTextColor());
                        RenderUtil.drawString(var12, var15, 26.0F, RenderUtil.getLightTextColor(), false);
                        if ((Boolean)var29.indicatorProp.getValue()) {
                           RenderUtil.drawString(var13, var17 - 10.0F - RenderUtil.getStringWidth(var13), 26.0F, RenderUtil.getSecondaryTextColor(), false);
                        }
                     } finally {
                        GlStateManager.popMatrix();
                     }
               }
            } finally {
               GlScaleUtil.endScale();
            }

            HudDragManager.handleDragging(this);
         } else {
            this.targetEntity = null;
               HudDragManager.handleDragging(this);
         }
      } else {
         this.scaleFadeInterpolator.reset(0.0F);
         this.targetEntity = null;
      }
   }

   @Override
   public boolean isHudVisible() {
      return this.isEnabled();
   }

   public static boolean initShader() {
      if (shaderProgram > 0) {
         return true;
      } else if (shaderFailed) {
         return false;
      } else {
         try {
            int var0;
            GL20.glShaderSource(var0 = GL20.glCreateShader(35633), VERTEX_SHADER_SOURCE);
            GL20.glCompileShader(var0);
            if (GL20.glGetShaderi(var0, 35713) == 0) {
               shaderFailed = true;
               return false;
            } else {
               int var1;
               GL20.glShaderSource(var1 = GL20.glCreateShader(35632), FRAGMENT_SHADER_SOURCE);
               GL20.glCompileShader(var1);
               if (GL20.glGetShaderi(var1, 35713) == 0) {
                  shaderFailed = true;

                  return false;
               } else {
                  GL20.glAttachShader(shaderProgram = GL20.glCreateProgram(), var0);
                  GL20.glAttachShader(shaderProgram, var1);
                  GL20.glLinkProgram(shaderProgram);
                  GL20.glDeleteShader(var0);
                  GL20.glDeleteShader(var1);
                  if (GL20.glGetProgrami(shaderProgram, 35714) == 0) {
                     shaderProgram = -1;
                              shaderFailed = true;

                     return false;
                  } else {
                     uniformTex = GL20.glGetUniformLocation(shaderProgram, "tex");
                     uniformRect = GL20.glGetUniformLocation(shaderProgram, "uRect");
                     uniformRadius = GL20.glGetUniformLocation(shaderProgram, "uRadius");
                     uniformUv = GL20.glGetUniformLocation(shaderProgram, "uUv");
                     return true;
                  }
               }
            }
         } catch (Throwable var2) {
            shaderFailed = true;

            shaderProgram = -1;
            return false;
         }
      }
   }

   public TargetHUD() {
      super("TargetHUD", false, false, ModuleCategory.VISUAL);
   }
}
