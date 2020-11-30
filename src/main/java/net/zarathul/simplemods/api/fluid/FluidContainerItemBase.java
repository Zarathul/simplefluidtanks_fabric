package net.zarathul.simplemods.api.fluid;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zarathul.simplefluidtanks.Settings;

public abstract class FluidContainerItemBase extends Item implements IFluidContainerItem
{
	protected FluidContainerItemBase(Properties properties)
	{
		super(properties);
	}

	@Override
	public boolean canBeDepleted()
	{
		return false;
	}

	@Override
	public void onCraftedBy(ItemStack stack, Level level, Player player)
	{
		super.onCraftedBy(stack, level, player);
		initItemTag(stack);
	}

	@Override
	public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList)
	{
		ItemStack stack = new ItemStack(this);
		initItemTag(stack);

		nonNullList.add(stack);
	}

	@Override
	public boolean verifyTagAfterLoad(CompoundTag rootTag)
	{
		int capacity = getCapacity();
		CompoundTag tags = rootTag.getCompound("tag");

		if (tags.isEmpty())
		{
			CompoundTag containerTag = new CompoundTag();
			FluidStack.empty().save(containerTag);
			tags.put(CONTAINER_TAG_NAME, containerTag);
			tags.putInt("Damage", capacity);
		}
		else
		{
			CompoundTag containerTag = tags.getCompound(CONTAINER_TAG_NAME);
			FluidStack fluid = FluidStack.load(containerTag);
			// Limit the fluid amount to the current capacity. This is necessary in case the capacity is lowered in the config.
			if (fluid.getAmount() > capacity)
			{
				fluid.setAmount(capacity);
				fluid.save(containerTag);
			}

			// Update the damage value. This has to be done, in case the capacity was changed in the config.
			int damage = capacity - fluid.getAmount();
			damage = Math.max(damage, 1);
			tags.putInt("Damage", damage);
		}

		return true;
	}

	private static void initItemTag(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTagElement(CONTAINER_TAG_NAME);
		FluidStack.empty().save(tag);
		stack.setDamageValue(Settings.bucketsPerPortableTank * FluidStack.BUCKET_VOLUME);
	}
}
