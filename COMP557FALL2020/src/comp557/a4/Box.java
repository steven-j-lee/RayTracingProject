package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple box class. A box is defined by it's lower (@see min) and upper (@see max) corner. 
 */
//260803947 steven lee
public class Box extends Intersectable {

	public Point3d max;
	public Point3d min;
	
    /**
     * Default constructor. Creates a 2x2x2 box centered at (0,0,0)
     */
    public Box() {
    	super();
    	this.max = new Point3d( 1, 1, 1 );
    	this.min = new Point3d( -1, -1, -1 );
    }	

	@Override
	public void intersect(Ray ray, IntersectResult result) {
		// TODO: Objective 6: intersection of Ray with axis aligned box
		//gamedev math theory
		//https://www.youtube.com/watch?v=USjbg5QXk3g&ab_channel=JorgeRodriguez
		//compute for t value minimum and max for each axis
		double txMin = (min.x - ray.eyePoint.x)/ray.viewDirection.x;
		double tyMin =  (min.y - ray.eyePoint.y)/ray.viewDirection.y;
		double txMax =  (max.x - ray.eyePoint.x)/ray.viewDirection.x;
		double tyMax = (max.y - ray.eyePoint.y)/ray.viewDirection.y;
		double tzMin = (min.z - ray.eyePoint.z)/ray.viewDirection.z;
		double tzMax = (max.z - ray.eyePoint.z)/ray.viewDirection.z;

		//minimum test with z
		double txLow = Math.min(txMin, txMax);
		double txHigh = Math.max(txMin, txMax);
		double tyLow = Math.min(tyMin, tyMax);
		double tyHigh = Math.max(tyMin, tyMax);
		double tzLow = Math.min(tzMin, tzMax);
		double tzHigh = Math.max(tzMin, tzMax);

		//take closest as possible for minimum, and same for maximum
		double tMin = Math.max(Math.max(txLow, tyLow), tzLow);
		double tMax = Math.min(Math.min(txHigh, tyHigh), tzHigh);

		//else no intersection exists
		if(tMin < tMax && tMin > 1e-9){
			//assign points along parametric curve
			result.t = tMin;
			ray.getPoint(result.t, result.p);
			result.material = material;
			//do the same as in sphere substract to obtain the normal
			//need to define normal depending on which section it's at (ex: on x, or -x, y or -y etc)
			if(Math.abs(result.p.x - max.x) < 1e-9){
				result.n = new Vector3d(1,0,0);
			} else if(Math.abs(result.p.x - min.x) <1e-9){
				result.n = new Vector3d(-1, 0,0);
			} else if(Math.abs(result.p.y - max.y) <1e-9 ){
				result.n = new Vector3d(0, 1,0);
			} else if(Math.abs(result.p.y - min.y) < 1e-9){
				result.n = new Vector3d(0,-1,0);
			} else if(Math.abs(result.p.z - max.z) < 1e-9){
				result.n = new Vector3d(0,0,1);
			} else if(Math.abs(result.p.z - min.z) < 1e-9){
				result.n = new Vector3d(0,0,-1);
			}

			/*
			if(result.p.x - max.x < 1e-9){
				result.n = new Vector3d(1,0,0);
			} else if(result.p.x - min.x<1e-9){
				result.n = new Vector3d(-1, 0,0);
			} else if(result.p.y - max.y <1e-9 ){
				result.n = new Vector3d(0, 1,0);
			} else if(result.p.y - min.y) < 1e-9){
				result.n = new Vector3d(0,-1,0);
			} else if(result.p.z - max.z) < 1e-9){
				result.n = new Vector3d(0,0,1);
			} else if(result.p.z - min.z) < 1e-9){
				result.n = new Vector3d(0,0,-1);
			}
			 */
		}

	}



}
