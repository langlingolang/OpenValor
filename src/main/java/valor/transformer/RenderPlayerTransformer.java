package valor.transformer;

import java.util.Iterator;
import valor.agent.BaseTransformer;
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

public class RenderPlayerTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      boolean var8 = false;
      Iterator var4 = var3.methods.iterator();

      while (var4.hasNext()) {
         ASM_MethodNode var5;
         if (matchesMethod(var5 = (ASM_MethodNode)var4.next(), "renderOffsetLivingLabel", "(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDLjava/lang/String;FD)V")) {
            var8 = true;
            ASM_LabelNode var6 = new ASM_LabelNode();
            ASM_InsnList var7;
            (var7 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 1));
            var7.add(new ASM_MethodInsnNode(184, "valor/hook/RendererLivingEntityHooks", "shouldHideVanillaNameTag", "(Lnet/minecraft/entity/Entity;)Z", false));
            var7.add(new ASM_JumpInsnNode(153, var6));
            var7.add(new ASM_InsnNode(177));
            var7.add(var6);
            var5.instructions.insert(var7);
         }
      }

      if (!var8) {
         return var1;
      } else {
         ASM_ClassWriter var9 = new ASM_ClassWriter();
         var3.accept(var9);
      return var9.toByteArray();
      }
   }

   public RenderPlayerTransformer() {
      super("net.minecraft.client.renderer.entity.RenderPlayer");
   }
}
