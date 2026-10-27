package valor.asm;

import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ASM_ClassReader {
   private final byte[] b;

   public ASM_ClassReader(byte[] b) {
      this.b = b;
   }

   public void accept(ASM_ClassNode classNode, int parsingOptions) {
      ClassReader cr = new ClassReader(this.b);
      ClassNode cn = new ClassNode();
      cr.accept(cn, parsingOptions);

      classNode.originalNode = cn;
      classNode.name = cn.name;
      classNode.fields.clear();
      classNode.methods.clear();

      if (cn.fields != null) {
         for (Object obj : cn.fields) {
            FieldNode fn = (FieldNode) obj;
            ASM_FieldNode afn = new ASM_FieldNode(fn.access, fn.name, fn.desc, fn.signature, fn.value);
            afn.asmNode = fn;
            classNode.fields.add(afn);
         }
      }

      if (cn.methods != null) {
         for (Object obj : cn.methods) {
            MethodNode mn = (MethodNode) obj;
            ASM_MethodNode amn = new ASM_MethodNode();
            amn.originalNode = mn;
            amn.name = mn.name;
            amn.desc = mn.desc;
            amn.maxStack = mn.maxStack;

            Map<LabelNode, ASM_LabelNode> labelMap = new HashMap<LabelNode, ASM_LabelNode>();
            AbstractInsnNode cur = mn.instructions.getFirst();
            while (cur != null) {
               if (cur instanceof LabelNode) {
                  ASM_LabelNode albl = new ASM_LabelNode();
                  albl.asmNode = cur;
                  labelMap.put((LabelNode) cur, albl);
               }
               cur = cur.getNext();
            }

            cur = mn.instructions.getFirst();
            while (cur != null) {
               ASM_AbstractInsnNode ainsn = convertToAsm(cur, labelMap);
               ainsn.asmNode = cur;
               amn.instructions.add(ainsn);
               cur = cur.getNext();
            }

            classNode.methods.add(amn);
         }
      }
   }

   private static ASM_AbstractInsnNode convertToAsm(AbstractInsnNode insn, Map<LabelNode, ASM_LabelNode> labelMap) {
      if (insn instanceof LabelNode) {
         ASM_LabelNode al = labelMap.get(insn);
         return al != null ? al : new ASM_LabelNode();
      }
      if (insn instanceof JumpInsnNode) {
         JumpInsnNode j = (JumpInsnNode) insn;
         ASM_LabelNode target = labelMap.get(j.label);
         if (target == null) {
            target = new ASM_LabelNode();
            target.asmNode = j.label;
            labelMap.put(j.label, target);
         }
         return new ASM_JumpInsnNode(j.getOpcode(), target);
      }
      if (insn instanceof MethodInsnNode) {
         MethodInsnNode m = (MethodInsnNode) insn;
         return new ASM_MethodInsnNode(m.getOpcode(), m.owner, m.name, m.desc, m.itf);
      }
      if (insn instanceof FieldInsnNode) {
         FieldInsnNode f = (FieldInsnNode) insn;
         return new ASM_FieldInsnNode(f.getOpcode(), f.owner, f.name, f.desc);
      }
      if (insn instanceof VarInsnNode) {
         VarInsnNode v = (VarInsnNode) insn;
         return new ASM_VarInsnNode(v.getOpcode(), v.var);
      }
      if (insn instanceof LdcInsnNode) {
         LdcInsnNode l = (LdcInsnNode) insn;
         return new ASM_LdcInsnNode(l.cst);
      }
      if (insn instanceof InsnNode) {
         return new ASM_InsnNode(insn.getOpcode());
      }
      return new ASM_OpaqueInsnNode(insn);
   }
}
