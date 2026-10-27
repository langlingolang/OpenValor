package valor.asm;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public final class ASM_InsnList implements Iterable<ASM_AbstractInsnNode> {
   public int size;
   public ASM_AbstractInsnNode first;
   public ASM_AbstractInsnNode last;
   public ASM_AbstractInsnNode[] cache;

   @Override
   public Iterator<ASM_AbstractInsnNode> iterator() {
      return new InsnListIterator();
   }

   public int size() {
      return this.size;
   }

   public ASM_AbstractInsnNode getFirst() {
      return this.first;
   }

   public ASM_AbstractInsnNode getLast() {
      return this.last;
   }

   public final void insert(ASM_AbstractInsnNode insn) {
      if (this.first == null) {
         this.add(insn);
      } else {
         this.insertBefore(this.first, insn);
      }
   }

   public final void insert(ASM_InsnList list) {
      if (list.size != 0) {
         this.size += list.size;
         if (this.first == null) {
            this.first = list.first;
            this.last = list.last;
         } else {
            ASM_AbstractInsnNode listLast = list.last;
            this.first.prev = listLast;
            listLast.next = this.first;
            this.first = list.first;
         }

         this.cache = null;
         list.clear();
      }
   }

   public final void insertBefore(ASM_AbstractInsnNode location, ASM_AbstractInsnNode insn) {
      this.size++;
      ASM_AbstractInsnNode prevNode = location.prev;
      if (prevNode == null) {
         this.first = insn;
      } else {
         prevNode.next = insn;
      }

      location.prev = insn;
      insn.next = location;
      insn.prev = prevNode;
      this.cache = null;
   }

   public final void insertBefore(ASM_AbstractInsnNode location, ASM_InsnList list) {
      if (list.size != 0) {
         this.size += list.size;
         ASM_AbstractInsnNode listFirst = list.first;
         ASM_AbstractInsnNode listLast = list.last;
         ASM_AbstractInsnNode prevNode = location.prev;
         if (prevNode == null) {
            this.first = listFirst;
         } else {
            prevNode.next = listFirst;
         }

         location.prev = listLast;
         listLast.next = location;
         listFirst.prev = prevNode;
         this.cache = null;
         list.clear();
      }
   }

   public final void insert(ASM_AbstractInsnNode location, ASM_InsnList list) {
      if (list.size != 0) {
         this.size += list.size;
         ASM_AbstractInsnNode listFirst = list.first;
         ASM_AbstractInsnNode listLast = list.last;
         ASM_AbstractInsnNode nextNode = location.next;
         if (nextNode == null) {
            this.last = listLast;
         } else {
            nextNode.prev = listLast;
         }

         location.next = listFirst;
         listLast.next = nextNode;
         listFirst.prev = location;
         this.cache = null;
         list.clear();
      }
   }

   public final void insert(ASM_AbstractInsnNode location, ASM_AbstractInsnNode insn) {
      this.size++;
      ASM_AbstractInsnNode nextNode = location.next;
      if (nextNode == null) {
         this.last = insn;
      } else {
         nextNode.prev = insn;
      }

      location.next = insn;
      insn.next = nextNode;
      insn.prev = location;
      this.cache = null;
   }

   public final ASM_AbstractInsnNode[] toArray() {
      java.util.List<ASM_AbstractInsnNode> list = new java.util.ArrayList<ASM_AbstractInsnNode>(this.size > 0 ? this.size : 16);
      ASM_AbstractInsnNode current = this.first;

      while (current != null) {
         list.add(current);
         current = current.next;
      }

      return list.toArray(new ASM_AbstractInsnNode[list.size()]);
   }

   public final void add(ASM_AbstractInsnNode insn) {
      this.size++;
      if (this.last == null) {
         this.first = insn;
         this.last = insn;
      } else {
         this.last.next = insn;
         insn.prev = this.last;
      }

      this.last = insn;
      this.cache = null;
   }

   public final void add(ASM_InsnList list) {
      if (list.size != 0) {
         this.size += list.size;
         if (this.last == null) {
            this.first = list.first;
            this.last = list.last;
         } else {
            ASM_AbstractInsnNode listFirst = list.first;
            this.last.next = listFirst;
            listFirst.prev = this.last;
            this.last = list.last;
         }

         this.cache = null;
         list.clear();
      }
   }

   public final void remove(ASM_AbstractInsnNode insn) {
      this.size--;
      ASM_AbstractInsnNode nextNode = insn.next;
      ASM_AbstractInsnNode prevNode = insn.prev;
      if (nextNode == null) {
         if (prevNode == null) {
            this.first = null;
            this.last = null;
         } else {
            prevNode.next = null;
            this.last = prevNode;
         }
      } else if (prevNode == null) {
         this.first = nextNode;
         nextNode.prev = null;
      } else {
         prevNode.next = nextNode;
         nextNode.prev = prevNode;
      }

      this.cache = null;
      insn.prev = null;
      insn.next = null;
   }

   public final void clear() {
      this.size = 0;
      this.first = null;
      this.last = null;
      this.cache = null;
   }

   final class InsnListIterator implements ListIterator<ASM_AbstractInsnNode> {
      private ASM_AbstractInsnNode nextInsn = ASM_InsnList.this.first;
      private ASM_AbstractInsnNode prevInsn = null;
      private ASM_AbstractInsnNode removeInsn = null;

      InsnListIterator() {
      }

      @Override
      public final void remove() {
         if (this.removeInsn != null) {
            if (this.removeInsn == this.nextInsn) {
               this.nextInsn = this.nextInsn.next;
            } else {
               this.prevInsn = this.prevInsn.prev;
            }

            ASM_InsnList.this.remove(this.removeInsn);
            this.removeInsn = null;
         } else {
            throw new IllegalStateException();
         }
      }

      @Override
      public final void add(ASM_AbstractInsnNode insn) {
         if (this.nextInsn != null) {
            ASM_InsnList.this.insertBefore(this.nextInsn, insn);
         } else if (this.prevInsn != null) {
            ASM_InsnList.this.insert(this.prevInsn, insn);
         } else {
            ASM_InsnList.this.add(insn);
         }

         this.prevInsn = insn;
         this.removeInsn = null;
      }

      @Override
      public final boolean hasNext() {
         return this.nextInsn != null;
      }

      @Override
      public final ASM_AbstractInsnNode next() {
         if (this.nextInsn == null) {
            throw new NoSuchElementException();
         } else {
            ASM_AbstractInsnNode node = this.nextInsn;
            this.prevInsn = node;
            this.nextInsn = node.next;
            this.removeInsn = node;
            return node;
         }
      }

      @Override
      public final void set(ASM_AbstractInsnNode insn) {
         if (this.removeInsn != null) {
            ASM_AbstractInsnNode target = this.removeInsn;
            ASM_InsnList list = ASM_InsnList.this;
            ASM_AbstractInsnNode nextNode = target.next;
            insn.next = nextNode;
            if (nextNode != null) {
               nextNode.prev = insn;
            } else {
               list.last = insn;
            }

            ASM_AbstractInsnNode prevNode = target.prev;
            insn.prev = prevNode;
            if (prevNode != null) {
               prevNode.next = insn;
            } else {
               list.first = insn;
            }

            target.prev = null;
            target.next = null;
            if (this.removeInsn == this.prevInsn) {
               this.prevInsn = insn;
            } else {
               this.nextInsn = insn;
            }
         } else {
            throw new IllegalStateException();
         }
      }

      @Override
      public final int previousIndex() {
         return -1;
      }

      @Override
      public final boolean hasPrevious() {
         return this.prevInsn != null;
      }

      @Override
      public final ASM_AbstractInsnNode previous() {
         if (this.prevInsn == null) {
            throw new NoSuchElementException();
         } else {
            ASM_AbstractInsnNode node = this.prevInsn;
            this.nextInsn = node;
            this.prevInsn = node.prev;
            this.removeInsn = node;
            return node;
         }
      }

      @Override
      public final int nextIndex() {
         return 0;
      }
   }
}
