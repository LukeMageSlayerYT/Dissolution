package ladysnake.dissolution.common.items;

import ladysnake.dissolution.api.Soul;
import ladysnake.dissolution.api.SoulTypes;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.capabilities.CapabilitySoulHandler;
import ladysnake.dissolution.common.entity.souls.EntityFleetingSoul;
import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemSoulInAJar extends ItemJar {

    public ItemSoulInAJar() {
        super();
        this.setHasSubtypes(false);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        Soul soul = getSoul(stack);

        if (soul == Soul.UNDEFINED)
            return;

        tooltip.add(soul.getType().toString());
        if (flagIn.isAdvanced()) {
            tooltip.add("Purity: " + soul.getPurity());
            tooltip.add("Willingness: " + soul.getWillingness());
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        Soul soul = getSoul(stack);
        RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, true);
        if (raytraceresult.typeOfHit == RayTraceResult.Type.MISS) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        BlockPos pos = raytraceresult.getBlockPos().offset(raytraceresult.sideHit);
        if (!worldIn.isRemote)
            worldIn.spawnEntity(new EntityFleetingSoul(worldIn, pos.getX(), pos.getY(), pos.getZ(), soul));
        stack.shrink(1);
        ItemStack emptyJar = new ItemStack(ModItems.GLASS_JAR);
        if (stack.isEmpty())
            stack = emptyJar;
        else
            playerIn.addItemStackToInventory(emptyJar);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public Soul getSoul(ItemStack stack) {
        NBTTagCompound soulNBT = stack.getSubCompound("soul");
        if (soulNBT != null)
            return new Soul(soulNBT);
        return Soul.UNDEFINED;
    }

    public static ItemStack newTypedSoulBottle(SoulTypes soulType) {
        ItemStack stack = new ItemStack(ModItems.SOUL_IN_A_FLASK);
        NBTTagCompound nbt = new NBTTagCompound();
        Soul soul = new Soul(soulType);
        nbt.setTag("soul", soul.writeToNBT());
        stack.setTagCompound(nbt);
        return stack;
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if (tab == Dissolution.CREATIVE_TAB)
            items.add(new ItemStack(this));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new CapabilitySoulHandler.Provider();
    }
}
