package ca.jakedot.eventhelper.config.gui;

import ca.jakedot.eventhelper.PlayerGroup;
import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import me.shedaniel.clothconfig2.gui.entries.AbstractListListEntry;
import me.shedaniel.clothconfig2.gui.widget.DynamicEntryListWidget;
import me.shedaniel.math.Rectangle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

// TODO still gotta add show changed values and highlight checkbox // perhaps implement temp fix for MC-259387 and extend hitbox left
// mostly based off AbstractTextFieldListListEntry
public class PlayerGroupListEntry extends AbstractListListEntry<String, PlayerGroupListEntry.AbstractPlayerGroupCell, PlayerGroupListEntry> {
    private final static Component CHECKED_BOX = Component.literal("☑");
    private final static Component UNCHECKED_BOX = Component.literal("☐");

    private final AddButtonCell addButtonCell;
    private final EditBox nameWidget;
    private final AbstractWidget toggleWidget;
    private final AbstractWidget removeWidget;
    private Runnable onRemove;
    private long removeConfirmationTimestamp;
    private boolean enabled;
    private PlayerGroup original;

    public PlayerGroupListEntry(Component fieldName, PlayerGroup value) {
        super(fieldName, value.players(), false, Optional::empty, null, Collections::emptyList, Component.empty(), false, false, false, PlayerGroupCell::new);
        this.setInsertButtonEnabled(false);

        this.addButtonCell = new AddButtonCell(this);
        this.nameWidget = new EditBox(Minecraft.getInstance().font, 100, 18, Component.empty());
        this.nameWidget.setBordered(false);
        this.nameWidget.setValue(value.name());
        this.nameWidget.moveCursorToStart(false);
        this.nameWidget.setHint(fieldName);
        this.nameWidget.setFormatter((string, firstCharacterIndex) -> FormattedCharSequence.forward(
                string, Style.EMPTY.withItalic(this.isEdited())
        ));
        this.enabled = value.enabled();
        this.toggleWidget = Button.builder(this.enabled ? CHECKED_BOX : UNCHECKED_BOX, button -> {
            this.enabled = !this.enabled;
            if (this.enabled) {
                button.setMessage(CHECKED_BOX);
            } else {
                button.setMessage(UNCHECKED_BOX);
            }
        }).size(18, 18).build();
        this.toggleWidget.setAlpha(0f);
        this.removeWidget = Button.builder(Component.translatable("event_helper.options.remove_group"), button -> {
            if (removeConfirmationTimestamp == 0) {
                removeConfirmationTimestamp = System.currentTimeMillis();
                button.setMessage(button.getMessage().copy().withStyle(ChatFormatting.RED));
            } else {
                this.onRemove.run();
            }
        }).width(80).build();
        onRemove = () -> {};

        this.widgets.addAll(0, List.of(this.toggleWidget, this.nameWidget, this.removeWidget));

        this.resetWidget.visible = false;
        this.widgets.remove(this.resetWidget);
        this.narratables.remove(this.resetWidget);
        this.original = value;
    }

    @Override
    public PlayerGroupListEntry self() {
        return this;
    }

    public void onRemove(Runnable onRemove) {
        this.onRemove = onRemove;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        // moving our cell to the end of each list
        this.widgets.remove(this.addButtonCell);
        this.widgets.add(this.addButtonCell);
        this.narratables.remove(this.addButtonCell);
        this.narratables.add(this.addButtonCell);

        this.cells.add(this.addButtonCell);
        super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        this.cells.remove(this.addButtonCell);

        this.toggleWidget.setX(x + 1);
        this.toggleWidget.setY(y + 1);
        this.toggleWidget.render(graphics, mouseX, mouseY, delta);

        String text = this.nameWidget.getValue().isEmpty() ? this.getFieldName().getString() : this.nameWidget.getValue();
        int underlineWidth = Minecraft.getInstance().font.width(text);

        this.nameWidget.setX(x + 20);
        this.nameWidget.setY(y + 6);
        this.nameWidget.setWidth(Math.max(underlineWidth + 7, 50));

        int oldCursor = this.nameWidget.getCursorPosition();
        this.nameWidget.setCursorPosition(0);
        this.nameWidget.setCursorPosition(oldCursor);

        graphics.fill(x + 20, y + 15, x + 20 + (this.nameWidget.isFocused() ? this.nameWidget.getWidth() : underlineWidth), y + 16, -5592406 /*this.getPreferredTextColor()*/); // where do we get the color from help
        this.nameWidget.render(graphics, mouseX, mouseY, delta);

        if (this.removeConfirmationTimestamp != 0 && System.currentTimeMillis() - this.removeConfirmationTimestamp > 5000L) {
            this.removeConfirmationTimestamp = 0L;
            this.removeWidget.setMessage(this.removeWidget.getMessage().copy().setStyle(Style.EMPTY));
        }
        this.removeWidget.setX(x + entryWidth - this.removeWidget.getWidth());
        this.removeWidget.setY(y);
        this.removeWidget.render(graphics, mouseX, mouseY, delta);
    }

    public PlayerGroup getPlayerGroup() {
        return new PlayerGroup(this.nameWidget.getValue(), this.getValue(), this.enabled);
    }

    @Override
    public boolean isEdited() { // should we compare this PlayerGroup to the original?
        return !Objects.equals(this.original.name(), this.nameWidget.getValue()) || !Objects.equals(this.original.players(), this.getValue());
    }

    @Override
    public int getItemHeight() {
        if (this.expanded) {
            return super.getItemHeight() + this.addButtonCell.getCellHeight();
        }
        return super.getItemHeight();
    }

    @Override
    public Component getDisplayedFieldName() {
        return Component.empty();
    }

    public abstract static class AbstractPlayerGroupCell extends AbstractListListEntry.AbstractListCell<String, AbstractPlayerGroupCell, PlayerGroupListEntry> {
        public AbstractPlayerGroupCell(@Nullable String value, PlayerGroupListEntry listListEntry) {
            super(value, listListEntry);
        }

        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            if (this.isMouseInside(mouseX, mouseY, y)) {
                Rectangle area = this.getEntryArea(y);
                if (this.getGrandparent() instanceof ClothConfigScreen.ListWidget) {
                    ((ClothConfigScreen.ListWidget<?>)this.getGrandparent()).thisTimeTarget = new Rectangle(area.x, area.y + this.getGrandparent().getScroll(), area.width, area.height);
                }
            }
        }

        private DynamicEntryListWidget<?> getGrandparent() {
            return this.listListEntry.getParent();
        }

        public Rectangle getEntryArea(int yOffset) {
            return new Rectangle(this.getGrandparent().left, yOffset, this.getGrandparent().right - this.getGrandparent().left, this.getCellHeight() - 4);
        }

        public boolean isMouseInside(int mouseX, int mouseY, int yOffset) {
            return this.getGrandparent().isMouseOver(mouseX, mouseY) && this.getEntryArea(yOffset).contains(mouseX, mouseY);
        }
    }

    public static class PlayerGroupCell extends AbstractPlayerGroupCell {
        protected EditBox textWidget;
        protected AbstractWidget removeWidget;
        private boolean isSelected;
        private boolean isHovered;

        public PlayerGroupCell(String value, PlayerGroupListEntry listListEntry) {
            super(value, listListEntry);
            this.textWidget = new EditBox(Minecraft.getInstance().font, 0, 0, 100, 18, Component.empty()) {
                public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
                    this.setFocused(PlayerGroupCell.this.isSelected);
                    super.renderWidget(graphics, mouseX, mouseY, delta);
                }
            };
            this.textWidget.setMaxLength(16);
            this.textWidget.setBordered(false);
            this.textWidget.setValue(value);
            this.textWidget.moveCursorToStart(false);
            this.textWidget.setResponder((s) -> {
                this.textWidget.setTextColor(this.getPreferredTextColor());
            });
            this.removeWidget = Button.builder(Component.literal("X"), button -> {
                this.listListEntry.cells.remove(this);
                this.listListEntry.widgets.remove(this);
                // remove from narratables too?
            }).width(20).build();
        }

        @Override
        public void updateSelected(boolean isSelected) {
            if (this.isSelected != isSelected && !isSelected) {
                this.textWidget.setValue(this.textWidget.getValue().trim());
            }
            this.isSelected = isSelected;
        }

        @Override
        public String getValue() {
            return this.textWidget.getValue();
        }

        @Override
        public Optional<Component> getError() {
            return Optional.empty();
        }

        @Override
        public int getCellHeight() {
            return 24;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);

            this.textWidget.setWidth(entryWidth - this.removeWidget.getWidth());
            this.textWidget.setX(x);
            this.textWidget.setY(y + 6);
            this.textWidget.setEditable(this.listListEntry.isEditable());
            this.textWidget.render(graphics, mouseX, mouseY, delta);
            this.isHovered = this.textWidget.isMouseOver(mouseX, mouseY);

            int textEnd = this.textWidget.getValue().isEmpty() ? x + 32 : this.textWidget.getScreenX(this.textWidget.getValue().length());
            graphics.fill(x, y + 15, this.isSelected ? x + 106 : textEnd, y + 16, -5592406);

            this.removeWidget.setX(x + entryWidth - this.removeWidget.getWidth());
            this.removeWidget.setY(y);
            this.removeWidget.render(graphics, mouseX, mouseY, delta);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.textWidget, this.removeWidget);
        }

        @Override
        public NarrationPriority narrationPriority() {
            return this.isSelected ? NarrationPriority.FOCUSED : (this.isHovered ? NarrationPriority.HOVERED : NarrationPriority.NONE);
        }

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {
            this.textWidget.updateNarration(narrationElementOutput);
        }
    }

    public static class AddButtonCell extends AbstractPlayerGroupCell {
        private final AbstractWidget addWidget;

        public AddButtonCell(PlayerGroupListEntry listListEntry) {
            super(null, listListEntry);

            this.addWidget = Button.builder(Component.translatable("event_helper.options.add_player"), button -> {
                int index = (int) listListEntry.widgets.stream()
                        .filter(widget -> widget instanceof PlayerGroupCell)
                        .count();
                PlayerGroupCell newCell = this.createNewCell(index);
                AddButtonCell.this.listListEntry.cells.add(newCell);
                AddButtonCell.this.listListEntry.widgets.add(newCell);
            }).width(80).build();
        }

        private PlayerGroupCell createNewCell(int index) {
            return new PlayerGroupCell(PlayerGroup.getExamplePlayer(index), this.listListEntry);
        }

        @Override
        public String getValue() {
            return Objects.toString(null);
        }

        @Override
        public Optional<Component> getError() {
            return Optional.empty();
        }

        @Override
        public int getCellHeight() {
            return 24;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
            this.addWidget.setX(x);
            this.addWidget.setY(y);
            this.addWidget.render(graphics, mouseX, mouseY, delta);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.singletonList(this.addWidget);
        }

        @Override
        public NarrationPriority narrationPriority() {
            return this.addWidget.narrationPriority();
        }

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {
            this.addWidget.updateNarration(narrationElementOutput);
        }
    }
}
