package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Class for a plane at y=0.
 * 
 * This surface can have two materials.  If both are defined, a 1x1 tile checker 
 * board pattern should be generated on the plane using the two materials.
 */
//260803947 steven lee
public class Plane extends Intersectable {
    
	/** The second material, if non-null is used to produce a checker board pattern. */
	Material material2;
	
	/** The plane normal is the y direction */
	public static final Vector3d n = new Vector3d( 0, 1, 0 );

    /**
     * Default constructor
     */
    public Plane() {
    	super();
    }

        
    @Override
    public void intersect( Ray ray, IntersectResult result ) {

        // TODO: Objective 4: intersection of ray with plane
        //https://www.cs.cornell.edu/courses/cs4620/2013fa/lectures/03raytracing1.pdf
        //ray's parametric equation: p = eye point + t * view direction
        //Plane's equation: P dot Normal + d = 0
        //constant d is at 0 since plane at y = 0
        //we get t = -p0 dot n/v dot n
        //since only the y point is affected by the dot product, we can divide -p0 by view direction
        Vector3d v = new Vector3d(ray.viewDirection);
        Vector3d p = new Vector3d(ray.eyePoint);
        p.scale(-1);
        double t1 = n.dot(p) / v.dot(n);


        if (t1 < result.t && t1 > 1e-9) {
            result.t = t1;
            ray.getPoint(t1, result.p);
            result.n = n;
            result.material = material;

            if (material2 != null) {
                //check for -x -z, x z, -x z, x -z
                int roundedPointX = Math.round((float) result.p.x);
                int roundedPointZ = Math.round((float) result.p.z);

                if (roundedPointX <= 0 && roundedPointZ <= 0 || roundedPointX > 0 && roundedPointZ > 0) {
                    if ((roundedPointX + roundedPointZ) % 2 == 0) {
                        result.material = material;
                    } else {
                        result.material = material2;
                    }
                    //all other conditions fit into here
                } else{
                    if ((roundedPointX + roundedPointZ) % 2 == 0){
                        //pair-wise
                        result.material = material;
                    } else {
                        result.material = material2;
                    }
                }
            }
        }
    }
}
