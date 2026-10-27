package valor.asm;

import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ASM_MethodNode {
   public int maxStack;
   public String name;
   public String desc;
   public ASM_InsnList instructions = new ASM_InsnList();
   public MethodNode originalNode;

   public void syncToOriginal() {
      if (this.originalNode == null) return;
      this.originalNode.instructions.clear();

      Map<ASM_LabelNode, LabelNode> labelMap = new HashMap<ASM_LabelNode, LabelNode>();
      ASM_AbstractInsnNode cur = this.instructions.getFirst();
      while (cur != null) {
         if (cur instanceof ASM_LabelNode) {
            ASM_LabelNode al = (ASM_LabelNode) cur;
            if (al.asmNode instanceof LabelNode) {
               labelMap.put(al, (LabelNode) al.asmNode);
            } else {
               LabelNode newLbl = new LabelNode();
               al.asmNode = newLbl;
               labelMap.put(al, newLbl);
            }
         }
         cur = cur.next;
      }

      cur = this.instructions.getFirst();
      while (cur != null) {
         AbstractInsnNode real = convertToReal(cur, labelMap);
         if (real != null) {
            this.originalNode.instructions.add(real);
         }
         cur = cur.next;
      }
   }

   private static AbstractInsnNode convertToReal(ASM_AbstractInsnNode ainsn, Map<ASM_LabelNode, LabelNode> labelMap) {
      if (ainsn instanceof ASM_LabelNode) {
         return labelMap.get(ainsn);
      }
      if (ainsn instanceof ASM_JumpInsnNode) {
         ASM_JumpInsnNode j = (ASM_JumpInsnNode) ainsn;
         LabelNode target = labelMap.get(j.label);
         if (target == null) {
            target = new LabelNode();
            if (j.label != null) {
               labelMap.put(j.label, target);
               j.label.asmNode = target;
            }
         }
         return new JumpInsnNode(j.getOpcode(), target);
      }
      if (ainsn instanceof ASM_MethodInsnNode) {
         ASM_MethodInsnNode m = (ASM_MethodInsnNode) ainsn;
         return new MethodInsnNode(m.getOpcode(), m.owner, m.name, m.desc, m.itf);
      }
      if (ainsn instanceof ASM_FieldInsnNode) {
         ASM_FieldInsnNode f = (ASM_FieldInsnNode) ainsn;
         return new FieldInsnNode(f.getOpcode(), f.owner, f.name, f.desc);
      }
      if (ainsn instanceof ASM_VarInsnNode) {
         ASM_VarInsnNode v = (ASM_VarInsnNode) ainsn;
         return new VarInsnNode(v.getOpcode(), v.var);
      }
      if (ainsn instanceof ASM_LdcInsnNode) {
         ASM_LdcInsnNode l = (ASM_LdcInsnNode) ainsn;
         return new LdcInsnNode(l.cst);
      }
      if (ainsn instanceof ASM_InsnNode) {
         return new InsnNode(ainsn.getOpcode());
      }
      if (ainsn.asmNode != null) {
         return ainsn.asmNode;
      }
      return null;
   }
}
