package valor.transformer;

import java.util.ArrayList;
import java.util.Iterator;
import valor.agent.BaseTransformer;
import valor.asm.ASM_AbstractInsnNode;
import valor.asm.ASM_ClassNode;
import valor.asm.ASM_ClassReader;
import valor.asm.ASM_ClassWriter;
import valor.asm.ASM_InsnList;
import valor.asm.ASM_InsnNode;
import valor.asm.ASM_JumpInsnNode;
import valor.asm.ASM_LabelNode;
import valor.asm.ASM_MethodInsnNode;
import valor.asm.ASM_MethodNode;
import valor.asm.ASM_VarInsnNode;

public class GuiIngameTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var11 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if ((var5 = (ASM_MethodNode)var4.next()).desc.equals("(Lnet/minecraft/scoreboard/ScoreObjective;Lnet/minecraft/client/gui/ScaledResolution;)V")) {
            var11 = true;
            ASM_InsnList var6 = new ASM_InsnList();
            ASM_LabelNode var7 = new ASM_LabelNode();
            var6.add(new ASM_MethodInsnNode(184, "valor/hook/GuiIngameHooks", "shouldHideScoreboard", "()Z", false));
            var6.add(new ASM_JumpInsnNode(153, var7));
            var6.add(new ASM_InsnNode(177));
            var6.add(var7);
            var5.instructions.insert(var6);
         }

         if (matchesMethod(var5, "updateTick", "()V")) {
            ASM_AbstractInsnNode[] var13;
            int var16 = (var13 = var5.instructions.toArray()).length;

            for (int var8 = 0; var8 < var16; var8++) {
               ASM_AbstractInsnNode var9;
               ASM_MethodInsnNode var10;
               if ((var9 = var13[var8]).getOpcode() == 182
                  && isSameName((var10 = (ASM_MethodInsnNode)var9).name, "getCurrentItem")
                  && var10.desc.equals("()Lnet/minecraft/item/ItemStack;")) {
                  var11 = true;
                  var10.setStatic();
                  var10.owner = "valor/hook/GuiIngameHooks";
                  var10.name = "onUpdateTick";
                  var10.desc = "(Lnet/minecraft/entity/player/InventoryPlayer;)Lnet/minecraft/item/ItemStack;";
               }
            }
         }

         if (matchesMethod(var5, "renderGameOverlay", "(F)V")) {
            var11 = true;
            ASM_InsnList var14;
            (var14 = new ASM_InsnList()).add(new ASM_VarInsnNode(23, 1));
            var14.add(new ASM_MethodInsnNode(184, "valor/hook/GuiIngameHooks", "preRenderOverlay", "(F)V", false));
            var5.instructions.insert(var14);
            ASM_AbstractInsnNode[] var17;
            int var18 = (var17 = var5.instructions.toArray()).length;

            for (int var19 = 0; var19 < var18; var19++) {
               ASM_AbstractInsnNode var20;
               if ((var20 = var17[var19]).getOpcode() == 177) {
                  (var14 = new ASM_InsnList()).add(new ASM_VarInsnNode(23, 1));
                  var14.add(new ASM_MethodInsnNode(184, "valor/hook/GuiIngameHooks", "postRenderOverlay", "(F)V", false));
                  var5.instructions.insertBefore(var20, var14);
               }
            }
         }
      }

      if (!var11) {
         return var1;
      } else {
         ASM_ClassWriter var12 = new ASM_ClassWriter();
         var3.accept(var12);
      return var12.toByteArray();
      }
   }

   public GuiIngameTransformer() {
      super("net.minecraft.client.gui.GuiIngame");
   }

   static {
      new ArrayList();
   }
}
