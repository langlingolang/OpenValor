package valor.module.player;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import valor.event.EventTarget;
import valor.event.types.EventType;
import valor.events.TickEvent;
import valor.module.Module;
import valor.module.ModuleCategory;
import valor.setting.BooleanProperty;
import valor.setting.FloatProperty;
import valor.util.BlockUtil;
import valor.util.ReflectionUtil;

public class FastPlace extends Module {
   private static final Minecraft mc = Minecraft.getMinecraft();
   private static final DecimalFormat format = new DecimalFormat("0.0#", new DecimalFormatSymbols(Locale.US));

   private long delayTimer = 0L;
   public final FloatProperty delayProp = new FloatProperty("delay", 1.0F, 0.0F, 3.0F);
   public final BooleanProperty blocksOnlyProp = new BooleanProperty("blocks-only", Boolean.TRUE);
   public final BooleanProperty placeFixProp = new BooleanProperty("place-fix", Boolean.TRUE);
   public final BooleanProperty skipObsidianProp = new BooleanProperty("skip-obsidian", Boolean.TRUE);
   public final BooleanProperty skipInteractableProp = new BooleanProperty("skip-interactable", Boolean.TRUE);

   public FastPlace() {
      super("FastPlace", false, ModuleCategory.PLAYER);
   }

   @EventTarget
   public void onTick(TickEvent event) {
      if (!this.isEnabled() || event.getType() != EventType.PRE) {
         return;
      }

      int delay = ReflectionUtil.getRightClickDelayTimer(mc);
      if (delay == 4) {
         this.delayTimer += (long)(50.0F * (Float)this.delayProp.getValue());
      }
      if (this.delayTimer > 0L) {
         this.delayTimer -= 50L;
      }
      if (this.delayTimer > 0L || delay <= 1) {
         return;
      }

      ItemStack stack = mc.thePlayer != null ? mc.thePlayer.getHeldItem() : null;
      boolean shouldPlace = false;
      if (stack != null) {
         Item item = stack.getItem();
         if (item instanceof ItemFishingRod) {
            shouldPlace = false;
         } else if (item instanceof ItemBlock) {
            Block block = ((ItemBlock)item).getBlock();
            if ((Boolean)this.skipObsidianProp.getValue() && block instanceof BlockObsidian
               || (Boolean)this.skipInteractableProp.getValue() && BlockUtil.isInteractableBlock(block)) {
               shouldPlace = false;
            } else if (!(Boolean)this.placeFixProp.getValue()) {
               shouldPlace = true;
            } else {
               MovingObjectPosition mop = mc.objectMouseOver;
               shouldPlace = mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                  && ((ItemBlock)item).canPlaceBlockOnSide((World)mc.theWorld, mop.getBlockPos(), mop.sideHit, (EntityPlayer)mc.thePlayer, stack);
            }
         } else if (!(Boolean)this.blocksOnlyProp.getValue()) {
            shouldPlace = true;
         }
      } else if (!(Boolean)this.blocksOnlyProp.getValue()) {
         shouldPlace = true;
      }

      if (shouldPlace) {
         ReflectionUtil.resetRightClickDelayTimer(mc);
      }
   }

   @Override
   public void onDisabled() {
      this.delayTimer = 0L;
   }

   @Override
   public String[] getSuffix() {
      return new String[]{format.format(this.delayProp.getValue())};
   }
}
