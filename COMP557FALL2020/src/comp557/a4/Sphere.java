package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.*;

/**
 * A simple sphere class.
 */
//260803947 steven lee
public class Sphere extends Intersectable {
    
	/** Radius of the sphere. */
	public double radius = 1;
    
	/** Location of the sphere center. */
	public Point3d center = new Point3d( 0, 0, 0 );
    
    /**
     * Default constructor
     */
    public Sphere() {
    	super();
    }
    
    /**
     * Creates a sphere with the request radius and center. 
     * 
     * @param radius
     * @param center
     * @param material
     */
    public Sphere( double radius, Point3d center, Material material ) {
    	super();
    	this.radius = radius;
    	this.center = center;
    	this.material = material;
    }
    
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
        // TODO: Objective 2: intersection of ray with sphere
        Vector3d rayDirection = ray.viewDirection;
        Vector3d rayOrigin = new Vector3d(ray.eyePoint.x, ray.eyePoint.y, ray.eyePoint.z);
        Vector3d sphereCenter = new Vector3d(center.x, center.y, center.z);
        //|Point-Center|^2 - R^2 = 0
        Vector3d distOriginCenter = new Vector3d(rayOrigin);
        distOriginCenter.sub(sphereCenter);
        //compute for quadratic equation to get t
        double a = rayDirection.dot(rayDirection);
        double b = (double)2 * distOriginCenter.dot(rayDirection);
        double c = distOriginCenter.dot(distOriginCenter) - (radius * radius);

        double discriminant = (b*b)-(4*a*c);

        double t0 = (-b - Math.sqrt(discriminant))/((double)2 * a);
        double t1 = (-b - Math.sqrt(discriminant))/((double)2 * a);

        if (discriminant >= 0.0){
            if(t0 < t1){
                if(t0 > 1e-9 && t0 < result.t){
                    //point for intersection = ray origin + (t * ray direction)
                    ray.getPoint(t0, result.p);
                    //normal = ray origin - sphere center
                    Vector3d normal = new Vector3d(result.p.x - sphereCenter.x, result.p.y - sphereCenter.y,
                        result.p.z - sphereCenter.z);
                    normal.normalize();
                    result.t = t0;
                    result.n = normal;
                    result.material = material;
                }
            } else {
           // double t0 = (-b + Math.sqrt(discriminant)) / ((double) 2 * a);
                if (t1 > 1e-9 && t1 < result.t) {
                    //point for intersection = ray origin + (t * ray direction)
                    ray.getPoint(t1, result.p);
                    //normal = ray origin - sphere center
                    Vector3d normal = new Vector3d(result.p.x - sphereCenter.x, result.p.y - sphereCenter.y,
                            result.p.z - sphereCenter.z);
                    normal.normalize();

                    result.t = t1;
                    result.n = normal;
                    result.material = material;
                 }
            }
        }
    }

}
