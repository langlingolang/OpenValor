package valor.asm;

import org.objectweb.asm.tree.AbstractInsnNode;

public class ASM_OpaqueInsnNode extends ASM_AbstractInsnNode {
   public ASM_OpaqueInsnNode(AbstractInsnNode asmNode) {
      super(asmNode != null ? asmNode.getOpcode() : -1);
      this.asmNode = asmNode;
   }
}

