package ca.jakedot.hideplayers.config.gui;

import ca.jakedot.hideplayers.PlayerGroup;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// TODO add targeting box to add button and pass collapse down to children
public class WhitelistSubCategoryListEntry extends SubCategoryListEntry {
    private final AbstractWidget addWidget;
    private final Consumer<List<PlayerGroup>> saveCallback;
    private final List<PlayerGroup> original;

    public WhitelistSubCategoryListEntry(Component categoryName, List<PlayerGroup> value, Consumer<List<PlayerGroup>> saveConsumer) {
        super(categoryName, new ArrayList<>(), false);
        this.addWidget = Button.builder(Component.translatable("dynamic_players.options.create_group"), button -> {
            this.createNewEntry(PlayerGroup.createDefaultGroup(this.getValue().size() + 1));
        }).width(80).build();
        this.getChildren().add(1, this.addWidget);
        for (PlayerGroup group : value) {
            this.createNewEntry(group);
        }
        this.saveCallback = saveConsumer;
        this.original = new ArrayList<>(value);
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
        if (this.isExpanded()) {
            int yy = y + 24;
            for (AbstractConfigListEntry<?> entry : this.filteredEntries()) {
                yy += entry.getItemHeight();
            }
            this.addWidget.setX(x + 14);
            this.addWidget.setY(yy);
            this.addWidget.render(graphics, mouseX, mouseY, delta);
        } else {
            for (AbstractConfigListEntry<?> entry : this.filteredEntries()) {
                if (entry instanceof PlayerGroupListEntry playerGroupListEntry) {
                    playerGroupListEntry.setExpanded(false);
                }
            }
        }
    }

    // temporary fix for MC-259387
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean bl = super.mouseClicked(mouseX, mouseY, button);
        if (bl && button == 0 && this.addWidget.isMouseOver(mouseX, mouseY)) {
            this.addWidget.setFocused(false);
        }
        return bl;
    }

    @Override
    public boolean isEdited() {
        return !this.getChildren().stream()
                .filter(entry -> entry instanceof PlayerGroupListEntry)
                .map(entry -> ((PlayerGroupListEntry)entry).getPlayerGroup())
                .toList().equals(this.original);
    }

    @Override
    public int getItemHeight() {
        int height = super.getItemHeight();
        if (this.isExpanded()) {
            height += this.addWidget.getHeight() + 4;
        }
        return height;
    }

    @Override
    public void save() {
        if (this.saveCallback != null) {
            this.saveCallback.accept(this.getChildren().stream()
                    .filter(entry -> entry instanceof PlayerGroupListEntry)
                    .map(entry -> ((PlayerGroupListEntry)entry).getPlayerGroup())
                    .toList());
        }
    }

    private void createNewEntry(PlayerGroup group) {
        List<Object> children = this.getChildren();
        PlayerGroupListEntry entry = new PlayerGroupListEntry(Component.literal(group.name()), group);
        entry.onRemove(() -> { // TODO unselect entry when removed
            this.getValue().remove(entry);
            this.getChildren().remove(entry);
        });
        this.getValue().add(entry);
        children.add(entry);
    }

    // i dont love this solution
    private List<Object> getChildren() {
        try {
            Field field = SubCategoryListEntry.class.getDeclaredField("children");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) field.get(this);
            return list;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
