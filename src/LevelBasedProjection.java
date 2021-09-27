//package gui;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public interface LevelBasedProjection {
	public Point2D fromLLtoPixel(Point2D point, int zoomLevel);
	public Point2D fromPixelToLL(Point2D point, int zoomLevel);
	public int     minZoomLevel();
	public int     maxZoomLevel();
	
	public LevelBasedProjection WEBMERCATOR = new WebMercartorProjection(); 
}


class WebMercartorProjection implements LevelBasedProjection{

	private class Constants{
		double Bc;
		double Cc;
		double Zc;
		
		public Constants(double bc, double cc, double zc) {
			super();
			Bc = bc;
			Cc = cc;
			Zc = zc;

		}	
	}
	
	private List<Constants> constants = new ArrayList<Constants>();

	
	public WebMercartorProjection() {
	    double c = 256;
	    for(int i=minZoomLevel(); i < maxZoomLevel()+1; ++i){
		        double e = c/2;
		        constants.add(new Constants(c/360.0, c/(2*Math.PI), e));
		        c *=2;
		}
	}
	
    private double minmax (double a, double b, double c){
        return Math.min(Math.max(a,b),c);
    }
	
	@Override
	public Point2D fromLLtoPixel(Point2D point, int zoomLevel) {
		Constants C = this.constants.get(zoomLevel);
	
	    double  d = C.Zc;
	    double  e = d + point.getX() * C.Bc;
	    double  f = minmax(Math.sin(Math.PI/180.0 * point.getY()),-0.9999,0.9999);
	    double  g = d + 0.5*Math.log((1+f)/(1-f))*-C.Cc;

	    return new Point2D.Double(e,g);
	}


	public Point2D fromPixelToLL(Point2D point, int zoom){
		Constants C = this.constants.get(zoom);
	    double  e = C.Zc;
	    double  f = (point.getX() - e)/C.Bc;
	    double  g = (point.getY() - e)/-C.Cc;
	    double  h = 180.0/Math.PI * ( 2 * Math.atan(Math.exp(g)) - 0.5 * Math.PI);
	    return new Point2D.Double(f,h);
	}

	@Override
	public int minZoomLevel() {
		return 0;
	}

	@Override
	public int maxZoomLevel() {
		return 18;
	}

	
	
}








