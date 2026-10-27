package valor.transformer;

import java.util.Iterator;
import valor.agent.BaseTransformer;
import valor.asm.ASM_AbstractInsnNode;
import valor.asm.ASM_ClassNode;
import valor.asm.ASM_ClassReader;
import valor.asm.ASM_ClassWriter;
import valor.asm.ASM_InsnList;
import valor.asm.ASM_InsnNode;
import valor.asm.ASM_LdcInsnNode;
import valor.asm.ASM_MethodInsnNode;
import valor.asm.ASM_MethodNode;

public class EntityPlayerTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var12 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "onLivingUpdate", "()V")) {
            ASM_AbstractInsnNode[] var6;
            int var7 = (var6 = var5.instructions.toArray()).length;

            for (int var8 = 0; var8 < var7; var8++) {
               ASM_AbstractInsnNode var9;
               ASM_MethodInsnNode var10;
               if ((var9 = var6[var8]).getOpcode() == 182 && isSameName((var10 = (ASM_MethodInsnNode)var9).name, "updatePlayerMoveState") && var10.desc.equals("()V")) {
                  var12 = true;
                  ASM_InsnList var11;
                  (var11 = new ASM_InsnList()).add(new ASM_MethodInsnNode(184, "valor/hook/EntityPlayerSPHooks", "onMoveInput", "()V", false));
                  var5.instructions.insert(var9, var11);
               }
            }
         }

         if (matchesMethod(var5, "attackTargetEntityWithCurrentItem", "(Lnet/minecraft/entity/Entity;)V")) {
            ASM_AbstractInsnNode[] var14;
            int var15 = (var14 = var5.instructions.toArray()).length;

            for (int var16 = 0; var16 < var15; var16++) {
               ASM_AbstractInsnNode var17;
               ASM_LdcInsnNode var18;
               if ((var17 = var14[var16]).getOpcode() == 18 && (var18 = (ASM_LdcInsnNode)var17).cst instanceof Double && (Double)var18.cst == 0.6) {
                  var12 = true;
                  ASM_InsnList var20;
                  (var20 = new ASM_InsnList()).add(new ASM_MethodInsnNode(184, "valor/hook/EntityPlayerHooks", "onAttackSpeed", "(D)D", false));
                  var5.instructions.insert(var17, var20);
               }

               ASM_MethodInsnNode var19;
               if (var17.getOpcode() == 182
                  && isSameName((var19 = (ASM_MethodInsnNode)var17).name, "setSprinting")
                  && var19.owner.equals("net/minecraft/entity/player/EntityPlayer")
                  && var19.desc.equals("(Z)V")) {
                  var12 = true;
                  var19.setStatic();
                  var19.owner = "valor/hook/EntityPlayerHooks";
                  var19.name = "onSetSprinting";
                  var19.desc = "(Lnet/minecraft/entity/player/EntityPlayer;Z)Z";
                  ASM_InsnList var21;
                  (var21 = new ASM_InsnList()).add(new ASM_InsnNode(87));
                  var5.instructions.insert(var19, var21);
               }
            }
         }
      }

      if (!var12) {
         return var1;
      } else {
         ASM_ClassWriter var13 = new ASM_ClassWriter();
         var3.accept(var13);
      return var13.toByteArray();
      }
   }

   public EntityPlayerTransformer() {
      super("net.minecraft.entity.player.EntityPlayer");
   }
}
