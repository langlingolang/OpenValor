package valor.transformer;

import java.util.Iterator;
import valor.agent.BaseTransformer;
import valor.asm.ASM_AbstractInsnNode;
import valor.asm.ASM_ClassNode;
import valor.asm.ASM_ClassReader;
import valor.asm.ASM_ClassWriter;
import valor.asm.ASM_MethodInsnNode;
import valor.asm.ASM_MethodNode;

public class WorldTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var10 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "handleMaterialAcceleration")) {
            ASM_AbstractInsnNode[] var6;
            int var7 = (var6 = var5.instructions.toArray()).length;

            for (int var8 = 0; var8 < var7; var8++) {
               ASM_AbstractInsnNode var9;
               ASM_MethodInsnNode var15;
               if ((var9 = var6[var8]).getOpcode() == 182 && isSameName((var15 = (ASM_MethodInsnNode)var9).name, "isPushedByWater") && var15.desc.equals("()Z")) {
                  var10 = true;
                  var15.setStatic();
                  var15.owner = "valor/hook/WorldHooks";
                  var15.name = "onIsPushedByWater";
                  var15.desc = "(Lnet/minecraft/entity/Entity;)Z";
               }
            }
         }

         if (matchesMethod(var5, "rayTraceBlocks")) {
            ASM_AbstractInsnNode[] var12;
            int var13 = (var12 = var5.instructions.toArray()).length;

            for (int var14 = 0; var14 < var13; var14++) {
               ASM_AbstractInsnNode var16;
               ASM_MethodInsnNode var17;
               if ((var16 = var12[var14]).getOpcode() == 182
                  && isSameName((var17 = (ASM_MethodInsnNode)var16).name, "getBlockState")
                  && var17.desc.equals("(Lnet/minecraft/util/BlockPos;)Lnet/minecraft/block/state/IBlockState;")) {
                  var10 = true;
                  var17.setStatic();
                  var17.owner = "valor/hook/WorldHooks";
                  var17.name = "onGetBlockStateForRaytrace";
                  var17.desc = "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;)Lnet/minecraft/block/state/IBlockState;";
               }
            }
         }
      }

      if (!var10) {
         return var1;
      } else {
         ASM_ClassWriter var11 = new ASM_ClassWriter();
         var3.accept(var11);
      return var11.toByteArray();
      }
   }

   public WorldTransformer() {
      super("net.minecraft.world.World");
   }
}
