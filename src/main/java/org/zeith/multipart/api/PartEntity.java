package org.zeith.multipart.api;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.zeith.hammerlib.api.io.NBTSerializationHelper;
import org.zeith.hammerlib.util.java.tuples.Tuple2;
import org.zeith.multipart.api.placement.PartPlacement;
import org.zeith.multipart.api.placement.PartPos;
import org.zeith.multipart.client.MultipartEffects;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public abstract class PartEntity
		implements IPartEntity
{
	protected final PartDefinition definition;
	protected final PartContainer container;
	protected final PartPlacement placement;
	protected final PartPos position;
	@Getter
	protected final int[] tintIndices;
	
	@Setter
	@Getter
	private boolean addedToWorld;
	
	@Deprecated(forRemoval = true)
	protected boolean syncDirty;
	
	protected boolean isShapeDirty, isCollisionShapeDirty;
	protected VoxelShape cachedShape, cachedCollisionShape;
	
	public PartEntity(PartDefinition definition, PartContainer container, PartPlacement placement)
	{
		this.tintIndices = new int[definition.getTintIndexCount()];
		this.container = container;
		this.definition = definition;
		this.placement = placement;
		this.position = new PartPos(container.pos(), placement);
	}
	
	@NotNull
	public PartPlacement getMainPart()
	{
		return placement;
	}
	
	public Collection<ResourceLocation> getParticleIcons(Set<ResourceLocation> allowed)
	{
		return allowed;
	}
	
	/**
	 * Return the hexadecimal color for tinting your part.
	 * NOTE: -1 WILL be called when particles are spawned.
	 */
	public int getTintLayerColor(@Range(from = -1, to = Integer.MAX_VALUE) int tintLayer)
	{
		return 0xFFFFFF;
	}
	
	public int getTintForParticle(ResourceLocation texture)
	{
		return 0xFFFFFF;
	}
	
	protected VoxelShape updateCollisionShape()
	{
		return updateShape();
	}
	
	protected VoxelShape updateShape()
	{
		return placement.getExampleShape();
	}
	
	public Optional<Tuple2<BlockState, Function<BlockPos, BlockEntity>>> disassemblePart()
	{
		return Optional.empty();
	}
	
	public AbstractContainerMenu openContainer(Player player, int windowId)
	{
		return null;
	}
	
	public float getDestroySpeed(Player player)
	{
		return definition.destroySpeed;
	}
	
	public boolean isCorrectToolForDrops(@NotNull Player player)
	{
		return true;
	}
	
	public void onRemovedBy(Player player, boolean willHarvest)
	{
		onRemoved(player, willHarvest && !player.isCreative(), false, true);
		
		var sound = definition().getSoundType(this);
		player.level().playSound(player, container().pos(), sound.getBreakSound(), SoundSource.BLOCKS,
				(sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F
		);
	}
	
	public void onRemoved(Player harvester, boolean spawnDrops, boolean playSound, boolean spawnParticles)
	{
		onRemove();
		
		if(playSound)
		{
			var sound = definition().getSoundType(this);
			container().level().playSound(null, container().pos(), sound.getBreakSound(), SoundSource.BLOCKS,
					(sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F
			);
		}
		
		if(spawnParticles)
		{
			if(FMLEnvironment.dist == Dist.CLIENT)
				MultipartEffects.spawnBreakFX(this);
		}
		
		if(spawnDrops && container.level instanceof ServerLevel sl)
		{
			var level = container.level();
			var pos = container.pos();
			
			var loot = new LootParams.Builder(sl)
					.withParameter(LootContextParams.TOOL,
							harvester != null ? harvester.getMainHandItem() : ItemStack.EMPTY
					)
					.withOptionalParameter(LootContextParams.THIS_ENTITY, harvester)
					.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos));
			
			for(ItemStack drop : getDrops(harvester instanceof ServerPlayer sp ? sp : null, loot))
				Block.popResource(level, pos, drop);
		}
	}
	
	public List<ItemStack> getDrops(@Nullable ServerPlayer harvester, LootParams.Builder context)
	{
		return List.of();
	}
	
	public void neighborChanged(@Nullable Direction from, BlockPos neigborPos, BlockState neigborState, boolean waterlogged)
	{
		if(!canSurviveInWater() && waterlogged)
			container.queuePartRemoval(placement, true, true, true);
	}
	
	public int getWeakSignal(Direction towards)
	{
		return 0;
	}
	
	public int getStrongSignal(Direction towards)
	{
		return 0;
	}
	
	public CompoundTag serialize(HolderLookup.Provider provider)
	{
		return NBTSerializationHelper.serialize(provider, this);
	}
	
	public void deserialize(HolderLookup.Provider provider, CompoundTag tag)
	{
		NBTSerializationHelper.deserialize(provider, this, tag);
	}
	
	public final PartDefinition definition()
	{
		return definition;
	}
	
	public SoundType getSoundType()
	{
		return definition.getSoundType(this);
	}
	
	public final PartContainer container()
	{
		return container;
	}
	
	public final PartPlacement placement()
	{
		return placement;
	}
	
	@Override
	public boolean isShapeDirty()
	{
		return isShapeDirty || cachedShape == null;
	}
	
	public boolean isCollisionShapeDirty()
	{
		return isCollisionShapeDirty || cachedCollisionShape == null;
	}
	
	public IndexedVoxelShape getSelectionShape(Player player, BlockHitResult hit)
	{
		return new IndexedVoxelShape(0, getShape());
	}
	
	public final VoxelShape getShape()
	{
		if(isShapeDirty())
		{
			cachedShape = updateShape();
			isShapeDirty = false;
		}
		return cachedShape;
	}
	
	public final VoxelShape getCollisionShape()
	{
		if(isCollisionShapeDirty())
		{
			cachedCollisionShape = updateCollisionShape();
			isCollisionShapeDirty = false;
		}
		return cachedCollisionShape;
	}
	
	public VoxelShape getPartOccupiedShape()
	{
		return getCollisionShape();
	}
	
	public VoxelShape getPartOccupiedShapeWith(PartEntity toBePlaced, VoxelShape shapeOfEntity)
	{
		return getPartOccupiedShape();
	}
	
	public InteractionResult useWithoutItem(Player player, BlockHitResult hit, IndexedVoxelShape selection)
	{
		return InteractionResult.PASS;
	}
	
	public ItemInteractionResult useItemOn(Player player, InteractionHand hand, BlockHitResult hit, IndexedVoxelShape selection)
	{
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}
	
	public void attack(Player player, BlockHitResult hit, IndexedVoxelShape selection)
	{
	}
	
	public void markForSync()
	{
		container.markForSync();
	}
	
	public ItemStack getCloneItemStack(BlockHitResult target, Player player, IndexedVoxelShape selection)
	{
		return definition.getCloneItem();
	}
	
	public void animateTick(RandomSource random)
	{
	}
	
	public boolean isRedstoneSource()
	{
		return false;
	}
	
	@Override
	public int getLightEmission()
	{
		return 0;
	}
	
	@Nullable
	public BlockState getRenderState()
	{
		return null;
	}
	
	@Nullable
	public BlockState getHardnessState()
	{
		return getRenderState();
	}
	
	public List<ModelResourceLocation> getRenderModels()
	{
		return List.of();
	}
	
	public void onPlaced()
	{
	}
	
	public void stepOn(Entity entity)
	{
	}
	
	public boolean isLadder(LivingEntity entity)
	{
		return false;
	}
	
	public boolean canHarvestPart(Player player)
	{
		var state = getRenderState();
		if(state != null)
			return state.canHarvestBlock(container.level(), container.pos(), player);
		return false;
	}
	
	public boolean canConnectRedstone(@Nullable Direction direction)
	{
		return false;
	}
	
	public BlockState getAppearance(BlockState state, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos)
	{
		return state;
	}
	
	public boolean makesOpenTrapdoorAboveClimbable(BlockState trapdoorState)
	{
		return false;
	}
	
	public boolean canSurviveInWater()
	{
		return definition.canSurviveInWater(this);
	}
	
	public static VoxelShape createAttachmentShape(Direction dir, float minAxial, float maxAxial, float elevation)
	{
		float invElevation = 1 - elevation;
		VoxelShape ourShape;
		if(dir == null) ourShape = Shapes.empty();
		else ourShape = switch(dir)
		{
			case DOWN -> Shapes.create(minAxial, 0, minAxial, maxAxial, elevation, maxAxial);
			case UP -> Shapes.create(minAxial, invElevation, minAxial, maxAxial, 1, maxAxial);
			case NORTH -> Shapes.create(minAxial, minAxial, 0, maxAxial, maxAxial, elevation);
			case SOUTH -> Shapes.create(minAxial, minAxial, invElevation, maxAxial, maxAxial, 1);
			case WEST -> Shapes.create(0, minAxial, minAxial, elevation, maxAxial, maxAxial);
			case EAST -> Shapes.create(invElevation, minAxial, minAxial, 1, maxAxial, maxAxial);
		};
		return ourShape;
	}
	
	public boolean blocksPlacementFor(PartDefinition definition, PartPlacement definitionPosition)
	{
		return false;
	}
	
	public void onChunkUnloaded()
	{
		onRemove();
	}
	
	/**
	 * Called when this is first added to the world (by {@link LevelChunk#addAndRegisterBlockEntity(BlockEntity)})
	 * or right before the first tick when the chunk is generated or loaded from disk.
	 * Override instead of adding {@code if (firstTick)} stuff in update.
	 */
	public void onLoad()
	{
	}
	
	/**
	 * Called when this entity is either broken or becomes unloaded.
	 */
	public void onRemove()
	{
	}
	
	public PartPos pos()
	{
		return position;
	}
	
	public boolean isViewBlocking()
	{
		return false;
	}
	
	/**
	 * Retrieve a capability with a given context from this part entity.
	 * It is recommended to use {@link #applyCapability(BlockCapability, Object, BlockCapability, Function)} to do safe typecasting.
	 * <p>
	 * Example:
	 * <pre>return applyCapability(capability, context, Capabilities.EnergyStorage.BLOCK, (d) -> (IEnergyStorage) this)
	 * 	.or(() -> applyCapability(capability, context, Capabilities.FluidHandler.BLOCK, (d) -> (IFluidHandler) this));</pre>
	 */
	public <T, C> Optional<T> getCapability(BlockCapability<T, C> capability, C context)
	{
		return Optional.empty();
	}
	
	public static <EXT_T, EXT_C, T, C> Optional<EXT_T> applyCapability(
			BlockCapability<EXT_T, EXT_C> cap, EXT_C ctx,
			BlockCapability<T, C> expected, Function<C, T> provider
	)
	{
		return cap != expected ? Optional.empty() : Optional.ofNullable((EXT_T) provider.apply((C) ctx));
	}
}