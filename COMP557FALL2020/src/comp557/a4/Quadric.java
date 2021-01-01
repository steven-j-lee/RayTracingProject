package comp557.a4;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

//260803947 steven lee
public class Quadric extends Intersectable {
    
	/**
	 * Radius of the sphere.
	 */
	public Matrix4d Q = new Matrix4d();
	public Matrix3d A = new Matrix3d();
	public Vector3d B = new Vector3d();
	public double C;
	
	/**
	 * The second material, e.g., for front and back?
	 */
	Material material2 = null;
	
	public Quadric() {
	
	}
	
	@Override
	public void intersect(Ray ray, IntersectResult result) {
	//https://people.cs.clemson.edu/~dhouse/courses/405/notes/quadrics.pdf
		// x= p + tu
		// p = eyepoint
		// u = view direction
		Vector4d homogenousU = new Vector4d();
		//use Q for homogenous coordinates
		Vector4d homogenousViewDirection = new Vector4d(ray.viewDirection.x, ray.viewDirection.y, ray.viewDirection.z,1);
		Q.transform(homogenousViewDirection, homogenousU);


		Vector4d homogenousP = new Vector4d();
		//use Q for homogenous coordinates
		Vector4d homogenousEyePoint = new Vector4d(ray.eyePoint.x, ray.eyePoint.y, ray.eyePoint.z, 1);
		Q.transform(homogenousEyePoint, homogenousP);


		//a = utQu
		double a = homogenousViewDirection.dot(homogenousU);
		//c = ptQp
		double c = homogenousEyePoint.dot(homogenousP);
		//b = utQp
		double b = homogenousViewDirection.dot(homogenousP);
		//discriminant = b^2 - ac
		double discriminant = (b*b)-(a*c);

		double t0 = (-b - Math.sqrt(discriminant))/a;
		double t1 = (-b + Math.sqrt(discriminant))/a;

		//check if discriminant is bigger than 0 like in spheres
		if(discriminant >= 0.0){
			//check if t0 smaller than t1 like in spheres
			if(t0 < t1){
				if(t0 > 1e-9 && t0 < result.t){
					result.t = t0;
					ray.getPoint(t0, result.p);
					result.material = material;
					//normal = 2AX - 2bt
					A.transform(result.p, result.n);
					result.n.scale(2);
					B.scale(2);
					result.n.sub(B);
					result.n.normalize();
				}
			} else{
				if(t1 > 1e-9 && t1 < result.t){
				result.t = t1;
				ray.getPoint(t1, result.p);
				result.material = material;
				//normal = 2AX - 2bt
				A.transform(result.p, result.n);
				result.n.scale(2);
				B.scale(2);
				result.n.sub(B);
				result.n.normalize();
			}
			}
		}

	}
	
}
