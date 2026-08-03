package net.zarathul.simplemods.api.fluid;

import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zarathul.simplefluidtanks.Settings;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;

public abstract class FluidContainerItemBase extends Item implements IFluidContainerItem
{
	protected FluidContainerItemBase(Properties properties)
	{
		super(properties.component(SimpleFluidTanks.FLUID_CONTAINER_COMPONENT, new FluidContainerComponent(0, Settings.bucketsPerPortableTank(), FluidStack.empty().getRegistryKey())));
	}

	@Override
	public boolean isBarVisible(ItemStack stack)
	{
		return true;
	}

	@Override
	public int getBarWidth(ItemStack stack)
	{
		var componentData = stack.get(SimpleFluidTanks.FLUID_CONTAINER_COMPONENT);
		if (componentData != null) return Mth.clamp(Math.round((componentData.amount() / (float)componentData.capacity()) * 13.0f), 0, 13);
		else return super.getBarWidth(stack);
	}
	@Override
	public int getBarColor(ItemStack stack)
	{
		var componentData = stack.get(SimpleFluidTanks.FLUID_CONTAINER_COMPONENT);
		if (componentData != null)
		{
			int capacity = componentData.capacity();
			float freeCapacity = Math.max(0.0F, ((float)capacity - componentData.amount()) / capacity);
			return Mth.hsvToRgb(freeCapacity / 3.0F, 1.0F, 1.0F);
		}
		else return super.getBarColor(stack);
	}

//
//	@Override
//	public void onCraftedBy(ItemStack itemStack, Player player)
//	{
//		super.onCraftedBy(itemStack, player);
//		initItemTag(itemStack);
//	}
//
//	@Override
//	public void onCraftedPostProcess(ItemStack itemStack, Level level)
//	{
//		super.onCraftedPostProcess(itemStack, level);
//		initItemTag(itemStack);
//	}

//	@Override
//	public boolean verifyTagAfterLoad(CompoundTag rootTag)
//	{
//		int capacity = getCapacity();
//		CompoundTag tags = rootTag.getCompound("tag").get();
//
//		if (tags.isEmpty())
//		{
//			CompoundTag containerTag = new CompoundTag();
//			FluidStack.empty().save(containerTag);
//			tags.put(CONTAINER_TAG_NAME, containerTag);
//			tags.putInt("Damage", capacity);
//		}
//		else
//		{
//			CompoundTag containerTag = tags.getCompound(CONTAINER_TAG_NAME).get();
//			FluidStack fluid = FluidStack.load(containerTag);
//			// Limit the fluid amount to the current capacity. This is necessary in case the capacity is lowered in the config.
//			if (fluid.getAmount() > capacity)
//			{
//				fluid.setAmount(capacity);
//				fluid.save(containerTag);
//			}
//
//			// Update the damage value. This has to be done, in case the capacity was changed in the config.
//			int damage = capacity - fluid.getAmount();
//			damage = Math.max(damage, 1);
//			tags.putInt("Damage", damage);
//		}
//
//		return true;
//	}
//
//	private static void initItemTag(ItemStack stack)
//	{
//		ValueOutput output = new TagValueOutput()
//		CompoundTag tag = stack.getOrCreateTagElement(CONTAINER_TAG_NAME);
//		FluidStack.empty().save(tag);
//		stack.setDamageValue(Settings.bucketsPerPortableTank() * FluidStack.BUCKET_VOLUME);
//	}
}
