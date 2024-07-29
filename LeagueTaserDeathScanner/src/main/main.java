package main;


import java.awt.AWTException;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fazecast.jSerialComm.SerialPort;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.imageio.ImageIO;

import net.sourceforge.tess4j.Tesseract; 
import net.sourceforge.tess4j.TesseractException; 

public class main {

	public static void main(String[] args) throws TesseractException, AWTException, IOException, InterruptedException {
		int deaths = 0;
		
		SerialPort[] allPorts = SerialPort.getCommPorts();
		Pattern patt = Pattern.compile("USB-SERIAL CH340", Pattern.CASE_INSENSITIVE);
		int i = 0;
		for (SerialPort ports: allPorts) {
			System.out.println(ports.getDescriptivePortName());
			Matcher matcher = patt.matcher(ports.getDescriptivePortName());
			boolean matchFound = matcher.find();
			if(matchFound) {
			      break;
			    } 
			i++;
		}
		SerialPort sp = SerialPort.getCommPort(allPorts[i].getSystemPortName());
		System.out.println("Connected to: "+allPorts[i].getDescriptivePortName());
		sp.openPort();
		System.out.println(sp.isOpen());
		sp.setComPortParameters(9600, Byte.SIZE, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
	    sp.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
	    sp.openPort();
	    
	    //Window to display connected port and have a way to close app.
	    JFrame frame = new JFrame("League Taser");
        JPanel panel = new JPanel();  
        panel.setLayout(new FlowLayout());  
        JLabel label = new JLabel("Connected to: "+allPorts[i].getDescriptivePortName());
        JLabel deathCount = new JLabel(deaths + " deaths.");
        JLabel reset = new JLabel("");
        panel.add(label);
        panel.add(deathCount);
        panel.add(reset);
        frame.add(panel);  
        frame.setSize(400, 200);  
        frame.setLocationRelativeTo(null);
	    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    frame.setVisible(true);
	    
	    String on = "1";
	    String off = "0";
	    byte[] taseBytesOn = on.getBytes();
	    byte[] taseBytesOff = off.getBytes();	
	    System.out.println("Length: " + taseBytesOn[0]);
	    
		// Setup Tesseract
		Tesseract tesseract = new Tesseract(); 
		//tesseract.setDatapath("C:\\\\Users\\\\dsguy\\\\Desktop\\\\Programming Stuff\\\\EclipseWorkspace\\\\Tess4J\\\\tessdata");
		tesseract.setDatapath("./tessdata");
		
		//setup regex
		Pattern pattern = Pattern.compile("(\\d(/\\d)+)", Pattern.CASE_INSENSITIVE);
		

		
		
		//Take SS and crop/save it
		Robot robot = new Robot();
		Rectangle rect = new Rectangle(1665, 0, 76, 25);
		int resetCounter = 0;
		while (true){
		BufferedImage screenShot = robot.createScreenCapture(rect);
		ImageIO.write(screenShot, "PNG", new File("screenShot.png"));
		
		
		//Read the image to text. C:\\Users\\dsguy\\Desktop\\Programming Stuff\\EclipseWorkspace\\LeagueTaserDeathScanner\\screenShot.png
		File img = new File("screenShot.png");
		String text =  tesseract.doOCR(img);
		Matcher matcher = pattern.matcher(text);
	    boolean matchFound = matcher.find();
	    
		if(matchFound) {
			resetCounter = 0;
			int checkDeaths = Integer.valueOf((text.split("/"))[1]);
			if (checkDeaths > deaths) {
				deaths = checkDeaths;
				System.out.println("You died " + deaths + " times!!!");
				deathCount.setText(deaths + " deaths.");
				sp.writeBytes(taseBytesOn, taseBytesOn.length);
			}
		}else {
			resetCounter++;
			System.out.println("ERROR: "+ resetCounter);
			//maybe redundant
			sp.writeBytes(taseBytesOff, taseBytesOff.length);
			if (resetCounter==20) {
				resetCounter = 0;
				deaths = 0;
				reset.setText("Reset!");
				deathCount.setText(deaths + " deaths.");
				Thread.sleep(2500);
				reset.setText("");
				System.out.println("/nRESET\n");
			}
		}
		
		Thread.sleep(250);
		}
		
	}

}
