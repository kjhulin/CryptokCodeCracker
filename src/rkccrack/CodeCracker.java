/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rkccrack;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.*;
import rkccrack.RKCCrack.RKC;
/**
 *
 * @author Kevin Hulin
 */
public class CodeCracker extends JFrame{
    
    JButton RKSolver = new JButton("Running Key Solver");
    JButton StreamSolver = new JButton("Stream Solver");
    JButton LoadProbabilities = new JButton("Learn Probabilities");
    
    RKCCrack rkc = null;
    StreamCrack sc = null;
    public CodeCracker(){
        setTitle("Cryptok Code Cracker");
        try{
            URL icon = CodeCracker.class.getResource("/cryptok.ICO");
            setIconImage(new ImageIcon(icon).getImage());
        }catch(Exception e){}
        setLayout(new BorderLayout());
        JTabbedPane jtp = new JTabbedPane();
        rkc = new RKCCrack();
        sc = new StreamCrack();
        jtp.add(rkc,"Running Key Cipher Solver");
        jtp.add(sc,"Classic Cipher Solver");
        
        //JPanel p = new JPanel(new GridLayout(0,1));
        //p.add(RKSolver);
        setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);


        add(jtp,BorderLayout.CENTER);
        this.setSize(new Dimension(800,800));
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        RKC.init(6,false); //TODO: Move this out of the GUI thread
        
    }

    public static void main(String[] args){
        CodeCracker c = new CodeCracker();
    }
    
    
}
