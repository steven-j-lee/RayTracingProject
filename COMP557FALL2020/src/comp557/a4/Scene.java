package comp557.a4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.*;
/**
 * Simple scene loader based on XML file format.
 */
//steven lee 260803947
public class Scene {

    /** List of surfaces in the scene */
    public List<Intersectable> surfaceList = new ArrayList<Intersectable>();

	/** All scene lights */
	public Map<String,Light> lights = new HashMap<String,Light>();

    /** Contains information about how to render the scene */
    public Render render;

    /** The ambient light colour */
    public Color3f ambient = new Color3f();

    /**
     * Default constructor.
     */
    public Scene() {
    	this.render = new Render();
    }

    /**
     * renders the scene
     */
    public void render(boolean showPanel) {

        Camera cam = render.camera;
        int w = cam.imageSize.width;
        int h = cam.imageSize.height;

        render.init(w, h, showPanel);
        Color3f c = new Color3f(render.bgcolor);
        // You should also add a boolean member to the Render class to store a attribute jitter
        // (modify the parser to set the member on loading a scene).
        // Per pixel jittering can help replace aliasing with noise if there is
        // a regular sampling pattern applied for super sampling at each pixel.
        // Even without super sampling, where one normally would cast a sample in the middle of the pixel,
        // a small amount of sub-pixel jittering is useful to deal with aliasing
        //fundamentals of computer graphics ch.13 13.4.1
        //create tempOffset
        //super sampling
        double[] tempOffset = new double[2];
        //tempOffset[0] = 0.5;
        //tempOffset[1] = 0.5;
        //check if jitter is true or not
        if(render.jitter) {
            for (int j = 0; j < h && !render.isDone(); j++) {
                for (int i = 0; i < w && !render.isDone(); i++) {
                    // TODO: Objective 1: generate a ray (use the generateRay method)
                    //create tempOffset
                    //double[] tempOffset = new double[2];
                    //n by n grid of the sample size
                    for (int n = 0; n < render.samples; n++) {
                        for (int n2 = 0; n2 < render.samples; n2++) {
                            //we can assume num of pixel will be power of 2
                            //exclusive 1
                            //stratified sampling
                            double random1 = ThreadLocalRandom.current().nextDouble(0.0, 0.7);
                            double random2 = ThreadLocalRandom.current().nextDouble(0.0, 0.65);
                            tempOffset[0] = ((double)n + random1) / (double)render.samples;
                            tempOffset[1] = ((double)n2 + random2) / (double)render.samples;
                            Ray ray = new Ray();
                            //FCG 13.4.1 p330

                            //depth of field blur  http://cg.skeelogy.com/depth-of-field-using-raytracing/


                            //ray
                            //j i per pixel use uniform sampling offered by java between 0 to 1
                            generateRay(i, j, tempOffset, cam, ray);
                            // TODO: Objective 2: test for intersection with scene surfaces
                            // for(Ray tempRay: rayList) {
                            //test intersect for sphere
                            IntersectResult result = new IntersectResult();
                            //go through the surface in a list
                            for (Intersectable intersectable : surfaceList) {
                                intersectable.intersect(ray, result);
                            }


                            // TODO: Objective 3: compute the shaded result for the intersection point (perhaps requiring shadow rays)
                            //ambient, diffuse Lambertian, and Blinn-Phong specular illumination models
                            //add all of them together

                            //color in which we will accumulate everything
                            Color3f color = new Color3f();
                            color.add(render.bgcolor);
                            IntersectResult shadowResult = new IntersectResult();
                            //for in shadows

                            Ray shadowRay = new Ray();
                            // shadowResult.t = Double.POSITIVE_INFINITY;
                            Color3f tempAmbient = new Color3f();

                            if (result.t < Double.POSITIVE_INFINITY && result.material != null) {
                                Color3f diffuse = new Color3f();
                                Color3f specular = new Color3f();
                                //ambient color
                                color.set(ambient.x * result.material.diffuse.x, ambient.y * result.material.diffuse.y, ambient.z * result.material.diffuse.z);
                                for (Map.Entry<String, Light> e : lights.entrySet()) {
                                    //shadowray computation section
                                    //calculate for shadow rays as in the ray tracing slides
                                    shadowRay.viewDirection.sub(e.getValue().from, result.p);
                                    shadowRay.eyePoint = new Point3d(result.p.x + shadowRay.viewDirection.x * 1e-9,
                                            result.p.y + shadowRay.viewDirection.y * 1e-9,
                                            result.p.z + shadowRay.viewDirection.z * 1e-9
                                    );


                                    //final IntersectResult result, final Light light, final SceneNode root, IntersectResult shadowResult, Ray shadowRay
                                    if (Scene.inShadow(result, e.getValue(), surfaceList, shadowResult, shadowRay))
                                        continue;

                                    //color accumulation calculations
                                    //for lambertian shading, we require the normal and the l
                                    //power = I
                                    Vector3d tempNorm = new Vector3d(result.n);
                                    Vector3d tempL = new Vector3d(e.getValue().from);
                                    tempL.sub(result.p);
                                    tempL.normalize();
                                    diffuse.set(result.material.diffuse.x * e.getValue().color.x, result.material.diffuse.y * e.getValue().color.y,
                                            result.material.diffuse.z * e.getValue().color.z);

                                    //Ld = kd * I * max(0, n dot l)
                                    double nL = Math.max(0, tempNorm.dot(tempL));
                                    double iTimesNL = e.getValue().power * nL;

                                    diffuse.scale((float) iTimesNL);

                                    //for blinn-phong need h (bisector) and normal
                                    // h = v + l / ||v + l||
                                    Vector3d tempV = new Vector3d(ray.viewDirection);
                                    tempV.scale(-1);
                                    tempV.normalize();

                                    Vector3d tempH = new Vector3d();
                                    Vector3d tempLight = new Vector3d(e.getValue().from);
                                    tempLight.sub(result.p);
                                    tempLight.normalize();

                                    tempH.add(tempV, tempLight);
                                    tempH.normalize();

                                    double maxNL = Math.max(0, tempNorm.dot(tempH));
                                    double ImaxNLPower = e.getValue().power * Math.pow(maxNL, result.material.shinyness);
                                    specular.set(result.material.specular.x * e.getValue().color.x, result.material.specular.y * e.getValue().color.y,
                                            result.material.specular.z * e.getValue().color.z);
                                    specular.scale((float) ImaxNLPower);

                                    //add lambertian and specular components into color (ambient already added)
                                    color.add(diffuse);
                                    color.add(specular);
                                }
                            }
                            //end of color accumulation

                            //set the new accumulated colors
                            c.set(color);
                        }
                    }
                    //clamp colors
                    c.clamp(0, 1);
                    int r = (int) (255 * c.x);
                    int g = (int) (255 * c.y);
                    int b = (int) (255 * c.z);
                    int a = 255;
                    int argb = (a << 24 | r << 16 | g << 8 | b);
                    // update the render image
                    render.setPixel(i, j, argb);
                }
            }

            // save the final render image
            render.save();

            // wait for render viewer to close
            render.waitDone();
        }else{
            for (int j = 0; j < h && !render.isDone(); j++) {
                for (int i = 0; i < w && !render.isDone(); i++) {
                    // TODO: Objective 1: generate a ray (use the generateRay method)
                    //create tempOffset
                    //double[] tempOffset = new double[2];
                            tempOffset[0] = 0.55;
                            tempOffset[1] = 0.55;
                            Ray ray = new Ray();
                            /*
                            //depth of field implementation
                            if(cam.isBlurred){
                                generateRay(i, j, tempOffset, cam, ray);
                                double angle = Math.toRadians(cam.fovy);
                                double tempH = Math.tan(angle/2.0);
                                double ratio = w/h;
                                double camHeight = 2.0 *tempH;
                                double camWidth = ratio * camHeight;

                                Vector3d wBasis = new Vector3d(cam.from.x - cam.to.x, cam.from.y-cam.to.y,cam.from.z-cam.from.z);
                                wBasis.normalize();
                                Vector3d uBasis = new Vector3d();
                                uBasis.cross(cam.up, wBasis);
                                uBasis.normalize();
                                Vector3d vBasis = new Vector3d();
                                vBasis.cross(wBasis, uBasis);

                                Vector3d origin = new Vector3d(cam.from.x, cam.from.y, cam.from.z);
                                Vector3d horizontal = new Vector3d(uBasis);
                                horizontal.scale(cam.focalDistance * camWidth);
                                Vector3d vertical = new Vector3d(vBasis);
                                vertical.scale(cam.focalDistance * camHeight);
                                //lowerleft = origin - horizontal/2 - vertical/2 - focus distance*wbasis
                                Vector3d lowerLeft = new Vector3d(origin);
                                Vector3d horOver2 = new Vector3d(horizontal);
                                horOver2.scale(0.5);
                                Vector3d verOver2 = new Vector3d(vertical);
                                verOver2.scale(0.5);
                                Vector3d wMulFD = new Vector3d(wBasis);
                                wMulFD.scale(cam.focalDistance);
                                lowerLeft.sub(horOver2);
                                lowerLeft.sub(verOver2);
                                lowerLeft.sub(wMulFD);
                                double radius = cam.apeture/2.0;

                                double random3 = ThreadLocalRandom.current().nextDouble(-1, 1);
                                double random4 = ThreadLocalRandom.current().nextDouble(-1, 1);

                                Point3d rand = new Point3d(random3, random4, 0);

                                Point3d newRand = new Point3d(rand.x * radius, rand.y * radius, rand.z * radius);

                                Vector3d lensOffsetU = new Vector3d(uBasis.x * newRand.x, uBasis.y * newRand.x, uBasis.z * newRand.x);
                                Vector3d lensOffsetV = new Vector3d(vBasis.x * newRand.y, vBasis.y * newRand.y, vBasis.z * newRand.z);
                                Vector3d finalOffset = new Vector3d();
                                finalOffset.add(lensOffsetU, lensOffsetV);

                                Point3d tempEyePoint = ray.eyePoint;
                                Vector3d tempViewDirection = ray.viewDirection;
                                ray.eyePoint = new Point3d(origin.x + finalOffset.x, origin.y + finalOffset.y, origin.z + finalOffset.z);
                                ray.viewDirection = new Vector3d(
                                  lowerLeft.x + tempEyePoint.x*horizontal.x + tempViewDirection.x*vertical.x - origin.x - finalOffset.x,
                                        lowerLeft.y + tempEyePoint.y*horizontal.y + tempViewDirection.y * vertical.y - origin.y -finalOffset.y,
                                        lowerLeft.z + tempEyePoint.z*horizontal.z + tempViewDirection.z * vertical.z - origin.z - finalOffset.z
                                );
                            }

                            else {
*/
                                //ray
                                //j i per pixel use uniform sampling offered by java between 0 to 1
                                generateRay(i, j, tempOffset, cam, ray);
                            //}
                            // TODO: Objective 2: test for intersection with scene surfaces
                            // for(Ray tempRay: rayList) {
                            //test intersect for sphere
                            IntersectResult result = new IntersectResult();
                            //go through the surface in a list
                            for (Intersectable intersectable : surfaceList) {
                                intersectable.intersect(ray, result);
                            }


                            // TODO: Objective 3: compute the shaded result for the intersection point (perhaps requiring shadow rays)
                            //ambient, diffuse Lambertian, and Blinn-Phong specular illumination models
                            //add all of them together

                            //color in which we will accumulate everything
                            Color3f color = new Color3f();
                            color.add(render.bgcolor);
                            IntersectResult shadowResult = new IntersectResult();
                            //for in shadows

                            Ray shadowRay = new Ray();
                            // shadowResult.t = Double.POSITIVE_INFINITY;
                            Color3f tempAmbient = new Color3f();

                            if (result.t < Double.POSITIVE_INFINITY && result.material != null) {
                                Color3f diffuse = new Color3f();
                                Color3f specular = new Color3f();
                                //ambient color
                                color.set(ambient.x * result.material.diffuse.x, ambient.y * result.material.diffuse.y, ambient.z * result.material.diffuse.z);
                                for (Map.Entry<String, Light> e : lights.entrySet()) {
                                    //shadowray computation section
                                    //calculate for shadow rays as in the ray tracing slides
                                    shadowRay.viewDirection.sub(e.getValue().from, result.p);
                                    shadowRay.eyePoint = new Point3d(result.p.x + shadowRay.viewDirection.x * 1e-9,
                                            result.p.y + shadowRay.viewDirection.y * 1e-9,
                                            result.p.z + shadowRay.viewDirection.z * 1e-9
                                    );


                                    //final IntersectResult result, final Light light, final SceneNode root, IntersectResult shadowResult, Ray shadowRay
                                    if (Scene.inShadow(result, e.getValue(), surfaceList, shadowResult, shadowRay))
                                        continue;

                                    //color accumulation calculations
                                    //for lambertian shading, we require the normal and the l
                                    //power = I
                                    Vector3d tempNorm = new Vector3d(result.n);
                                    Vector3d tempL = new Vector3d(e.getValue().from);
                                    tempL.sub(result.p);
                                    tempL.normalize();
                                    diffuse.set(result.material.diffuse.x * e.getValue().color.x, result.material.diffuse.y * e.getValue().color.y,
                                            result.material.diffuse.z * e.getValue().color.z);

                                    //Ld = kd * I * max(0, n dot l)
                                    double nL = Math.max(0, tempNorm.dot(tempL));
                                    double iTimesNL = e.getValue().power * nL;

                                    diffuse.scale((float) iTimesNL);

                                    //for blinn-phong need h (bisector) and normal
                                    // h = v + l / ||v + l||
                                    Vector3d tempV = new Vector3d(ray.viewDirection);
                                    tempV.scale(-1);
                                    tempV.normalize();

                                    Vector3d tempH = new Vector3d();
                                    Vector3d tempLight = new Vector3d(e.getValue().from);
                                    tempLight.sub(result.p);
                                    tempLight.normalize();

                                    tempH.add(tempV, tempLight);
                                    tempH.normalize();

                                    double maxNL = Math.max(0, tempNorm.dot(tempH));
                                    double ImaxNLPower = e.getValue().power * Math.pow(maxNL, result.material.shinyness);
                                    specular.set(result.material.specular.x * e.getValue().color.x, result.material.specular.y * e.getValue().color.y,
                                            result.material.specular.z * e.getValue().color.z);
                                    specular.scale((float) ImaxNLPower);

                                    //add lambertian and specular components into color (ambient already added)
                                    color.add(diffuse);
                                    color.add(specular);


                                }
                            }
                            //end of color accumulation
                            //set the new accumulated colors
                            c.set(color);
                    //clamp colors
                    c.clamp(0, 1);
                    int r = (int) (255 * c.x);
                    int g = (int) (255 * c.y);
                    int b = (int) (255 * c.z);
                    int a = 255;
                    int argb = (a << 24 | r << 16 | g << 8 | b);
                    // update the render image
                    render.setPixel(i, j, argb);
                }
            }

            // save the final render image
            render.save();

            // wait for render viewer to close
            render.waitDone();
        }
    }

    /**
     * Generate a ray through pixel (i,j).
     *
     * @param i The pixel row.
     * @param j The pixel column.
     * @param offset The offset from the center of the pixel, in the range [-0.5,+0.5] for each coordinate.
     * @param cam The camera.
     * @param ray Contains the generated ray.
     */
	public static void generateRay(final int i, final int j, final double[] offset, final Camera cam, Ray ray) {

		// TODO: Objective 1: generate rays given the provided parmeters

       // System.out.println(offset[0]);
        //System.out.println(offset[1]);
       // System.out.println(offset[2]);

        Point3d e = cam.from;
        double ny = cam.imageSize.getHeight();
        double nx = cam.imageSize.getWidth();
        double imageRatio =   (double) cam.imageSize.width/ (double) cam.imageSize.height;

        Vector3d viewDirection = new Vector3d();

        //w = -z plane -> eye i.e. camera
        Vector3d w = new Vector3d(cam.from.x - cam.to.x, cam.from.y - cam.to.y, cam.from.z - cam.to.z);
        w.normalize();
        //perform cross product to get u. We know the up vector is the v for the ray.
        Vector3d u = new Vector3d();
        u.cross(cam.up, w);
        u.normalize();
        Vector3d v = new Vector3d();
        v.cross(u, w);
        v.normalize();

        //our near plane will be given by the the distance between camera from to lookat
        Vector3d near = new Vector3d(cam.from.x - cam.to.x, cam.from.y - cam.to.y, cam.from.z - cam.to.z);

        double top = (Math.tan(Math.toRadians(cam.fovy/2.0))) * near.length();
        double bottom = -top;
        double right = top*imageRatio;
        double left = -(top*imageRatio);

        //use formula learned in class to generate coordinate pixels u and v
        double pixelCoordU = left + (right-left)*(i+0.5+offset[0])/nx;
        double pixelCoordV = bottom + (top-bottom)*(j+0.5+offset[1])/ny;

        //compute for vector s since it controls the ray s = e + uu + vv - dw
        //then compute for d = s - e
        Vector3d tempE = new Vector3d(cam.from);

        Vector3d tempU = new Vector3d(u);
        tempU.scale(pixelCoordU);

        Vector3d tempV = new Vector3d(v);
        tempV.scale(pixelCoordV);

        Vector3d tempW = new Vector3d(w);
        tempW.scale(near.length());

        Vector3d computation = new Vector3d(tempE);
        computation.add(tempU);
        computation.add(tempV);
        computation.sub(tempW);

        Vector3d s = new Vector3d(computation);
        s.sub(tempE);

        s.normalize();

        ray.set(e, s);


	}

	/**
	 * Shoot a shadow ray in the scene and get the result.
	 *
	 * @param result Intersection result from raytracing.
	 * @param light The light to check for visibility.
	 * @param shadowResult Contains the result of a shadow ray test.
	 * @param shadowRay Contains the shadow ray used to test for visibility.
	 *
	 * @return True if a point is in shadow, false otherwise.
	 */
	public static boolean inShadow(final IntersectResult result, final Light light,
                                   List<Intersectable> surfaceList, IntersectResult shadowResult, Ray shadowRay) {
		// TODO: Objective 5: check for shdows and use it in your lighting computation
        //for each intersecting elements, determine if ray lands on shadows or not
        shadowResult.t = Double.POSITIVE_INFINITY;
        for(Intersectable intersectable: surfaceList){
            intersectable.intersect(shadowRay, shadowResult);
            if(shadowResult.t != Double.POSITIVE_INFINITY){
                //mean it intersected shadow
                return true;
            } else{
                return false;
            }
        }

        return false;
	}

    public static boolean isPowerOfTwo(int number) {
        return number > 0 && ((number & (number - 1)) == 0);
    }
}

