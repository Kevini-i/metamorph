package mchorse.metamorph.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mchorse.metamorph.api.abilities.IAbility;
import mchorse.metamorph.api.abilities.IAction;
import mchorse.metamorph.api.abilities.IAttackAbility;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.SkeletonType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Morph manager class
 * 
 * This manager is responsible for managing available morphings.
 */
public class MorphManager
{
    /**
     * Default <s>football</s> morph manager 
     */
    public static final MorphManager INSTANCE = new MorphManager();

    /**
     * Registered abilities 
     */
    public Map<String, IAbility> abilities = new HashMap<String, IAbility>();

    /**
     * Registered actions 
     */
    public Map<String, IAction> actions = new HashMap<String, IAction>();

    /**
     * Registered attacks 
     */
    public Map<String, IAttackAbility> attacks = new HashMap<String, IAttackAbility>();

    /**
     * Registered morph factories
     */
    public List<IMorphFactory> factories = new ArrayList<IMorphFactory>();

    /**
     * Model manager
     */
    public ModelManager models;

    /**
     * That's a singleton, boy! 
     */
    private MorphManager()
    {}

    /**
     * Register all morph factories 
     */
    public void register()
    {
        for (int i = this.factories.size() - 1; i >= 0; i--)
        {
            this.factories.get(i).register(this);
        }
    }

    /**
     * Register all morph factories on the client side 
     */
    @SideOnly(Side.CLIENT)
    public void registerClient()
    {
        for (int i = this.factories.size() - 1; i >= 0; i--)
        {
            this.factories.get(i).registerClient(this);
        }
    }

    /**
     * Checks if manager has given morph by ID and NBT tag compound
     * 
     * This meethod iterates over all {@link IMorphFactory}s and if any of them 
     * returns true, then there's a morph, otherwise false.
     */
    public boolean hasMorph(String name)
    {
        for (int i = this.factories.size() - 1; i >= 0; i--)
        {
            if (this.factories.get(i).hasMorph(name))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Get an abstract morph from NBT
     * 
     * This method iterates over all {@link IMorphFactory}s, returns a morph 
     * from the first morph factory that does have a morph.
     */
    public AbstractMorph morphFromNBT(NBTTagCompound tag)
    {
        String name = tag.getString("Name");

        for (int i = this.factories.size() - 1; i >= 0; i--)
        {
            if (this.factories.get(i).hasMorph(name))
            {
                return this.factories.get(i).getMorphFromNBT(tag);
            }
        }

        return null;
    }

    /**
     * Get all morphs that factories provide
     */
    public List<AbstractMorph> getMorphs()
    {
        List<AbstractMorph> morphs = new ArrayList<AbstractMorph>();

        for (int i = this.factories.size() - 1; i >= 0; i--)
        {
            morphs.addAll(this.factories.get(i).getMorphs());
        }

        return morphs;
    }

    /**
     * Get morph from the entity
     * 
     * Here I should add some kind of mechanism that allows people to substitute 
     * the name of the morph based on the given entity (in the future with 
     * introduction of the public API).
     */
    public String morphNameFromEntity(Entity entity)
    {
        if (entity instanceof EntitySkeleton)
        {
            SkeletonType skeleton = ((EntitySkeleton) entity).func_189771_df();

            if (skeleton.equals(SkeletonType.WITHER))
            {
                return "WitherSkeleton";
            }
        }

        return EntityList.getEntityString(entity);
    }

    /**
     * Get display name for morph (only client)
     */
    @SideOnly(Side.CLIENT)
    public String morphDisplayNameFromMorph(String morph)
    {
        if (morph.equals("WitherSkeleton"))
        {
            morph = "Skeleton";
        }

        String key = "entity." + morph + ".name";
        String result = I18n.format(key);

        return key.equals(result) ? morph : result;
    }
}