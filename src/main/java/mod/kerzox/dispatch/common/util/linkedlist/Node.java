package mod.kerzox.dispatch.common.util.linkedlist;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;

public class Node {

    BlockEntity node;
    private Map<Direction, BlockEntity> neighbors = new HashMap<>();

    public Node(BlockEntity node) {
        this.node = node;
    }

    public Node(BlockEntity e1, BlockEntity e2, Direction ofEdge) {
        this(e1);
        neighbors.put(ofEdge, e2);
    }

    public BlockEntity getNode() {
        return node;
    }

    public Map<Direction, BlockEntity> getNeighbors() {
        return neighbors;
    }

    public void addNeighbourAt(Direction direction, BlockEntity neighbour) {
        this.neighbors.put(direction, neighbour);
    }

    public void removeNeighbour(BlockEntity neighbour) {
        this.neighbors.entrySet().removeIf(n -> n.getValue() == neighbour);
    }

}
