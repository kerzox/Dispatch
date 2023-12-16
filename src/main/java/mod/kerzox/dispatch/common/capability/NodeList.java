package mod.kerzox.dispatch.common.capability;

import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class NodeList {

    private HashSet<LevelNode> nodes = new HashSet<>();

    public void removeNodeByPosition(BlockPos pos) {
        this.nodes.removeIf(n -> n.getPos().equals(pos));
    }

    public void removeNode(LevelNode pos) {
        this.nodes.remove(pos);
    }

    public void addNode(LevelNode node) {
        if (hasPosition(node.getPos())) return; // don't add another node
        this.nodes.add(node);
    }

    public void addByPosition(BlockPos pos) {
        if (hasPosition(pos)) return; // don't add another node
        this.nodes.add(new LevelNode(pos));
    }

    public boolean hasPosition(BlockPos pos) {
        for (LevelNode node : this.nodes) {
            if (node.getPos().equals(pos)) return true;
        }
        return false;
    }

    public int size() {
        return this.nodes.size();
    }

    public HashSet<LevelNode> getNodes() {
        return nodes;
    }

    public Set<BlockPos> getNodesAsPositions() {
        return nodes.stream().map(LevelNode::getPos).collect(Collectors.toSet());
    }

    public LevelNode getByPos(BlockPos pos) {
        for (LevelNode node : this.nodes) {
            if (node.getPos().equals(pos)) return node;
        }
        return null;
    }
}
