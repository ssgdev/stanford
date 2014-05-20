package org.ssg.Stanford;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Vector2f;

public class StaticObject {

    public Vector2f pos;
    public int l;
    public Polygon p;
    public boolean coll;
    
    public StaticObject(int a, int b, int length){//a and b are coordinates
        pos = new Vector2f (a, b);
        l=length;
        p = new Polygon(new float[]{pos.x,pos.y,pos.x+l,pos.y,pos.x+l,pos.y+l,pos.x,pos.y+l});
    }    
    
    public void shiftDown(double d){
        pos.y+=d;
        p.setY(pos.y);
    }
    
    public void collided() {
    	coll = true;
    }
    
    public void render(Graphics g) {
    	
    }
    
}
