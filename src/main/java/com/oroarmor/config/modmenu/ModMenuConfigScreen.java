package com.oroarmor.config.modmenu;

import java.util.stream.Collectors;

import com.oroarmor.config.Config;
import com.oroarmor.config.ConfigItem;
import com.oroarmor.config.ConfigItemGroup;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.text.TranslatableText;

/**
 * This class allows for the easy addition of a Mod Menu config screen to your
 * mod. The abstract modifier is so that your {@link ModMenuConfigScreen} can be
 * used as a entry point for modmenu, as you need to set the config in the
 * constructor for this to work. <br>
 * <br>
 * Add this to your entrypoint list in {@code fabric.mod.json}: <br>
 * <code>
 * "modmenu" : [ <br>
 * &emsp;"your.package.structure.YourModMenuConfigScreen" <br>
 * ]
 * </code>
 * 
 * @author Eli Orona
 *
 */
public abstract class ModMenuConfigScreen implements ModMenuApi {

	/**
	 * The config for the screen
	 */
	private final Config config;

	/**
	 * Creates a new {@link ModMenuConfigScreen}
	 * 
	 * @param config The config
	 */
	public ModMenuConfigScreen(Config config) {
		this.config = config;
	}

	private ConfigCategory createCategory(ConfigBuilder builder, String categoryName) {
		return builder.getOrCreateCategory(new TranslatableText(categoryName));
	}

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> {
			ConfigBuilder builder = ConfigBuilder.create().setParentScreen(screen).setTitle(new TranslatableText("config." + config.getID()));
			builder.setSavingRunnable(config::saveConfigToFile);

			ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();

			config.getConfigs().forEach(group -> {
				ConfigCategory groupCategory = createCategory(builder, "config." + config.getID() + "." + group.getName());
				group.getConfigs().forEach(configItem -> {
					setupConfigItem(configItem, groupCategory, entryBuilder);
				});
			});

			return builder.build();
		};
	}

	private void setupBooleanConfigItem(ConfigItem<Boolean> ci, ConfigCategory category, ConfigEntryBuilder entryBuilder) {
		category.addEntry(createBooleanConfigItem(ci, entryBuilder));
	}

	private void setupDoubleConfigItem(ConfigItem<Double> ci, ConfigCategory category, ConfigEntryBuilder entryBuilder) {
		category.addEntry(createDoubleConfigItem(ci, entryBuilder));
	}

	private void setupIntegerConfigItem(ConfigItem<Integer> ci, ConfigCategory category, ConfigEntryBuilder entryBuilder) {
		category.addEntry(createIntegerConfigItem(ci, entryBuilder));
	}

	private void setupStringConfigItem(ConfigItem<String> ci, ConfigCategory category, ConfigEntryBuilder entryBuilder) {
		category.addEntry(createStringConfigItem(ci, entryBuilder));
	}

	private AbstractConfigListEntry<?> createBooleanConfigItem(ConfigItem<Boolean> ci, ConfigEntryBuilder entryBuilder) {
		return entryBuilder.startBooleanToggle(new TranslatableText(ci.getDetails()), ci.getValue()).setSaveConsumer(ci::setValue).setDefaultValue(ci::getDefaultValue).build();
	}

	private AbstractConfigListEntry<?> createDoubleConfigItem(ConfigItem<Double> ci, ConfigEntryBuilder entryBuilder) {
		return entryBuilder.startDoubleField(new TranslatableText(ci.getDetails()), ci.getValue()).setSaveConsumer(ci::setValue).setDefaultValue(ci::getDefaultValue).build();
	}

	private AbstractConfigListEntry<?> createIntegerConfigItem(ConfigItem<Integer> ci, ConfigEntryBuilder entryBuilder) {
		return entryBuilder.startIntField(new TranslatableText(ci.getDetails()), ci.getValue()).setSaveConsumer(ci::setValue).setDefaultValue(ci::getDefaultValue).build();
	}

	private AbstractConfigListEntry<?> createStringConfigItem(ConfigItem<String> ci, ConfigEntryBuilder entryBuilder) {
		return entryBuilder.startStrField(new TranslatableText(ci.getDetails()), ci.getValue()).setSaveConsumer(ci::setValue).setDefaultValue(ci::getDefaultValue).build();
	}

	@SuppressWarnings("unchecked")
	private void setupConfigItem(ConfigItem<?> ci, ConfigCategory category, ConfigEntryBuilder entryBuilder) {
		switch (ci.getType()) {
		case BOOLEAN:
			setupBooleanConfigItem((ConfigItem<Boolean>) ci, category, entryBuilder);
			break;
		case DOUBLE:
			setupDoubleConfigItem((ConfigItem<Double>) ci, category, entryBuilder);
			break;
		case GROUP:
			SubCategoryBuilder groupCategory = entryBuilder.startSubCategory(new TranslatableText(category.getCategoryKey().getString() + "." + ci.getName()), ((ConfigItemGroup) ci).getConfigs().stream().map(configItem -> createConfigItem(ci, entryBuilder)).collect(Collectors.toList()));
			category.addEntry(groupCategory.build());
			break;
		case INTEGER:
			setupIntegerConfigItem((ConfigItem<Integer>) ci, category, entryBuilder);
			break;
		case STRING:
			setupStringConfigItem((ConfigItem<String>) ci, category, entryBuilder);
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private AbstractConfigListEntry<?> createConfigItem(ConfigItem<?> ci, ConfigEntryBuilder entryBuilder) {
		switch (ci.getType()) {
		case BOOLEAN:
			return createBooleanConfigItem((ConfigItem<Boolean>) ci, entryBuilder);
		case DOUBLE:
			return createDoubleConfigItem((ConfigItem<Double>) ci, entryBuilder);
		case INTEGER:
			return createIntegerConfigItem((ConfigItem<Integer>) ci, entryBuilder);
		case STRING:
			return createStringConfigItem((ConfigItem<String>) ci, entryBuilder);
		default:
			return null;
		}
	}

}
