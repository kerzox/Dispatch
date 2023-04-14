package mod.kerzox.dispatch.common.entity.manager;

import it.unimi.dsi.fastutil.Hash;
import mod.kerzox.dispatch.common.entity.MultirolePipe;
import mod.kerzox.dispatch.common.util.IPipe;
import mod.kerzox.dispatch.common.util.PipeTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class PipeNetworkUtil {

    public static HashSet<IPipe> traverseAndReturnAllNodes(HashSet<IPipe> networkToTraverse, IPipe startingNode) {
        Queue<IPipe> queue = new LinkedList<>();
        HashSet<IPipe> visited = new HashSet<>();

        queue.add(startingNode);
        visited.add(startingNode);

        while (!queue.isEmpty()) {

            IPipe current = queue.poll();

            for (Direction dir : Direction.values()) {
                if (current.getAsBlockEntity().getLevel().getBlockEntity(current.getAsBlockEntity().getBlockPos().relative(dir)) instanceof IPipe neighbour
                        && networkToTraverse.contains(neighbour)
                        && !visited.contains(neighbour) && !neighbour.getAsBlockEntity().isRemoved()) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }

        }
        return visited;
    }

    public static HashSet<IPipe> traverseAndReturnAllSubtypeNodes(HashSet<IPipe> networkToTraverse, IPipe startingNode, PipeTypes subType) {
        Queue<IPipe> queue = new LinkedList<>();
        HashSet<IPipe> visited = new HashSet<>();

        queue.add(startingNode);
        visited.add(startingNode);

        while (!queue.isEmpty()) {

            IPipe current = queue.poll();

            for (Direction dir : Direction.values()) {
                if (current.getAsBlockEntity().getLevel().getBlockEntity(current.getAsBlockEntity().getBlockPos().relative(dir)) instanceof IPipe neighbour
                        && networkToTraverse.contains(neighbour)
                        && !visited.contains(neighbour)
                        && !neighbour.getAsBlockEntity().isRemoved()
                        && neighbour.getSubtypes().contains(subType)) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }

        }
        return visited;
    }

    public static HashSet<IPipe> traverseAndFindValidCapabilityHolders(HashSet<IPipe> networkToTraverse, MultirolePipe startingNode) {
        Queue<MultirolePipe> queue = new LinkedList<>();
        HashSet<MultirolePipe> visited = new HashSet<>();
        HashSet<IPipe> populatedPipe = new HashSet<>();

        queue.add(startingNode);
        visited.add(startingNode);

        while (!queue.isEmpty()) {

            MultirolePipe current = queue.poll();

            current.findCapabilityHolders();

            if (current.hasCachedInventories()) {
                populatedPipe.add(current);
            }

            for (Direction dir : Direction.values()) {
                if (current.getAsBlockEntity().getLevel().getBlockEntity(current.getAsBlockEntity().getBlockPos().relative(dir)) instanceof MultirolePipe neighbour
                        && networkToTraverse.contains(neighbour)
                        && !visited.contains(neighbour)
                        && !neighbour.getAsBlockEntity().isRemoved()) {



                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }

        }
        return populatedPipe;
    }

}
