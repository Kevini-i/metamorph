package mchorse.metamorph.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

/**
 * Scroll list of available morphs
 * 
 * More morphs than presented in this menu are available, but the problem that 
 * it's impossible to list all variation of those morphs. iChun probably knows 
 * it, that's why he doesn't bother with a GUI of all available morphs.
 */
public class GuiMorphs extends GuiScrollPane
{
    private static final int cellH = 60;

    private int perRow;

    private int selected = -1;
    private int selectedMorph = -1;

    /**
     * This field stores categories, which store available morphs 
     */
    private List<MorphCategory> categories = new ArrayList<MorphCategory>();

    /**
     * Initiate this GUI.
     * 
     * Compile the categories list and compute the scroll height of this scroll pane 
     */
    public GuiMorphs(int perRow)
    {
        Map<String, MorphCategory> categories = new HashMap<String, MorphCategory>();

        for (List<AbstractMorph> morphs : MorphManager.INSTANCE.getMorphs().morphs.values())
        {
            for (AbstractMorph morph : morphs)
            {
                MorphCategory category = categories.get(morph.category);

                if (category == null)
                {
                    category = new MorphCategory(morph.category, morph.category);
                    categories.put(morph.category, category);
                }

                category.cells.add(new MorphCell(morph.name, morph, category.cells.size()));
            }
        }

        this.perRow = perRow;
        this.scrollHeight = 20;
        this.categories.addAll(categories.values());

        /* Calculate the scroll height and per category height */
        for (MorphCategory category : categories.values())
        {
            category.height = MathHelper.ceiling_float_int((float) category.cells.size() / (float) this.perRow);
            category.y = this.scrollHeight + 20;

            this.scrollHeight += category.height * cellH + 20;
        }

        this.scrollHeight -= 20;
    }

    /**
     * Get currently selected morph 
     */
    public AbstractMorph getSelected()
    {
        if (this.selected >= 0 && this.selected < this.categories.size())
        {
            MorphCategory category = this.categories.get(this.selected);

            if (this.selectedMorph >= 0 && this.selectedMorph < category.cells.size())
            {
                return category.cells.get(this.selectedMorph).morph;
            }
        }

        return null;
    }

    /**
     * 
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.isInside(mouseX, mouseY) && !this.dragging)
        {
            int y = mouseY - this.y + this.scrollY + 10;
            int x = (mouseX - this.x) / (this.w / this.perRow);
            int i = 0;

            MorphCategory cat = null;

            for (MorphCategory category : this.categories)
            {
                if (y >= category.y && y < category.y + category.height * cellH)
                {
                    cat = category;
                    break;
                }

                i++;
            }

            if (cat != null)
            {
                y = (y - cat.y) / cellH;

                this.selected = i;
                this.selectedMorph = x + y * this.perRow;
            }
            else
            {
                this.selected = -1;
                this.selectedMorph = -1;
            }
        }
    }

    /**
     * Don't draw the background 
     */
    @Override
    protected void drawBackground()
    {}

    @Override
    protected void drawPane(int mouseX, int mouseY, float partialTicks)
    {
        int m = this.w / this.perRow;
        int j = 0;

        /* Render morphs */
        for (MorphCategory category : this.categories)
        {
            this.drawString(fontRendererObj, category.title, this.x + 1, category.y, 0xFFFFFFFF);

            for (MorphCell cell : category.cells)
            {
                int i = cell.index;

                int x = i % this.perRow * m + this.x;
                int y = i / this.perRow * cellH + category.y;
                float scale = 21.5F;

                this.renderMorph(cell, Minecraft.getMinecraft().thePlayer, x + m / 2, y + 50, scale);

                if (j == this.selected && i == this.selectedMorph)
                {
                    this.renderSelected(x + 1, y + 10, m - 2, cellH);
                }
            }

            j++;
        }
    }

    /**
     * Render a morph 
     */
    private void renderMorph(MorphCell cell, EntityPlayer player, int x, int y, float scale)
    {
        /* Render the model */
        cell.morph.renderOnScreen(player, x, y, scale, 1.0F);
    }

    /**
     * Render a grey outline around the given area.
     * 
     * Basically, this method renders selection.
     */
    private void renderSelected(int x, int y, int width, int height)
    {
        int color = 0xffcccccc;

        this.drawHorizontalLine(x, x + width - 1, y, color);
        this.drawHorizontalLine(x, x + width - 1, y + height - 1, color);

        this.drawVerticalLine(x, y, y + height - 1, color);
        this.drawVerticalLine(x + width - 1, y, y + height - 1, color);
    }

    /**
     * Morph category class
     * 
     * This class is responsible for holding morph cells located in this 
     * category
     */
    public static class MorphCategory
    {
        /**
         * Prefix for morph category titles 
         */
        public static final String KEY = "morph.category.";

        public List<MorphCell> cells = new ArrayList<MorphCell>();
        public String title;
        public String key;
        public int height;
        public int y;

        public MorphCategory(String title, String key)
        {
            String result = I18n.format(KEY + title);

            if (title.isEmpty())
            {
                result = I18n.format(KEY + "unknown");
            }
            else if (result.equals(KEY + title))
            {
                result = I18n.format(KEY + "modded", title);
            }

            this.title = result;
            this.key = key;
        }
    }

    /**
     * Morph cell class
     * 
     * An instance of this class represents a morph which can be selected and 
     * morphed into upon pressing "Morph" button.
     */
    public static class MorphCell
    {
        public String name;
        public AbstractMorph morph;
        public int index;

        public MorphCell(String name, AbstractMorph morph, int index)
        {
            this.name = name;
            this.morph = morph;
            this.index = index;
        }
    }
}