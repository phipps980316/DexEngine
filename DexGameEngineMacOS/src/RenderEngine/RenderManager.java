package RenderEngine;

import Entities.Camera;
import Entities.Entity;
import Entities.Light;
import Models.TexturedModel;
import Shaders.EntityShader;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenderManager {
    private static final float FOV = 90;
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 10000f;

    private static final Vector3f skyColour = new Vector3f(0.2f,0.2f,0.4f);

    private Matrix4f projectionMatrix;

    private final EntityShader modelShader;
    private final ModelRenderer modelRenderer;

    private final Map<TexturedModel, List<Entity>> models = new HashMap<>();

    public RenderManager(){
        enableCulling();
        createProjectionMatrix();

        modelShader = new EntityShader();
        modelRenderer = new ModelRenderer(modelShader, projectionMatrix);
    }

    private void createProjectionMatrix(){
        float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
        float yScale = (float) (1f / Math.tan(Math.toRadians(FOV/2f))) * aspectRatio;
        float xScale = yScale / aspectRatio;
        float frustumLength = FAR_PLANE - NEAR_PLANE;

        projectionMatrix = new Matrix4f();
        projectionMatrix.m00 = xScale;
        projectionMatrix.m11 = yScale;
        projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustumLength);
        projectionMatrix.m23 = -1;
        projectionMatrix.m32 = -((2*NEAR_PLANE*FAR_PLANE) / frustumLength);
        projectionMatrix.m33 = 0;
    }

    public void prepare(){
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glClearColor(skyColour.x, skyColour.y, skyColour.z, 1);
    }

    public void render(List<Light> lights, Camera camera){
        prepare();

        modelShader.start();
        modelShader.loadSkyColour(skyColour);
        modelShader.loadLights(lights);
        modelShader.loadViewMatrix(camera);
        modelRenderer.render(models);
        modelShader.stop();

        models.clear();
    }

    public static void enableCulling(){
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
    }

    public static void disableCulling(){
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    public void processModel(Entity entity){
        TexturedModel model = entity.getModel();
        List<Entity> batch = models.get(model);
        if(batch != null) {
            batch.add(entity);
        }
        else {
            List<Entity> newBatch = new ArrayList<>();
            newBatch.add(entity);
            models.put(model, newBatch);
        }
    }

    public void cleanUp(){
        modelShader.cleanUp();
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }
}
