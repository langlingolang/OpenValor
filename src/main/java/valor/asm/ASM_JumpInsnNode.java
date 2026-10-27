package valor.asm;

public final class ASM_JumpInsnNode extends ASM_AbstractInsnNode {
   public ASM_LabelNode label;

   public ASM_JumpInsnNode(int opcode, ASM_LabelNode label) {
      super(opcode);
      this.label = label;
   }
}
