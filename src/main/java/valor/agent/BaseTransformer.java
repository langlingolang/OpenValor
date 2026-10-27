package valor.agent;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import valor.asm.ASM_ClassNode;
import valor.asm.ASM_ClassWriter;
import valor.asm.ASM_MethodNode;
import valor.asm.SafeClassWriter;

public abstract class BaseTransformer implements ClassFileTransformer {
   private static void logToFile(String msg) {
      System.out.println("[Valor] " + msg);
      try {
         String path = System.getProperty("user.home") + "\\AppData\\Roaming\\.minecraft\\valor_runtime.log";
         java.io.FileWriter writer = new java.io.FileWriter(path, true);
         writer.write("[Valor] " + msg + "\n");
         writer.flush();
         writer.close();
      } catch (Exception ignored) {
      }
   }

   private final String targetClassName;
   private final String targetClassInternal;
   private static final Map<String, String> SEARGE_METHODS = new HashMap<String, String>();

   public BaseTransformer(String targetClassName) {
      this.targetClassName = targetClassName;
      this.targetClassInternal = targetClassName != null ? targetClassName.replace('.', '/') : null;
   }

   protected abstract byte[] doTransform(byte[] classfileBuffer);

   @Override
   public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
      if (className != null && className.equals(this.targetClassInternal)) {
         try {
            SafeClassWriter.CURRENT_TRANSFORMER_LOADER = loader;
            if (!ValorAgent.isInjected(loader)) {
               ValorAgent.ensureInjected(loader);
            }
            if (loader != null && NativeBridge.getGenesisClassLoader() == null) {
               NativeBridge.setGenesisClassLoader(loader);
               try {
                  NativeBridge.startValor();
               } catch (Throwable t) {
                  System.err.println("[Valor] startValor on transform error: " + t.getMessage());
               }
            }

            byte[] transformed = this.doTransform(classfileBuffer);
            if (transformed != null && transformed != classfileBuffer) {
               logToFile("Successfully transformed " + this.targetClassName + " (" + classfileBuffer.length + " -> " + transformed.length + " bytes)");
            }
            return transformed;
         } catch (Throwable t) {
            System.err.println("[Valor] Transform failed for " + this.targetClassName + ": " + t.getMessage());
            t.printStackTrace();
            return classfileBuffer;
         }
      } else {
         return null;
      }
   }

   public String getTargetClassInternal() {
      return this.targetClassInternal;
   }

   protected static boolean matchesMethod(ASM_MethodNode node, String name) {
      if (node == null || name == null) return false;
      if (node.name.equals(name)) return true;
      String srg = SEARGE_METHODS.get(name);
      return srg != null && node.name.equals(srg);
   }

   protected static boolean matchesMethod(ASM_MethodNode node, String name, String desc) {
      if (!matchesMethod(node, name)) return false;
      return desc == null || desc.equals(node.desc);
   }

   protected static boolean isSameName(String name1, String name2) {
      if (name1 == null || name2 == null) return false;
      if (name1.equals(name2)) return true;
      String srg = SEARGE_METHODS.get(name2);
      if (srg != null && name1.equals(srg)) return true;
      String srgRev = SEARGE_METHODS.get(name1);
      return srgRev != null && name2.equals(srgRev);
   }

   protected static byte[] writeClass(ASM_ClassNode classNode, byte[] originalBytes) {
      try {
         ASM_ClassWriter writer = new ASM_ClassWriter();
         classNode.accept(writer);
         return writer.toByteArray();
      } catch (Throwable t) {
         return originalBytes;
      }
   }

   static {
      SEARGE_METHODS.put("setOpaqueCube", "func_178606_a");
      SEARGE_METHODS.put("onUpdate", "func_70071_h_");
      SEARGE_METHODS.put("onUpdateWalkingPlayer", "func_175161_p");
      SEARGE_METHODS.put("onLivingUpdate", "func_70636_d");
      SEARGE_METHODS.put("isUsingItem", "func_71039_bw");
      SEARGE_METHODS.put("isPotionActive", "func_70644_a");
      SEARGE_METHODS.put("isRiding", "func_70115_ae");
      SEARGE_METHODS.put("updatePlayerMoveState", "func_78898_a");
      SEARGE_METHODS.put("updateCameraAndRender", "func_181560_a");
      SEARGE_METHODS.put("updateRenderer", "func_78464_a");
      SEARGE_METHODS.put("renderWorldPass", "func_175068_a");
      SEARGE_METHODS.put("hurtCameraEffect", "func_78482_e");
      SEARGE_METHODS.put("getMouseOver", "func_78473_a");
      SEARGE_METHODS.put("orientCamera", "func_78467_g");
      SEARGE_METHODS.put("updateFogColor", "func_78466_h");
      SEARGE_METHODS.put("setupFog", "func_78468_a");
      SEARGE_METHODS.put("setupCameraTransform", "func_78479_a");
      SEARGE_METHODS.put("distanceTo", "func_72438_d");
      SEARGE_METHODS.put("setVelocity", "func_70016_h");
      SEARGE_METHODS.put("setAngles", "func_70082_c");
      SEARGE_METHODS.put("moveEntity", "func_70091_d");
      SEARGE_METHODS.put("jump", "func_70664_aZ");
      SEARGE_METHODS.put("moveEntityWithHeading", "func_70612_e");
      SEARGE_METHODS.put("moveFlying", "func_70060_a");
      SEARGE_METHODS.put("attackTargetEntityWithCurrentItem", "func_71059_n");
      SEARGE_METHODS.put("setSprinting", "func_70031_b");
      SEARGE_METHODS.put("handleMaterialAcceleration", "func_72918_a");
      SEARGE_METHODS.put("isPushedByWater", "func_96092_aw");
      SEARGE_METHODS.put("rayTraceBlocks", "func_147447_a");
      SEARGE_METHODS.put("getBlockState", "func_180495_p");
      SEARGE_METHODS.put("attackEntity", "func_78764_a");
      SEARGE_METHODS.put("syncCurrentPlayItem", "func_78750_j");
      SEARGE_METHODS.put("windowClick", "func_78753_a");
      SEARGE_METHODS.put("onStoppedUsingItem", "func_78766_c");
      SEARGE_METHODS.put("sendPacket", "func_179290_a");
      SEARGE_METHODS.put("writePacketData", "func_148840_b");
      SEARGE_METHODS.put("isPressed", "func_151468_f");
      SEARGE_METHODS.put("renderString", "func_180455_b");
      SEARGE_METHODS.put("getStringWidth", "func_78256_a");
      SEARGE_METHODS.put("hasEffect", "func_77962_s");
      SEARGE_METHODS.put("shouldSideBeRendered", "func_176225_a");
      SEARGE_METHODS.put("getBlockLayer", "func_180664_k");
      SEARGE_METHODS.put("renderModel", "func_178259_a");
      SEARGE_METHODS.put("renderBlock", "func_175018_a");
      SEARGE_METHODS.put("getFovModifier", "func_175156_o");
      SEARGE_METHODS.put("getAttributeValue", "func_111126_e");
      SEARGE_METHODS.put("renderEntityStatic", "func_147936_a");
      SEARGE_METHODS.put("cacheActiveRenderInfo", "func_180597_a");
      SEARGE_METHODS.put("putColorMultiplier", "func_178978_a");
      SEARGE_METHODS.put("computeVisibility", "func_178607_a");
      SEARGE_METHODS.put("renderEntities", "func_180446_a");
      SEARGE_METHODS.put("startGame", "func_71384_a");
      SEARGE_METHODS.put("runTick", "func_71407_l");
      SEARGE_METHODS.put("runGameLoop", "func_71411_J");
      SEARGE_METHODS.put("loadWorld", "func_71353_a");
      SEARGE_METHODS.put("updateFramebufferSize", "func_147119_ah");
      SEARGE_METHODS.put("clickMouse", "func_147116_af");
      SEARGE_METHODS.put("rightClickMouse", "func_147121_ag");
      SEARGE_METHODS.put("sendClickBlockToController", "func_147115_a");
      SEARGE_METHODS.put("setKeyBindState", "func_74510_a");
      SEARGE_METHODS.put("changeCurrentItem", "func_70453_c");
      SEARGE_METHODS.put("updateTick", "func_73831_a");
      SEARGE_METHODS.put("renderGameOverlay", "func_175180_a");
      SEARGE_METHODS.put("renderScoreboard", "func_180475_a");
      SEARGE_METHODS.put("renderLivingLabel", "func_147906_a");
      SEARGE_METHODS.put("renderOffsetLivingLabel", "func_177069_a");
      SEARGE_METHODS.put("getCurrentItem", "func_70448_g");
   }
}
