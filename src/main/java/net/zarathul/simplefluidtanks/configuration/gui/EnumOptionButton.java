package net.zarathul.simplefluidtanks.configuration.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;


@Environment(EnvType.CLIENT)
public class EnumOptionButton<E extends Enum<E>> extends Button
{
	private Class<E> clazz;
	private int selectedIndex;
	private String[] names;
	private TextComponent[] i18nNames;

	private static final String I18N_ENUM_PREFIX = "config.enums.";
	/**
	 * The button message is automatically localized. Localization keys are of the following format:
	 * "config.enums.enum_name.enum_value" e.g. "config.enums.colors.red". Note that both enum_name
	 * and enum_value need to be lower case.
	 */
	public EnumOptionButton(Class<E> clazz, String value, int x, int y, int width, int height)
	{
		super(x, y, width, height,
			  new TextComponent(I18n.get(I18N_ENUM_PREFIX + clazz.getSimpleName().toLowerCase() + "." + value.toLowerCase())),
			  (button) -> {;});

		this.clazz = clazz;
		this.selectedIndex = 0;

		int i = 0;

		E[] constants = clazz.getEnumConstants();
		this.names = new String[constants.length];
		this.i18nNames = new TextComponent[constants.length];

		for (E e : constants)
		{
			names[i] = e.name();
			i18nNames[i] = new TextComponent(I18n.get(I18N_ENUM_PREFIX + clazz.getSimpleName().toLowerCase() + "." + e.name().toLowerCase()));
			if (e.name().equals(value)) selectedIndex = i;
			i++;
		}
	}

	@Override
	public void onPress()
	{
		super.onPress();
		nextValue();
	}

	public E getValue()
	{
		return Enum.valueOf(this.clazz, names[this.selectedIndex]);
	}

	public void setValue(E value)
	{
		for (int i = 0; i < names.length; i++)
		{
			if (names[i].equals(value.name()))
			{
				this.selectedIndex = i;
				this.setMessage(this.i18nNames[i]);
				break;
			}
		}
	}

	private void nextValue()
	{
		this.selectedIndex = (this.selectedIndex + 1) % this.names.length;
		this.setMessage(this.i18nNames[this.selectedIndex]);
	}
}
