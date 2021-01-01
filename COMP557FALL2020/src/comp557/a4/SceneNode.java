package comp557.a4;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * The scene is constructed from a hierarchy of nodes, where each node
 * contains a transform, a material definition, some amount of geometry, 
 * and some number of children nodes.  Each node has a unique name so that
 * it can be instanced elsewhere in the hierarchy (provided it does not 
 * make loops. 
 * 
 * Note that if the material (inherited from Intersectable) for a scene 
 * node is non-null, it should override the material of any child.
 * 
 */
//260803947 steven lee
public class SceneNode extends Intersectable {
	
	/** Static map for accessing scene nodes by name, to perform instancing */
	public static Map<String,SceneNode> nodeMap = new HashMap<String,SceneNode>();
	
    public String name;
   
    /** Matrix transform for this node */
    public Matrix4d M;
    
    /** Inverse matrix transform for this node */
    public Matrix4d Minv;
    
    /** Child nodes */
    public List<Intersectable> children;
    
    /**
     * Default constructor.
     * Note that all nodes must have a unique name, so that they can used as an instance later on.
     */
    public SceneNode() {
    	super();
    	this.name = "";
    	this.M = new Matrix4d();
    	this.Minv = new Matrix4d();
    	this.children = new LinkedList<Intersectable>();
    }
    
    private IntersectResult tmpResult = new IntersectResult();
    
    private Ray tmpRay = new Ray();
    
    @Override
    public void intersect(Ray ray, IntersectResult result) {
    	tmpRay.eyePoint.set(ray.eyePoint);
    	tmpRay.viewDirection.set(ray.viewDirection);
    	Minv.transform(tmpRay.eyePoint);
    	Minv.transform(tmpRay.viewDirection);
    	tmpResult.t = Double.POSITIVE_INFINITY;
    	//the setting of this specific normal did not work for me (it kept flipping the plane, but i believe it would be related to my ray call)
    	tmpResult.n.set(0, 1, 0);
        for ( Intersectable s : children ) {
            s.intersect( tmpRay, tmpResult );
        }
        if ( tmpResult.t > 1e-9 && tmpResult.t < result.t ) {
        	// TODO: do something useful here!
            //homogenous transform
           // Point3d parametricLineScaledByT = new Point3d(tmpRay.viewDirection);
           // parametricLineScaledByT.scale(tmpResult.t);
            // If the material of the intersection result is null, then the material of the scene node should be assigned to the result.
            result.material = tmpResult.material;
            result.t = tmpResult.t;
            tmpRay.getPoint(result.t, result.p);
            Vector3d tempNormal = new Vector3d(tmpResult.n.x, tmpResult.n.y, tmpResult.n.z);
            result.n = tempNormal;
            result.n.normalize();
            M.transform(result.p);
            M.transform(result.n);
        }
        else{
            // If the material of the intersection result is null,
            // then the material of the scene node should be assigned to the result
            if(result.material == null){
                result.material = material;
            }
        }
       // M.transform(tmpRay.viewDirection);
       // M.transform(tmpRay.eyePoint);
    }

}
