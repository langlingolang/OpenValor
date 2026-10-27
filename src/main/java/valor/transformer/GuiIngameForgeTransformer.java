package valor.transformer;

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

public class GuiIngameForgeTransformer extends BaseTransformer {
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

         if (matchesMethod(var5, "renderGameOverlay", "(F)V")) {
            var11 = true;
            ASM_InsnList var13;
            (var13 = new ASM_InsnList()).add(new ASM_MethodInsnNode(184, "valor/hook/GuiIngameHooks", "configureForgeScoreboard", "()V", false));
            var13.add(new ASM_VarInsnNode(23, 1));
            var13.add(new ASM_MethodInsnNode(184, "valor/hook/GuiIngameHooks", "preRenderOverlay", "(F)V", false));
            var5.instructions.insert(var13);
            ASM_AbstractInsnNode[] var15;
            int var14 = (var15 = var5.instructions.toArray()).length;

            for (int var8 = 0; var8 < var14; var8++) {
               ASM_AbstractInsnNode var9;
               if ((var9 = var15[var8]).getOpcode() == 177) {
                  ASM_InsnList var10;
                  (var10 = new ASM_InsnList()).add(new ASM_VarInsnNode(23, 1));
                  var10.add(new ASM_MethodInsnNode(184, "valor/hook/GuiIngameHooks", "postRenderOverlay", "(F)V", false));
                  var5.instructions.insertBefore(var9, var10);
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

   public GuiIngameForgeTransformer() {
      super("net.minecraftforge.client.GuiIngameForge");
   }
}
