package org.zeith.multipart.net;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.*;
import org.zeith.hammerlib.net.*;
import org.zeith.multipart.api.PartEntity;
import org.zeith.multipart.blocks.BlockMultipartContainer;
import org.zeith.multipart.client.IClientPartDefinitionExtensions;

@MainThreaded
public class PacketSendLandingEffect
		implements IPacket
{
	protected BlockPos pos;
	protected int numberOfParticles;
	protected int entityId;
	protected AABB entityAABB;
	protected Vec3 particlePos;
	
	public PacketSendLandingEffect(BlockPos pos, int numberOfParticles, LivingEntity entity)
	{
		this.pos = pos;
		this.numberOfParticles = numberOfParticles;
		this.entityId = entity.getId();
		this.entityAABB = entity.getBoundingBox();
		
		double x = entity.getX();
		double y = entity.getY();
		double z = entity.getZ();
		BlockPos entPos = entity.blockPosition();
		if(pos.getX() != entPos.getX() || pos.getZ() != entPos.getZ())
		{
			double d3 = x - (double) pos.getX() - 0.5D;
			double d5 = z - (double) pos.getZ() - 0.5D;
			double d6 = Math.max(Math.abs(d3), Math.abs(d5));
			x = (double) pos.getX() + 0.5D + d3 / d6 * 0.5D;
			z = (double) pos.getZ() + 0.5D + d5 / d6 * 0.5D;
		}
		
		this.particlePos = new Vec3(x, y, z);
	}
	
	public PacketSendLandingEffect()
	{
	}
	
	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeBlockPos(pos);
		buf.writeInt(numberOfParticles);
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
		numberOfParticles = buf.readInt();
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
		if(level != null && level.getEntity(entityId) instanceof LivingEntity living)
		{
			var ctr = BlockMultipartContainer.pc(level, pos);
			
			for(PartEntity part : ctr.parts())
			{
				var shape = Shapes.join(part.getShape().move(pos.getX(), pos.getY(), pos.getZ()),
						Shapes.create(entityAABB.inflate(0.1)),
						BooleanOp.AND
				);
				if(shape.isEmpty()) continue;
				IClientPartDefinitionExtensions.of(part)
						.addLandingEffects(part, shape, living, numberOfParticles, entityAABB, particlePos);
			}
		}
	}
}