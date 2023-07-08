package org.zeith.multipart.net;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.*;
import org.zeith.hammerlib.net.*;
import org.zeith.multipart.api.PartEntity;
import org.zeith.multipart.blocks.BlockMultipartContainer;
import org.zeith.multipart.client.IClientPartDefinitionExtensions;

@MainThreaded
public class PacketSendRunningEffect
		implements IPacket
{
	protected BlockPos pos;
	protected int entityId;
	protected AABB entityAABB;
	protected Vec3 particlePos;
	
	public PacketSendRunningEffect(BlockPos pos, Entity entity)
	{
		this.pos = pos;
		this.entityId = entity.getId();
		this.entityAABB = entity.getBoundingBox();
		
		var random = RandomSource.create();
		BlockPos bpos = entity.blockPosition();
		double x = entity.getX() + (random.nextDouble() - 0.5D) * (double) entity.getBbWidth();
		double y = entity.getY() + 0.1D;
		double z = entity.getZ() + (random.nextDouble() - 0.5D) * (double) entity.getBbWidth();
		if(bpos.getX() != pos.getX())
			x = Mth.clamp(x, pos.getX(), (double) pos.getX() + 1.0D);
		if(bpos.getZ() != pos.getZ())
			z = Mth.clamp(z, pos.getZ(), (double) pos.getZ() + 1.0D);
		
		this.particlePos = new Vec3(x, y, z);
	}
	
	public PacketSendRunningEffect()
	{
	}
	
	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeBlockPos(pos);
		buf.writeInt(entityId);
		
		buf.writeDouble(entityAABB.minX);
		buf.writeDouble(entityAABB.minY);
		buf.writeDouble(entityAABB.minZ);
		buf.writeDouble(entityAABB.maxX);
		buf.writeDouble(entityAABB.maxY);
		buf.writeDouble(entityAABB.maxZ);
		
		buf.writeDouble(particlePos.x);
		buf.writeDouble(particlePos.y);
		buf.writeDouble(particlePos.z);
	}
	
	@Override
	public void read(FriendlyByteBuf buf)
	{
		pos = buf.readBlockPos();
		entityId = buf.readInt();
		
		double minX = buf.readDouble();
		double minY = buf.readDouble();
		double minZ = buf.readDouble();
		double maxX = buf.readDouble();
		double maxY = buf.readDouble();
		double maxZ = buf.readDouble();
		entityAABB = new AABB(
				minX, minY, minZ,
				maxX, maxY, maxZ
		);
		
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();
		particlePos = new Vec3(x, y, z);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientExecute(PacketContext ctx)
	{
		var level = Minecraft.getInstance().level;
		if(level != null)
		{
			var entity = level.getEntity(entityId);
			if(entity == null) return;
			
			var ctr = BlockMultipartContainer.pc(level, pos);
			
			Vec3 vec3 = entity.getDeltaMovement();
			Vec3 motion = new Vec3(vec3.x * -4.0D, 1.5D, vec3.z * -4.0D);
			
			for(PartEntity part : ctr.parts())
			{
				var shape = Shapes.join(part.getShape().move(pos.getX(), pos.getY(), pos.getZ()),
						Shapes.create(entityAABB.inflate(0.1)),
						BooleanOp.AND
				);
				if(shape.isEmpty()) continue;
				IClientPartDefinitionExtensions.of(part)
						.addRunningEffects(part, shape, entity, entityAABB, particlePos, motion);
			}
		}
	}
}