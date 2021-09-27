//package gui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.JFrame;


public class Window extends JFrame{
	

	private static final long serialVersionUID = 1L;
	
	
	public Window(SimpleGraphicsView view){
		setMinimumSize(new Dimension(800,600));
		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.add(view);
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_PLUS){
					System.out.println("zoom in");
					view.zoomIn();
				}
				if(e.getKeyCode() == KeyEvent.VK_MINUS){
					System.out.println("zoom out");
					view.zoomOut();
				}
			}
		});
		view.moveToCenter();
		
	}
	

	
	
	
}
