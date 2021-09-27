

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
//import javafx.geometry.Point2D;

import javax.swing.JFrame;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import math.geom2d.Vector2D;


public class Polyline {
	public static void main(String[] args) throws Exception{
		
		//Display of the original contours
		ArrayList<ArrayList<Point2D>> originalContours = importFromFile("contour.csv");
		
		SimpleGraphicsView display1 = new SimpleGraphicsView();
		JFrame frame1 = new JFrame("Test1");
		frame1.setSize(400,400);
		frame1.add(display1);
		
		for (int i = 0; i < originalContours.size(); i++){ 
			Path2D pl = new Path2D.Double();
			ArrayList<Point2D> SingleContour = originalContours.get(i);
			pl.moveTo(SingleContour.get(0).getX(),SingleContour.get(0).getY());

			for (int j = 1; j < SingleContour.size(); j++){
				pl.lineTo(SingleContour.get(j).getX(),SingleContour.get(j).getY());
				display1.addShape(pl, Color.RED, 3);
			}
		//System.out.print("Total number of points at this loop : " + count + "\n" + "\n");
		}
		
		display1.moveToCenter();
		frame1.setVisible(true);
		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		//Display of the optimised contours
		ArrayList<DiGraph<Point2D, Line2D>> optimizedContours = optimization(lineSimplifier(originalContours,5),10);
		
		SimpleGraphicsView display2 = new SimpleGraphicsView();
		JFrame frame2 = new JFrame("Test2");
		frame2.setSize(400,400);
		frame2.add(display2);
		
		for (int w = 0; w < optimizedContours.size(); w++) {
			DiGraph<Point2D, Line2D> simplifiedContour = optimizedContours.get(w);
			for (int q = 0; q < simplifiedContour.m(); q++) {
				Line2D arc = simplifiedContour.getArc(q).getArcData();
				display2.addShape(arc,Color.MAGENTA,3);
			}
		}
		
		display2.moveToCenter();
		frame2.setVisible(true);
		frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	
	

	public static ArrayList<ArrayList<Point2D>> importFromFile(String filename) throws Exception{
	//public static void main(String[] args) throws FileNotFoundException{	

		ArrayList<ArrayList<Point2D>> contours = new ArrayList<ArrayList<Point2D>>();
		Scanner s = new Scanner(new File(filename));
		//Scanner s = new Scanner(new File("contours (new).csv"));
		s.nextLine();

		while (s.hasNextLine()) {
			String first = s.nextLine();
			String[] t = first.split("\""); 
			//System.out.print(t[1]);
			StringTokenizer k = new StringTokenizer(t[1],"MULTILINESTRING, ()");

			ArrayList<Point2D> polyline = new ArrayList<Point2D>();

			while(k.hasMoreTokens()) {
				Double x = Double.parseDouble(k.nextToken());
				Double y = Double.parseDouble(k.nextToken()); 
				Point2D p = LevelBasedProjection.WEBMERCATOR.fromLLtoPixel(new Point2D.Double(x, y), 12);
				polyline.add(p);
				
			}
			
			contours.add(polyline);

			
			//System.out.println("first" + contours.get(2));
			//System.out.println(contours.size());
			//System.out.println("second" + contours.get(1));


			//StringTokenizer l = new StringTokenizer(t[4],",");
			//Double elev = Double.parseDouble(l.nextToken());
			//System.out.print(elev + "\n");
			//return contours;


		}s.close();
		
		//System.out.println("Polyline" + contours);
		//System.out.println("Number of contours: " + contours.size());
		
		/*SimpleGraphicsView display = new SimpleGraphicsView();
		JFrame frame = new JFrame("Test");
		frame.setSize(400,400);
		frame.add(display);
		
		for (int i = 0; i < contours.size(); i++){ 
			Path2D pl = new Path2D.Double();
			ArrayList<Point2D> SingleContour = contours.get(i);
			pl.moveTo(SingleContour.get(0).getX(),SingleContour.get(0).getY());

			for (int j = 1; j < SingleContour.size(); j++){
				pl.lineTo(SingleContour.get(j).getX(),SingleContour.get(j).getY());
				display.addShape(pl, Color.RED, 3);
			}
		//System.out.print("Total number of points at this loop : " + count + "\n" + "\n");
		}
		
		display.moveToCenter();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);*/
		return contours;

	}
	
	
	
	public static ArrayList<DiGraph<Point2D, Line2D>> lineSimplifier(ArrayList<ArrayList<Point2D>> contours, double errorTolerance) {
		ArrayList<DiGraph<Point2D, Line2D>> groupofshortcutGraphs = new ArrayList<DiGraph<Point2D, Line2D>>() ;

		for (int f = 0; f < contours.size(); f++) {
			ArrayList<Point2D> contour = contours.get(f);
			DiGraph<Point2D,Line2D> shortcutGraph = new DiGraph<>();
			
			for (int g = 0; g < contour.size(); g++) {
				shortcutGraph.addNode(contour.get(g));
			}

			for (int h = 0; h < shortcutGraph.n()-1; h++) {
				for (int i = h+1; i < shortcutGraph.n(); i++ ) {
					Line2D lineSegment = new Line2D.Double(shortcutGraph.getNodeData(h), shortcutGraph.getNodeData(i));

					boolean add2Graph = true;
					for (int k = h + 1; k < i; k++ ) {
						double dist = lineSegment.ptLineDist(shortcutGraph.getNodeData(k));
						if (dist > errorTolerance) {
							add2Graph = false;
						}
					}
					if (add2Graph) {
						shortcutGraph.addArc(shortcutGraph.getNode(h), shortcutGraph.getNode(i),lineSegment);
					}	
				}
			}
			groupofshortcutGraphs.add(shortcutGraph);
		}
		//System.out.println(groupofshortcutGraphs.size());
		return groupofshortcutGraphs;

	}
	
	
	
	public static int getEdgeVarIndex(ArrayList<DiGraph.DiGraphArc<Point2D, Line2D>> edges, DiGraph.DiGraphArc<Point2D, Line2D> edge) {
		int ind = 0;
		for (int i = 0; i < edges.size(); i++) {
			if(edges.get(i).equals(edge)) {
				ind = i;
			}
		}
		return ind;
	}
	
	public static int getVertexVarIndex(ArrayList<DiGraph.DiGraphNode<Point2D, Line2D>> vertices, DiGraph.DiGraphNode<Point2D, Line2D> vertex) {
		int ind = 0;
		for (int i = 0; i < vertices.size(); i++) {
			if(vertices.get(i).equals(vertex)) {
				ind = i;
			}
		}
		return ind;
	}
	
	
	
	public static ArrayList<DiGraph<Point2D, Line2D>> optimization(ArrayList<DiGraph<Point2D, Line2D>> groupofshortcutGraphs, double thresholdAngle) throws GRBException,NullPointerException {
		ArrayList<DiGraph<Point2D, Line2D>> optimizedGraphs = new ArrayList<DiGraph<Point2D, Line2D>> ();

		ArrayList<DiGraph.DiGraphArc<Point2D, Line2D>> edgesList = new ArrayList<DiGraph.DiGraphArc<Point2D, Line2D>> ();
		ArrayList<Integer> indexesofEdges = new ArrayList<Integer> ();
		
		ArrayList<DiGraph.DiGraphNode<Point2D, Line2D>> verticesList = new ArrayList<DiGraph.DiGraphNode<Point2D, Line2D>> ();
		ArrayList<Integer> indexesofVertices = new ArrayList<Integer> ();
		
		
		for (int i = 0; i < groupofshortcutGraphs.size(); i++) {
			DiGraph<Point2D, Line2D> edges = groupofshortcutGraphs.get(i);
			for (int j = 0; j < edges.m(); j++) {
				DiGraph.DiGraphArc<Point2D, Line2D> edge = edges.getArc(j); 
				edgesList.add(edge);
				indexesofEdges.add(i);
			}
		}
		
		for (int u = 0; u < groupofshortcutGraphs.size(); u++) {
			DiGraph<Point2D, Line2D> vertices = groupofshortcutGraphs.get(u);
			for (int z = 0; z < vertices.n(); z++) {
				DiGraph.DiGraphNode<Point2D, Line2D> vertex = vertices.getNode(z); 
				verticesList.add(vertex);
				indexesofVertices.add(u);
			}
		}

		
		//The Gurobi model
		GRBModel grbModel = new GRBModel(new GRBEnv());

		//Variables to be solved
		/// Edge variables
		List<GRBVar> edgeVariables = new ArrayList<GRBVar>();
		
		for (int s = 0; s < edgesList.size(); s++) {
			edgeVariables.add(grbModel.addVar(0,1,0, GRB.BINARY, "x ("+ s +")")) ;
		}
		grbModel.update();
		
		/// Vertex variables
		List<GRBVar> vertexVariables = new ArrayList<GRBVar>();
		
		for (int s = 0; s < verticesList.size(); s++) {
			vertexVariables.add(grbModel.addVar(0,1,0, GRB.BINARY, "y ("+ s +")")) ;
		}
		grbModel.update();


		//Constraints for the optimisation problem
		for (int i = 0; i < groupofshortcutGraphs.size(); i++) {
			DiGraph<Point2D, Line2D> simplifiedContour = groupofshortcutGraphs.get(i);
			
			///Constraint for the start vertex 
			List<DiGraph.DiGraphArc<Point2D, Line2D>> outGoingEdgesFromStVx = simplifiedContour.getNode(0).getOutgoingArcs();
			GRBLinExpr exp = new GRBLinExpr();
			for (int g = 0; g < outGoingEdgesFromStVx.size() ; g++) {
				int varIndex = getEdgeVarIndex(edgesList, outGoingEdgesFromStVx.get(g));
				exp.addTerm(1,edgeVariables.get(varIndex));
			}
			grbModel.addConstr(exp, GRB.EQUAL, 1, "outgoingEdgesFromStVx = 1");

			///Constraint for the last vertex 
			List<DiGraph.DiGraphArc<Point2D, Line2D>> inComingEdgesToLstVx = simplifiedContour.getNode(simplifiedContour.n() - 2).getIncomingArcs();
			GRBLinExpr exp1 = new GRBLinExpr();
			for (int h = 0; h < inComingEdgesToLstVx.size() ; h++) {
				int varIndex = getEdgeVarIndex(edgesList, inComingEdgesToLstVx.get(h));
				exp1.addTerm(1,edgeVariables.get(varIndex));
			}
			grbModel.addConstr(exp1, GRB.EQUAL, 1, "inComingEdgesToLstVx = 1");

			///Constraint to ensure that the number of edges entering any intermediate vertex equals the number of edges leaving it
			for (int k = 1; k < simplifiedContour.n()-1; k++) {
				List<DiGraph.DiGraphArc<Point2D, Line2D>> inComingArcs = simplifiedContour.getNode(k).getIncomingArcs();
				List<DiGraph.DiGraphArc<Point2D, Line2D>> outGoingArcs = simplifiedContour.getNode(k).getOutgoingArcs();

				GRBLinExpr linExpr = new GRBLinExpr();
				for (int j = 0; j < inComingArcs.size() ; j++) {
					int varIndex = getEdgeVarIndex(edgesList, inComingArcs.get(j));
					linExpr.addTerm(1, edgeVariables.get(varIndex));
				}

				for (int f = 0; f < outGoingArcs.size() ; f++) {
					int varIndex = getEdgeVarIndex(edgesList, outGoingArcs.get(f));
					linExpr.addTerm(-1, edgeVariables.get(varIndex));
				}

				grbModel.addConstr(linExpr, GRB.EQUAL,0, "incomingEdges" + i + " = " + "outGoingEdges" + i);
			}
			
			
			// Angle constraint
			for (int p = 1; p < simplifiedContour.n()-1; p++) {
				List<DiGraph.DiGraphArc<Point2D, Line2D>> incomingArcsOfNode = simplifiedContour.getNode(p).getIncomingArcs();
				List<DiGraph.DiGraphArc<Point2D, Line2D>> outgoingArcsOfNode = simplifiedContour.getNode(p).getOutgoingArcs();

				GRBLinExpr linExp = new GRBLinExpr();
				for (int j = 0; j < incomingArcsOfNode.size(); j++) {
					for (int k = 0; k < outgoingArcsOfNode.size(); k++) {
						Vector2D vectorOfIncomingEdge = new Vector2D(incomingArcsOfNode.get(j).getSource().getNodeData().getX() - incomingArcsOfNode.get(j).getTarget().getNodeData().getX(),
								incomingArcsOfNode.get(j).getSource().getNodeData().getY() - incomingArcsOfNode.get(j).getTarget().getNodeData().getY());
						Vector2D vectorOfOutgoingEdge = new Vector2D(outgoingArcsOfNode.get(k).getTarget().getNodeData().getX() - outgoingArcsOfNode.get(k).getSource().getNodeData().getX(),
								outgoingArcsOfNode.get(k).getTarget().getNodeData().getY() - outgoingArcsOfNode.get(k).getSource().getNodeData().getY());
						

						double angle = Math.acos(Vector2D.dot(vectorOfIncomingEdge,vectorOfOutgoingEdge) /(vectorOfIncomingEdge.norm() * vectorOfOutgoingEdge.norm())) * 180/Math.PI;
						//System.out.println("Angle : "+ angle);
						if (angle <= thresholdAngle) {
							int vertexIndex = getVertexVarIndex(verticesList, simplifiedContour.getNode(i));
							int incomingEdgeIndex = getEdgeVarIndex(edgesList, incomingArcsOfNode.get(j));
							int outgoingEdgeIndex = getEdgeVarIndex(edgesList, outgoingArcsOfNode.get(k));
							linExp.addTerm(-1, vertexVariables.get(vertexIndex));
							linExp.addTerm(1, edgeVariables.get(incomingEdgeIndex));
							linExp.addTerm(1, edgeVariables.get(outgoingEdgeIndex));
						}
					}
				}
				grbModel.addConstr(linExp, GRB.LESS_EQUAL,1, "incomingArcs" + i + " = " + "outGoingArcs" + i);	
			}	
		}	
		
		///No intersections of arcs constraint
		
		for (int m = 0; m < edgesList.size(); m++) { 
			for (int n = m+1; n < edgesList.size(); n++) {
				if (indexesofEdges.get(m) != indexesofEdges.get(n)){
					if (edgesList.get(m).getArcData().intersectsLine(edgesList.get(n).getArcData())){
						GRBLinExpr consInte = new GRBLinExpr(); 
						consInte.addTerm(1, edgeVariables.get(m)); 
						consInte.addTerm(1, edgeVariables.get(n));
						grbModel.addConstr(consInte, GRB.LESS_EQUAL, 1, "ConstrOfInter"); 
					}
				}
			}
		}
		 


		
		//The objective function
		GRBLinExpr obj = new GRBLinExpr();
		for (int t = 0; t < edgesList.size(); t++ ) {
			obj.addTerm(0, edgeVariables.get(t));
		}
		
		for (int q = 0; q < verticesList.size(); q++ ) {
			obj.addTerm(1, vertexVariables.get(q));
		}
		
		grbModel.setObjective(obj, GRB.MINIMIZE);

		grbModel.optimize();

		
		//Printing results
		for (int p = 0; p < edgesList.size(); p++) {
			double value = edgeVariables.get(p).get(GRB.DoubleAttr.X);
			DiGraph<Point2D,Line2D> optimizedgraph = new DiGraph<>();
			if (Math.round(value) == 1) {
				optimizedgraph.addArc(edgesList.get(p).getSource(), edgesList.get(p).getTarget(), edgesList.get(p).getArcData()); 
				optimizedGraphs.add(optimizedgraph);
			}
		}
		return optimizedGraphs;
	}

}

