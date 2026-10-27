package valor.asm;

public final class ASM_LdcInsnNode extends ASM_AbstractInsnNode {
   public Object cst;

   public ASM_LdcInsnNode(Object cst) {
      super(18);
      this.cst = cst;
   }
}
