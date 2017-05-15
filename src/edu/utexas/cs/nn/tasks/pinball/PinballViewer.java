package edu.utexas.cs.nn.tasks.pinball;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import edu.utexas.cs.nn.parameters.Parameters;
import pinball.PinBall;
import pinball.PinBallCanvas;

/**
 * Used to watch a PinballTask live
 * 
 * @author johnso17
 *
 */
public class PinballViewer extends JFrame {

	private static final long serialVersionUID = 1L;
	PinBall pball;
	PinBallCanvas canvas;
	
	/**
	 * Constructor for the PinballViewer
	 */
	public PinballViewer(){
		// Following is the code in common from PinBallGUI and PinBallGUIReplay Constructors
		setSize(500, 500);
        setTitle("PinBall Domain");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        Toolkit toolkit = getToolkit();
        Dimension size = toolkit.getScreenSize();
        setLocation(size.width/2 - getWidth()/2, 
		size.height/2 - getHeight()/2);
        
        pball = new PinBall("data/pinball/" + Parameters.parameters.stringParameter("pinballConfig"));
        canvas = new PinBallCanvas(pball, "data/pinball/" + Parameters.parameters.stringParameter("pinballConfig"));
        add(canvas);
        canvas.setVisible(true);
	}
	
	/**
	 * Repaints the PinBallCanvas based on the action taken
	 * 
	 * @param action Integer representing the action taken in the PinballTask
	 */
	public void actionPerformed(int action) {
		pball.step(action); // Update PinBall state
		repaint(); // Repaint PinBallCanvas
	}

}