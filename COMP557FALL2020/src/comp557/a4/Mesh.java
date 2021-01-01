package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.HashMap;
import java.util.Map;
//260803947 steven lee
public class Mesh extends Intersectable {
	
	/** Static map storing all meshes by name */
	public static Map<String,Mesh> meshMap = new HashMap<String,Mesh>();
	
	/**  Name for this mesh, to allow re-use of a polygon soup across Mesh objects */
	public String name = "";
	
	/**
	 * The polygon soup.
	 */
	public PolygonSoup soup;

	public Mesh() {
		super();
		this.soup = null;
	}			
		
	@Override
	public void intersect(Ray ray, IntersectResult result) {

		// TODO: Objective 7: ray triangle intersection for meshes
		//do the same as slides
			for (int[] faceList : soup.faceList) {
				Vector3d a = new Vector3d(soup.vertexList.get(faceList[0]).p.x,
						soup.vertexList.get(faceList[0]).p.y,
						soup.vertexList.get(faceList[0]).p.z);
				Vector3d b = new Vector3d(soup.vertexList.get(faceList[1]).p.x,
						soup.vertexList.get(faceList[1]).p.y,
						soup.vertexList.get(faceList[1]).p.z);
				Vector3d c = new Vector3d(soup.vertexList.get(faceList[2]).p.x,
						soup.vertexList.get(faceList[2]).p.y,
						soup.vertexList.get(faceList[2]).p.z);

				Vector3d bMinusA = new Vector3d(b);
				bMinusA.sub(a);
				//System.out.println(distBA.x + " " + distBA.y + " " + distBA.z);
				Vector3d aMinusC = new Vector3d(a);
				aMinusC.sub(c);
				Vector3d cMinusB = new Vector3d(c);
				cMinusB.sub(b);

				Vector3d cMinusA = new Vector3d(c);
				cMinusA.sub(a);

				//bMinusA.normalize();
				//aMinusC.normalize();
				//cMinusB.normalize();

				//https://www.rose-hulman.edu/class/cs/csse451/examples/notes/present8.pdf
				//take the cross product to get a normal
				Vector3d normal = new Vector3d();
				normal.cross(bMinusA, cMinusA);
				normal.normalize();

				//derive t using the normal and ray viewdirection and ray eyepoint
				Vector3d v = new Vector3d(ray.viewDirection);
				Vector3d p = new Vector3d(a);
				p.sub(ray.eyePoint);

				double t  = normal.dot(p)/normal.dot(v);

				//find x
				Point3d x = new Point3d();
				//aline along parametric line
				ray.getPoint(t, x);

				Vector3d xMinusA = new Vector3d(x.x - a.x, x.y - a.y, x.z - a.z);
				Vector3d xMinusB = new Vector3d(x.x - b.x, x.y - b.y, x.z - b.z);
				Vector3d xMinusC = new Vector3d(x.x - c.x, x.y - c.y, x.z - c.z);

				xMinusA.normalize();
				xMinusB.normalize();
				xMinusC.normalize();

				Vector3d bMaCrossxMa = new Vector3d();
				bMaCrossxMa.cross(bMinusA, xMinusA);
				bMaCrossxMa.normalize();
				double bMaCrossxMaDotted = bMaCrossxMa.dot(normal);

				Vector3d cMbCrossxMb = new Vector3d();
				cMbCrossxMb.cross(cMinusB, xMinusB);
				cMbCrossxMb.normalize();
				double cMbCrossxMbDotted = cMbCrossxMb.dot(normal);

				Vector3d aMcCrossxMc = new Vector3d();
				aMcCrossxMc.cross(aMinusC, xMinusC);
				aMcCrossxMc.normalize();
				double aMcCrossxMcDotted = aMcCrossxMc.dot(normal);

				if(bMaCrossxMaDotted > 0 && cMbCrossxMbDotted > 0 && aMcCrossxMcDotted > 0 && t > 1e-9 && t < result.t){
					result.t = t;
					result.p = x;
					result.material = material;
					result.n = normal;
				}
		}
	}

}