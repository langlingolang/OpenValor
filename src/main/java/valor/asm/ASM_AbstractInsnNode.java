package valor.asm;

import org.objectweb.asm.tree.AbstractInsnNode;

public abstract class ASM_AbstractInsnNode {
   public int opcode;
   public ASM_AbstractInsnNode prev;
   public ASM_AbstractInsnNode next;
   public AbstractInsnNode asmNode;

   public final int getOpcode() {
      return this.opcode;
   }

   protected ASM_AbstractInsnNode(int opcode) {
      this.opcode = opcode;
   }
}
