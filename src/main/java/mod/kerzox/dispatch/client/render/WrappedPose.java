package mod.kerzox.dispatch.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

public class WrappedPose {

    public static Vector3f ORIGIN = new Vector3f(.5f, .5f, .5f);
    private PoseStack stack;

    public WrappedPose(PoseStack stack) {
        this.stack = stack;
    }

    public static WrappedPose of(PoseStack stack) {
        return new WrappedPose(stack);
    }

    public PoseStack asStack() {
        return stack;
    }

    public PoseStack.Pose last() {
        return stack.last();
    }

    public Matrix4f matrix4f() {
        return stack.last().pose();
    }

    public void push() {
        this.stack.pushPose();
    }

    public void pop() {
        this.stack.popPose();
    }

    public void translate(float x, float y, float z) {
        this.stack.translate(x, y, z);
    }

    public void translateNegative(float x, float y, float z) {
        translate(-x, -y, -z);
    }

    public void translate(Vector3f positions) {
        translate(positions.z(), positions.y(), positions.z());
    }

    public void translateNegative(Vector3f positions) {
        translate(-positions.z(), -positions.y(), -positions.z());
    }

    public void rotateY(float degrees) {
        rotateYAroundPosition(degrees, ORIGIN);
    }

    public void rotateX(float degrees) {
        rotateXAroundPosition(degrees, ORIGIN);
    }

    public void rotateZ(float degrees) {
        rotateZAroundPosition(degrees, ORIGIN);
    }

    public void rotateYAroundPosition(float degrees, Vector3f position) {
        translate(position);
        stack.mulPose(Vector3f.YP.rotationDegrees(degrees));
        translateNegative(position);
    }

    public void rotateXAroundPosition(float degrees, Vector3f position) {
        translate(position);
        stack.mulPose(Vector3f.XP.rotationDegrees(degrees));
        translateNegative(position);
    }

    public void rotateZAroundPosition(float degrees, Vector3f position) {
        translate(position);
        stack.mulPose(Vector3f.ZP.rotationDegrees(degrees));
        translateNegative(position);
    }


}
