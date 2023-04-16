package mod.kerzox.dispatch.common.event;

import mod.kerzox.dispatch.common.block.multi.MultirolePipeBlock;
import mod.kerzox.dispatch.common.entity.MultirolePipe;
import mod.kerzox.dispatch.common.network.PacketHandler;
import mod.kerzox.dispatch.common.network.SyncBlockEntityAt;
import mod.kerzox.dispatch.common.util.PipeTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Iterator;

public class BlockEvents {

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        LevelAccessor level = event.getLevel();
        BlockPos pos = event.getPos();
        //TODO
        // check if a block has sub types if it does remove one of the non default type.
        if (level.getBlockEntity(pos) instanceof MultirolePipe pipe && pipe.getBlockState().getBlock() instanceof MultirolePipeBlock asBlock) {
            if (pipe.getSubtypes().size() > 1) {
                event.setCanceled(true);

                Iterator<PipeTypes> iterator = pipe.getSubtypes().iterator();
                while (iterator.hasNext()) { // we just remove one
                    PipeTypes subtype = iterator.next();
                    if (subtype != asBlock.getDefaultType()) {
                        iterator.remove();
                        // do drop
                        break;
                    }
                }

                //pipe.syncBlockEntity();
                pipe.getManager().detach(pipe);
                pipe.createManager();

//                for (Direction direction : Direction.values()) {
//                    BlockEntity entity = pipe.getAsBlockEntity().getLevel().getBlockEntity(pipe.getAsBlockEntity().getBlockPos().relative(direction));
//                    if (entity != null) {
//                        if (entity instanceof MultirolePipe pipe1) {
//                            pipe.addVisualConnection(direction, pipe1);
//                            pipe1.addVisualConnection(direction.getOpposite(), pipe);
//                        } else {
//                            pipe.addVisualConnection(direction, entity);
//                        }
//                    }
//                }

                pipe.getManager().addToUpdateQueue(pipe);

//                ChunkAccess chunkAccess = level.getChunk(pipe.getBlockPos());
//                PacketHandler.sendToAllClientsFromChunk(new SyncBlockEntityAt(pipe.getUpdateTag()), level.getChunkSource().getChunkNow(chunkAccess.getPos().x, chunkAccess.getPos().z));

            }
        }
    }

}
