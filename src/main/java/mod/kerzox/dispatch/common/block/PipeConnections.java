package mod.kerzox.dispatch.common.block;

import net.minecraft.util.StringRepresentable;

public enum PipeConnections implements StringRepresentable {
    NONE("none"),
    CONNECTED("connected");

    private final String name;
    PipeConnections(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    @Override
    public String getSerializedName() {
        return getName();
    }
}
