package valor.asm;

public final class ASM_VarInsnNode extends ASM_AbstractInsnNode {
   public int var;

   public ASM_VarInsnNode(int opcode, int var) {
      super(opcode);
      this.var = var;
   }
}
