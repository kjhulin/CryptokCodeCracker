/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rkccrack;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;



/**
 *
 * @author Kevin Hulin
 */
public class RKCCrack extends JPanel implements ComponentListener{
    
    public static final String DC20_UBER_BADGE =     "QMMDYPSQSAMSPMQJLTYVRXHBKYRDSZJYHBGSNQHQBWLVJVPMFUISDCGWXATLEOBULNIHPCSPTFPWIISXWTAMTXSVHTFLHWVVNWPPWYIPLDQFIJWTYCILZGTXMUDAFPCUKPNTOXJSNVSTMLUX";
    public static final String DC20_LONG_CIPHER = "WWDWUMFUPQBHPSDAAEMNVZAWZCFYRCDGVWCVJSELMXSHBWAZHNWPUMMWXAGDOHWPEEDHIEYMMQAMMCKGMEFIIGOGNUKPVGSWTBCRHMLAOIMAFCPFLXWCBEDZCDHNZEEWRHIWTFQAEPEPLZTLEKDRTTJPABMYOSPEYSBTNOLAIIZOIWRUMILERPLWSQYPVJWYMWLTXGCCMOUSBLJCFWPEXGBUCAEJYEFROMWHLESRBUCLKNTGBHTFWIOMCAGPIJ";
    public static final String KRYPTOS = "OBKRUOXOGHULBSOLIFBBWFLRVQQPRNGKSSOTWTQSJQSSEKZZWATJKLUDIAWINFBNYPVTTMZFPKWGDKZXTJCDIGKUHUAUEKCAR";
    public static final String DC23_UBER_COIN = "BVFBHGHXAWJEKEDMDZAPRMWGNMTVIRPWIKHGIPUU";
    public static double EPSILON = Math.log(0.00001);
    public static final int NUM_SOLUTIONS = 50;
    public static final int NUM_NGRAMS_PER = 20;
    public static final int TOP_N_WORDS = 50;
    JPanel bottom = new JPanel();
    JPanel right = new JPanel();
    
    JProgressBar prog = new JProgressBar();
    JTextArea input = new JTextArea();
    JTextArea input2 = new JTextArea();
    JTextArea input3 = new JTextArea();
    
    JTextArea output = new JTextArea();
    JTextArea output2 = new JTextArea();
    JTextArea outputOS = new JTextArea();
    JTextArea outputWS = new JTextArea();
    
    JButton b1 = new JButton("Solve!");
    //JButton b2 = new JButton("Learn!");
    
    JTextField setting1 = new JTextField();
    JTextField setting2 = new JTextField();
    JTextField setting3 = new JTextField();
    
    JSplitPane outs;
    JSplitPane jsp;
    
    JTabbedPane jtbp = new JTabbedPane();
    private static boolean isRunning = false;
    
    
    public RKCCrack(){
        
        setting1.setText("6");
        setting2.setText("100000");
        setting3.setText("6");
        Font monoFont = new Font(Font.MONOSPACED, 1,11);
        setLayout(new BorderLayout());

        prog.setStringPainted(true);
        JPanel p1 = new JPanel();
        JPanel p2 = new JPanel(new BorderLayout());
        outs = new JSplitPane();
        outs.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        
        jtbp.add(new JScrollPane(output),"Viterbi");
        jtbp.add(new JScrollPane(outputOS),"N-Gram Search");
        jtbp.add(new JScrollPane(outputWS),"Word Search");
        outs.setLeftComponent(jtbp);
        outs.setRightComponent(new JScrollPane(output2));
        p2.add(outs,BorderLayout.CENTER);
        
        for(JTextArea o : new JTextArea[]{output,output2,outputWS,outputOS}){
            o.setEditable(false);
            o.setLineWrap(false);
            o.setFont(monoFont);
        }
   
        
        input.setLineWrap(true);
        input.setFont(monoFont);
        input2.setFont(monoFont);
        input3.setFont(monoFont);
        p2.add(new JLabel("Output:"),BorderLayout.NORTH);
        jsp = new JSplitPane();
        jsp.setContinuousLayout(true);
        
        jsp.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jsp.setTopComponent(p2);
        jsp.setBottomComponent(p1);
        p1.setLayout(new BorderLayout());
        p1.add(new JScrollPane(input),BorderLayout.CENTER);
        JPanel guessPanel = new JPanel(new BorderLayout());
        guessPanel.add(new JLabel("Crib: "),BorderLayout.WEST);
        guessPanel.add(new JScrollPane(input2),BorderLayout.CENTER);

        JPanel hintPanel = new JPanel(new BorderLayout());
        hintPanel.add(new JLabel("Hints (ENTER to attempt crib; TAB to swap between crib and highest scoring solution; SHIFT+ENTER to accept crib into HINT):"),BorderLayout.NORTH);
        hintPanel.add(input3,BorderLayout.CENTER);
        JPanel p3 = new JPanel(new BorderLayout());
        
        p3.add(hintPanel,BorderLayout.CENTER);
        p3.add(guessPanel,BorderLayout.SOUTH);
        p1.add(p3,BorderLayout.SOUTH);
        
        p1.add(new JLabel("Input (Running Key Cipher goes here):"),BorderLayout.NORTH);
        
        setting1.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                RKC.initDone = false; // Invalidate initialization (need to reload probabilities...?
            }
        });
        //this.setTitle("RKC Crack by Cryptok");
        input.addKeyListener(new KeyListener(){

            @Override
            public void keyTyped(KeyEvent e) {
                while(input.getText().length() > input3.getText().length()){
                    input3.setText(input3.getText()+"_");
                }
                if(input.getText().length() < input3.getText().length()){
                    input3.setText(input3.getText().substring(0,input.getText().length()));
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                while(input.getText().length() > input3.getText().length()){
                    input3.setText(input3.getText()+"_");
                }
                if(input.getText().length() < input3.getText().length()){
                    input3.setText(input3.getText().substring(0,input.getText().length()));
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
               while(input.getText().length() > input3.getText().length()){
                    input3.setText(input3.getText()+"_");
                }
                if(input.getText().length() < input3.getText().length()){
                    input3.setText(input3.getText().substring(0,input.getText().length()));
                }
            }
            
        });
        input2.addKeyListener(new KeyListener(){
            boolean isSwapped = false;
            String last = "";
            @Override
            public void keyTyped(KeyEvent e) {
                
            }

            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println(e.getKeyChar() + " " + e.isShiftDown());
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    e.consume();
                    if(!RKC.initDone){
                        output2.append("Must learn first!\n");
                    }else{
                        if(e.isShiftDown()){
                            if(output2.getText().contains(" --- ")){
                                try{
                                    int idx = Integer.parseInt(output2.getText().split("---")[1].split("::")[0].trim());
                                    String text = input2.getText().toUpperCase();
                                    char[] input = input3.getText().toCharArray();
                                    for(int i = idx; i < idx + text.length(); ++i){
                                        input[i] = text.charAt(i - idx);
                                    }
                                    input3.setText(new String(input));
                                    input3.setCaretPosition(idx + text.length());
                                }catch(Exception ee){System.out.println("Error in shift+enter\n"+ee.getMessage());}
                                
                            }
                        }else{
                            RKC.guess(input.getText().toUpperCase(), input2.getText().toUpperCase(), 20);
                            isSwapped = false;
                        }
                    }
                }else if(e.getKeyCode() == KeyEvent.VK_TAB){
                    e.consume();
                    if(e.isShiftDown()){
                        //shift hint
                        char[] in = input3.getText().toCharArray();
                        for(int i = 0; i < in.length; i++){
                            if(in[i] != '_'){
                                in[i] = RKC.dec(input.getText().charAt(i),in[i]);
                            }
                        }
                        int idx = input3.getCaretPosition();
                        input3.setText(new String(in));
                        input3.setCaretPosition(idx);
                    }else{
                        if(isSwapped){
                            input2.setText(last);
                            isSwapped = false;
                        }else{
                            if(output2.getText().contains(" :: ")){
                                String swap = output2.getText().split("::")[1].split("\n")[0].trim();
                                last = input2.getText();
                                input2.setText(swap);
                                isSwapped = true;
                            }
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        
        input3.addKeyListener(new KeyListener(){

            @Override
            public void keyTyped(KeyEvent e) {
                e.consume();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyChar() == KeyEvent.VK_DELETE){
                    System.out.println(input3.getCaretPosition() + " " + input3.getSelectionStart() + " " + input3.getSelectionEnd());
                    if(input3.getSelectedText() != null){
                        int start = input3.getSelectionStart();
                        int stop = input3.getSelectionEnd();
                        
                        char[] input = input3.getText().toCharArray();
                        for(int i = start; i < stop; ++i){
                            input[i] = '_';
                        }
                        input3.setText(new String(input));
                        input3.setCaretPosition(start);
                        e.consume();                    
                    }else{
                        int pos = input3.getCaretPosition();
                        char[] input = input3.getText().toCharArray();
                        if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE && pos > 0){
                            input[pos-1] = '_';
                            input3.setText(new String(input));
                            if(pos > 0){
                                input3.setCaretPosition(pos-1);
                            }else{
                                input3.setCaretPosition(pos);
                            }
                            e.consume();
                        }else if(e.getKeyCode() == KeyEvent.VK_DELETE && pos < input.length){
                            input[pos] = '_';
                            input3.setText(new String(input));
                            if(pos < input.length){
                                input3.setCaretPosition(pos+1);
                            }else{
                                input3.setCaretPosition(pos);
                            }
                            e.consume();
                        }
                    }
                }else if(e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z ){
                    if(input3.getSelectedText() != null){
                        int start = input3.getSelectionStart();
                        int stop = input3.getSelectionEnd();
                        
                        char[] input = input3.getText().toCharArray();
                        input[start] = (char)e.getKeyCode();
                        for(int i = start+1; i < stop; ++i){
                            input[i] = '_';
                        }
                        input3.setText(new String(input));
                        input3.setCaretPosition(start+1);
                        e.consume();                    
                    }else{
                        int pos = input3.getCaretPosition();
                       
                        char[] input = input3.getText().toCharArray();
                        if(pos < input.length){
                            input[pos] = (char)e.getKeyCode();
                            input3.setText(new String(input));
                            if(pos < input.length){
                                input3.setCaretPosition(pos+1);
                            }else{
                                input3.setCaretPosition(pos);
                            }
                        }
                        e.consume();
                        return;
                    }
                }
                
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        add(jsp, BorderLayout.CENTER);
        
        bottom.setLayout(new BorderLayout());
        bottom.add(prog,BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        
        right.setLayout(new BorderLayout());
        JPanel topRight = new JPanel(new GridLayout(0,1));
        JPanel botRight = new JPanel(new GridLayout(0,1));
        right.add(topRight,BorderLayout.NORTH);
        right.add(botRight,BorderLayout.SOUTH);
        topRight.add(new JLabel("Markov Order:"));
        topRight.add(setting1);
        topRight.add(new JLabel("Search Depth:"));
        topRight.add(setting2);
        topRight.add(new JLabel("Min Word Len:"));
        topRight.add(setting3);
        
        botRight.add(b1);
//        botRight.add(b2);
        add(right,BorderLayout.EAST);
        
        
        setSize(800,800);
        setVisible(true);

        //setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        
        
        b1.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                Thread t = new Thread(new Runnable(){

                    @Override
                    public void run() {
                        if(isRunning){
                            isRunning = false;
                            b1.setText("Stopping...");
                            
                        }else{
                            try{
                                //b1.setEnabled(false);
                                b1.setText("Stop");
                                isRunning = true;
                                //b2.setEnabled(false);
                                if(!RKC.initDone){
                                    RKC.init(Integer.parseInt(setting1.getText()),false);
                                }
                                final String cipher = input.getText();
                                final String hint = input3.getText();
                                Thread t1 = new Thread(new Runnable(){

                                    @Override
                                    public void run() {
                                        RKC.fastsolve(cipher.toUpperCase(),Integer.parseInt(setting2.getText()),hint);  
                                    }
                                });
                                Thread t2 = new Thread(new Runnable(){

                                    @Override
                                    public void run() {
                                        RKC.wordsearch(cipher.toUpperCase(),Integer.parseInt(setting3.getText()),hint);
                                    }
                                });
                                Thread t3 = new Thread(new Runnable(){

                                    @Override
                                    public void run() {
                                        RKC.ngramsearch(cipher.toUpperCase(),Integer.parseInt(setting2.getText()),hint);
                                    }
                                });
                                t1.start();
                                t2.start();
                                t3.start();
                                t1.join();
                                t2.join();
                                t3.join();
                            }catch(Exception e){
                                e.printStackTrace();
                                isRunning = false;
                                b1.setText("Solve!");
                                b1.setEnabled(true);
                            }
                            isRunning = false;
                            b1.setText("Solve!");
                            b1.setEnabled(true);
                        }
                    }
                });
                t.start();
                }

        });
        
        RKC.setOutput(TA2PS(output));
        RKC.setGuessOutput(TA2PS(output2));
        RKC.setOrderSearchOutput(TA2PS(outputOS));
        RKC.setWordSearchOutput(TA2PS(outputWS));
            
        
        RKC.setProg(new ShowProgress(){

            String msg = "Progress: ";
            @Override
            public void setMax(int i) {
                prog.setMaximum(i);
            }

            @Override
            public void setValue(int i) {
                prog.setValue(i);
                prog.setString(msg + prog.getValue() +  " / " + prog.getMaximum() + " (" + String.format("%.1f",prog.getPercentComplete()*100) + "%)");
            }

            @Override
            public void setMsg(String s) {
                msg = s;
            }
            
        });
        
        addComponentListener(this);
        
    }
    
    public final PrintString TA2PS(final JTextArea ta){
        return new PrintString() {

            @Override
            public void print(String s) {
                ta.append(s);
                ta.setCaretPosition(ta.getDocument().getLength());
                
            }

            @Override
            public void println(String s) {
                ta.append(s + "\n");
                ta.setCaretPosition(ta.getDocument().getLength());
            }
            @Override
            public void clear(){
                ta.setText("");
                ta.setCaretPosition(0);
            }
            
            @Override
            public int getPos(){
                return ta.getCaretPosition();
            }
            @Override
            public void setPos(int pos){
                ta.setCaretPosition(pos);
            }
        };
    }
    public static String rep(String s, int n){
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < n; ++i){
            ret.append(s);
        }
        return ret.toString();
    }
    public static void main(String[] args) {

        
        RKCCrack r = new RKCCrack();
        //r.input.setText(KRY);
        //r.input.setText(LCC);
        r.input.setText(DC20_UBER_BADGE);
        r.input3.setText(rep("_",DC20_UBER_BADGE.length()));
    }

    @Override
    public void componentResized(ComponentEvent e) {
        outs.setDividerLocation(.75);
        jsp.setDividerLocation(.9);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void componentShown(ComponentEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
    static class RKC{
        public static final String PROB_FILE_PATH = "RKC.prob";
        static Random rand = new Random();
        static final byte[] alphabet = {'A','B','C','D','E','F','G','H','I','J','K','L','M',
                                         'N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};//"ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        static final byte[] unalpha = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25};
        static final byte[] alphabet1 =  {'K','R','Y','P','T','O','S','A','B','C','D','E','F', //"KRYPTOS..."
                                         'G','H','I','J','L','M','N','Q','U','V','W','X','Z'};
        static final byte[] unalpha1 = {7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 0, 17, 18, 19, 5, 3, 20, 1, 6, 4, 21, 22, 23, 24, 2, 25};
        
        static int order;
        static boolean initDone = false;
        static FrequencyTree freq;
        static HashSet<String> wordlist;
        static long[] counts;
        static DecimalFormat df = new DecimalFormat("0.0000");
        static String corpus = "corpus"; //Directory where corpus is located (currently set to look in working directory
        static String wordlistfile = "/res/words.txt";
        static boolean findSpaces = false;
        
        static PrintString ps = PrintString.STANDARD_OUT;
        static PrintString ps2 = PrintString.STANDARD_OUT;
        static PrintString ps3 = PrintString.STANDARD_OUT;
        static PrintString gps = PrintString.STANDARD_OUT;
        static ShowProgress sp = new ShowProgress(){
            int max = 0;
            String msg = "";
            @Override
            public void setMax(int i) {
                max = i;
            }

            @Override
            public void setValue(int i) {
                ps.println(msg + " " + i + "/" + max + " (" + String.format("%.2f", i/(double)max) + "%)");
            }

            @Override
            public void setMsg(String s) {
                msg = s;
            }
            
        };
        public static void setOutput(PrintString p){
            ps = p;
        }
        public static void setOrderSearchOutput(PrintString p){
            ps2 = p;
        }
        public static void setWordSearchOutput(PrintString p){
            ps3 = p;
        }
        
        public static void setProg(ShowProgress s){
            sp = s;
        }
        public static void setGuessOutput(PrintString p){
            gps = p;
        }

        public static void init(int n, boolean findSpaces){ //Initialize for order N 
            if(n < 1){
                ps.println("Error invalid order!  Bad state.  Exiting.");
                //System.exit(0);
            }
            order = n;
            freq = new FrequencyTree(alphabet);
            counts = new long[order+1];
            RKC.findSpaces = findSpaces;

            try{
                learn(corpus);
            }catch(Exception e){
                ps.println("Error occured while loading corpus!");
                e.printStackTrace();
                //System.exit(0);
            }
            try{
                wordlist = new HashSet<String>();
                loadWordList(wordlistfile);
                ps.println("Loaded " + wordlist.size() + " words!");
            }catch(Exception e){
                ps.println("Error occured while loading wordlist!");
                e.printStackTrace();
            }
            initDone = true;
        }
        
        public static void loadWordList(String wl) throws FileNotFoundException, IOException, URISyntaxException{
            
            BufferedReader br = new BufferedReader(new InputStreamReader(CodeCracker.class.getResourceAsStream(wl)));//wl.openStream());
            String line;
            while((line = br.readLine()) != null){
                wordlist.add(line);
            }
            br.close();
        }
        public static void learn(String dir) throws FileNotFoundException, IOException{
            learn(dir,false);
        }
        public static void learn(String dir,boolean force) throws FileNotFoundException, IOException{
            if(!force){
                File f = new File(PROB_FILE_PATH);
                if(f.exists()){
                    
                    try{
                        
                        ps.println("Retrieving probabilities.  This may take a while");
                        freq = FrequencyTree.load(PROB_FILE_PATH);
                        ps.println(FrequencyTree.Node.numNodes + " nodes loaded!");
                        ps.println(freq.get("") + " characters seen");
                        EPSILON = - Math.log( freq.get(""));
                        ps.println(String.format("Epsilon = %02f", EPSILON));
                        return;
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    ps.println("Could not open file... Re-learning probabilities");
                    System.out.println("Could not open file... Re-learning probabilities");
                }
                
            }
            byte[] buffer = new byte[4096];
            LinkedList<File> q = new LinkedList<File>();
            q.offer(new File(dir));
            int numFiles = 0;
            int fileIndex = 0;
            sp.setMsg("Learning: " );
            do{
                File f = q.pop();
                if(f.isDirectory()){
                    for(File ff : f.listFiles()){
                        if(!ff.getName().equals("RKC.prob"));
                        q.offer(ff);
                        numFiles ++;
                        sp.setMax(numFiles);
                    }
                }else{
                    fileIndex ++;
                    sp.setValue(fileIndex);
                    //Learn file
                    learnNewFile();
                    ps.println("Reading file: " + f.getAbsolutePath());
                    FileInputStream fis = new FileInputStream( f );
                    FileChannel ch = fis.getChannel( );
                    MappedByteBuffer mb = ch.map(FileChannel.MapMode.READ_ONLY, 0L, ch.size());
                    
                    int nGet;
                    int total = 0;
                    while( mb.hasRemaining( ) )
                    {
                        nGet = Math.min( mb.remaining( ), 4096 ); //4KB
                        mb.get( buffer, 0, nGet );
                        total += nGet;
                        learnProcess(buffer,nGet);
                    }
                    ps.println("Num bytes in text: " + total);
                    ps.println("Total num of nodes: " + FrequencyTree.Node.numNodes);
                    //return; //Learn from one book (for debugging)
                }
            }while(!q.isEmpty());
            
            //Save file
            System.out.println("Writing frequency tree to file");
            try{
                freq.save(PROB_FILE_PATH);
                System.out.println("Probabilities written to file");
            }catch(Exception e){
                System.out.println("Failed to write file");
                e.printStackTrace();
            }

            ps.println(freq.get("") + " characters seen");
            EPSILON = - Math.log( freq.get(""));
            ps.println(String.format("Epsilon = %02f", EPSILON));
        }
        
        static byte[] prevChars;
        static byte[] border;
        static int prevIndex;
        static boolean havePrev;
        public static void learnNewFile(){ //Reset
            prevChars = new byte[order+1];
            border = new byte[order + 1];
            Arrays.fill(prevChars, (byte)0);
            prevIndex = 0;
            havePrev = false;
        }
        public static byte[] scrub(byte[] b,int end, String extra){
            StringBuilder sb = new StringBuilder();
            byte c;
            for(int i = 0; i < end; i++){
                c = (byte)Character.toUpperCase((char)b[i]);
                if(c >= 'A' && c <= 'Z'){
                    sb.append((char)unalpha[c-'A']);
                }else if(extra != null && extra.indexOf(c) > -1){
                    sb.append((char)(26 + extra.indexOf(c)));
                }
            }
            return sb.toString().getBytes();
        }
        public static String unscrub(byte[] b,int start, int end, String extra){
            StringBuilder sb = new StringBuilder();
            for(int i = start; i < end; i++){
                if(b[i] < 26){
                    sb.append((char)alphabet[b[i]]);
                }else{
                    sb.append(extra.charAt(b[i]-26));
                }
            }
            return sb.toString();
        }
        public static byte[] scrub(byte[] b,int end){
            StringBuilder sb = new StringBuilder();
            char c;
            for(int i = 0; i < end; i++){
                c = Character.toUpperCase((char)b[i]);
                if(c >= 'A' && c <= 'Z'){
                    sb.append((char)unalpha[c-'A']);
                }
                //System.out.println(sb.toString());
            }
            return sb.toString().getBytes();
        }
        public static void learnProcess(byte[] b, int n){
            if(findSpaces){
                b = scrub(b,n," ");
            }else{
                b = scrub(b,n);
            }       
            
            for(int i = 0; i < b.length - order; i ++){
                if(i < order + 1){
                    //check for prev
                    if(havePrev){
                        for(int j = 0; j < order + 1; j++){
                            if(prevIndex+j < prevChars.length){
                                border[j] = prevChars[prevIndex+j];
                            }else{
                                border[j] = b[j - prevIndex];
                            }
                        }
                        freq.increment(border);
                    }
                }else{
                    freq.increment(b,i,i+order+1);
                }
            }
            if(b.length - order - 1 > 0){ //Assuming less than Order bytes in b implies this is the last chunk
                for(int i = 0; i < order + 1; i++){
                    prevChars[i] = b[b.length - order - 1 + i];
                }
                havePrev = true;
            }else{
                havePrev = false;
            }
                
        }
        public static String padLeft(String s, int n){
            if(n == 0) return s;
            else return String.format("%" + n + "s", s);
        }
        public static void ngramsearch(String cipher, int depth, String solution){
            ps2.clear();
            ps2.println("Initializing N-Gram Search.");
            byte[] ciph = RKC.scrub(cipher.getBytes(),cipher.length());
            byte[] solu = null;
            if(solution != null){
                solu = RKC.scrub(solution.getBytes(),solution.length(),"_");
            }

            ps2.println("Retrieving learned probabilities");
            int wordsize = Math.min(order,ciph.length);
            System.out.println("StartIndex: " + wordsize);
            boolean match = true;
            boolean ignoreOrder = false;
            int t = 0;
			@SuppressWarnings("unchecked")
            ArrayList<Pair<byte[],Double>>[] topN = new ArrayList[ciph.length - wordsize + 1];
            for(int i = 0; i < topN.length; i++){
                topN[i] = new ArrayList<Pair<byte[],Double>>();
            }

            double p1 = 0;
            for(byte[] s : freq.Keys(wordsize)){
                p1 = lProb(s,0,wordsize);
                if(!isRunning){return ;}
                for(int i = 0; i < ciph.length - wordsize + 1; i++){
                    match = true;
                    ignoreOrder = false;
                    if(solu != null){
                        if(solu[i] != 26 || solu[i+1] != 26){
                            ignoreOrder = true;
                        }
                        for(int j = i; j < i+wordsize; j++){
                            if(solu[j] != 26 && solu[j] != s[j-i]){
                                match = false; break;
                            }
                        }
                    }
                    if(!match) continue;

                    byte[] dec = decode(ciph,i,s,0,wordsize);
                    if(!ignoreOrder &&( dec[0] > s[0] || (dec[0] == s[0] && dec[1] > s[1]))){continue;} // Enforce order on pairs -> Remove mirror pairs (for simplicity, include cases where first chars are == )
                    
                    Pair<byte[],Double> p = new Pair<byte[],Double>(s,0.0); //log(1) == 0 
                    p.second += p1 + lProb(dec,0,wordsize);
                    topN[i].add(p);
                    
                    
                    if(topN[i].size() > Math.max(NUM_NGRAMS_PER,100000)){
                        System.out.println("[gc] ngramsearch");
                        Collections.sort(topN[i]);
                        topN[i] = new ArrayList<>(topN[i]
                                .subList(topN[i].size() - NUM_NGRAMS_PER,
                                        topN[i].size()));
                    }
                }
            }

            ps2.println("Sorting results");
            for (ArrayList<Pair<byte[], Double>> topN1 : topN) {
                Collections.sort(topN1);
            }
            
            
            for(int offset = 0; offset < wordsize + 1; offset++){
                String title = "+++ Offset(s) ";
                for(int i = offset; i < topN.length; i+= wordsize){
                    if(i > offset){
                        title += ", " + i;
                    }else{
                        title += i;
                    }
                }
                ps2.println(title + " +++");
                
                for(int k = 0; k < NUM_NGRAMS_PER; k++){
                    String l1 = padLeft("",offset);
                    String l2 = padLeft("",offset);
                    
                    String pl1 = "";
                    String pl2 = "";
                    for(int i = offset; i < topN.length; i+= wordsize){
                        
                        if (k < topN[i].size()){
                            byte[] key = topN[i].get(topN[i].size() -1 -k).first;

                            l1 += unscrub(key,0,wordsize,"") + " ";
                            l2 += unscrub(decode(ciph,i,key,0,wordsize),0,wordsize,"") + " ";
                            pl1 += String.format("%.4f ",topN[i].get(topN[i].size() -1 -k).second);
                        }else{
                            l1 += padLeft("",wordsize + 1);
                            l2 += padLeft("",wordsize + 1);
                            pl1 += padLeft("",8);
                        }
                    }
                    ps2.println(l1 + " | " + pl1);
                    ps2.println(l2 + " | " + pl2);
                    if(k < NUM_NGRAMS_PER - 1){
                        ps2.println("");
                    }
                }
            }
            ps2.setPos(0);
        }
        
        
        public static final int MAX_WORD_LENGTH = 15;
        public static void wordsearch(String cipher, int minLength, String solution){
            ps3.clear();
            ps3.println("Searching...");
            byte[] ciph = RKC.scrub(cipher.getBytes(),cipher.length());
            byte[] solu = null;
            ArrayList<Pair<byte[],Pair<Integer,Double>>> zeros = new ArrayList<Pair<byte[],Pair<Integer,Double>>>(); //words with location = 0
            if(solution != null){
                solu = RKC.scrub(solution.getBytes(),solution.length(),"_");
            }
			@SuppressWarnings("unchecked")
            ArrayList<Pair<byte[],Pair<Integer,Double>>>[] topN = new ArrayList[MAX_WORD_LENGTH - minLength + 1];
            for(int i = 0; i < MAX_WORD_LENGTH - minLength + 1; i++){
                topN[i] = new ArrayList<Pair<byte[],Pair<Integer,Double>>>();
            }
            for(String s : wordlist){
                if(!isRunning){break;}
                if(s.length() >= minLength && s.length() <= MAX_WORD_LENGTH){
                    byte[] k = scrub(s.getBytes(),s.length(),"");
                    for(Pair<byte[],Pair<Integer,Double>> r :  search(ciph,k,5)){
                        topN[s.length() - minLength].add(r);
                    }
                    //Handle memory...
                    if(topN[s.length() - minLength].size() > Math.max(TOP_N_WORDS,100000)){
                        System.out.println("[gc] ngramsearch");

                        Collections.sort(topN[s.length() - minLength]);
                        topN[s.length() - minLength] = new ArrayList<>(topN[s.length() - minLength]
                                .subList(topN[s.length() - minLength].size() - TOP_N_WORDS,
                                        topN[s.length() - minLength].size()));
                    }
                }
                
                
            }
            ps3.println("Sorting results...");
            for(int i = 0; i < MAX_WORD_LENGTH - minLength + 1; i++){
                Collections.sort(topN[i]);
            }
            for(int j = 0; j < topN.length; j++){
                for(int i = 0; i < topN[j].size(); i++){
                    Pair<byte[],Pair<Integer,Double>> max = topN[j].get(i);
                    if(max.second.first == 0){
                        zeros.add(max);
                    }
                }
            }
            Collections.sort(zeros);
            ps3.println("*** Top Starting Words ***");
            for(int i = 0; i < TOP_N_WORDS && i < zeros.size(); i++){
                Pair<byte[],Pair<Integer,Double>> max = zeros.get(zeros.size()-i-1);
                ps3.println("[" + String.format("%03d",i+1) + "] " + df.format(max.second.second) + " --- " + String.format("%03d",max.second.first) + " :: " + unscrub(decode(ciph,max.second.first,max.first,0,max.first.length),0,max.first.length,"")+"  " + unscrub(max.first,0,max.first.length,""));

            }
            
            for(int j = 0; j < topN.length; j++){
                ps3.println("*** Wordlength = " + (minLength + j) + " ***");
                for(int i = 0; i < TOP_N_WORDS; i++){
                    Pair<byte[],Pair<Integer,Double>> max = topN[j].get(topN[j].size()-i-1);
                    ps3.println("[" + String.format("%03d",i+1) + "] " + df.format(max.second.second) + " --- " + String.format("%03d",max.second.first) + " :: " + unscrub(decode(ciph,max.second.first,max.first,0,max.first.length),0,max.first.length,"")+"  " + unscrub(max.first,0,max.first.length,""));
                }
            }
            ps3.setPos(0);
        }
        public static ArrayList<Pair<byte[],Double>> fastsolve(String cipher, int depth){
            return fastsolve(cipher, depth, null);
        }
        public static ArrayList<Pair<byte[],Double>> fastsolve(String cipher, int depth, String solution){
            ps.println("Initializing Fast solve.");
            byte[] ciph = RKC.scrub(cipher.getBytes(),cipher.length());
            byte[] solu = null;
            if(solution != null){
                solu = RKC.scrub(solution.getBytes(),solution.length(),"_");
            }
			@SuppressWarnings("unchecked")
            ArrayList<Pair<byte[],Double>>[] vit = new ArrayList[ciph.length];
            for(int i = 0; i < ciph.length; i++){
                vit[i] = new ArrayList<Pair<byte[],Double>>();
            }
            ps.println("Retrieving learned probabilities");
            int startIndex = Math.min(order+1,ciph.length-1);
            System.out.println("StartIndex: " + startIndex);
            boolean match = true;
            boolean ignoreOrder = false;
            int t = 0;
            for(byte[] s : freq.Keys(startIndex)){
                match = true;
                ignoreOrder = false;
                //if(s[0] > alphabet.length / 2){ continue; } //Only include keys in the first half of the alphabet to remove duplicate solutions (i.e. key swapped with plaintext)
                if(solu != null){
                    if(solu[0] != 26 || solu[1] != 26){
                        ignoreOrder = true;
                    }
                    for(int i = 0; i < startIndex; i++){
                        if(solu[i] != 26 && solu[i] != s[i]){
                            match = false; break;
                        }
                    }
                }
                if(!match) continue;
                
                
                byte[] dec = decode(ciph,0,s,0,startIndex);
                
                if(!ignoreOrder &&( dec[0] > s[0] || (dec[0] == s[0] && dec[1] > s[1]))){continue;} // Enforce order on pairs -> Remove mirror pairs (for simplicity, include cases where first chars are == )
                t ++;
                Pair<byte[],Double> p = new Pair<byte[],Double>(s,0.0); //log(1) == 0
                for(int i = startIndex; i > 0; i--){ 
                    p.second += lProb(s,0,i) + lProb(dec,0,i);
                }
                vit[startIndex].add(p);
                
            }
            System.out.println("Num keys: " + t);
            ps.println(vit[startIndex].size() + " solutions learned");
            ps.println("Removing low probability solutions");
            Collections.sort(vit[startIndex]);
            vit[startIndex] = new ArrayList<>(vit[startIndex].subList(Math.max(0,vit[startIndex].size() - depth - 1), vit[startIndex].size()  ));
            ps.println("Applying Viterbi Algorithm");
            sp.setMsg("Computing MPE: ");
            sp.setMax(ciph.length);
            for(int i = startIndex+1; i < ciph.length; i++){
                if(!isRunning){return i > 0 ? vit[i-1] : null;}
                if(solu!= null && solu.length > i && solu[i-1] != 26){ //If there is a hint, use the hint to extend current solution list
                    for(Pair<byte[],Double> prev : vit[i-1]){
                        byte[] plain = decode(ciph,i-order-1,prev.first,i-order-1,order+1); // add char here for use later
                        byte[] key = Arrays.copyOf(prev.first, prev.first.length + 1);
                        
                        key[key.length-1] = solu[i-1]; //Use hint
                        plain[plain.length-1] = dec(ciph[i-1],solu[i-1]);

                        //Calculate probabilities of last N-character sequence
                        double p1 = lProb(key,i-order-1,i);
                        double p2 = lProb(plain,0,order+1);

                        vit[i].add(new Pair<byte[],Double>(key, prev.second +p1 + p2));
                    }
                }else{
                    for(Pair<byte[],Double> prev : vit[i-1]){ //For each previous solution
                        byte[] plain = decode(ciph,i-order-1,prev.first,i-order-1,order+1); // add char here for use later

                        for(byte k = 0; k < alphabet.length; k++){ //Create a new solution by adding each character from the alphabet
                            byte[] key = Arrays.copyOf(prev.first, prev.first.length + 1);
                            
                            key[key.length-1] = k;
                            plain[plain.length-1] = dec(ciph[i-1],k);

                            //Calculate probabilities of last N-character sequence
                            double p1 = lProb(key,i-order-1,i);
                            double p2 = lProb(plain,0,order+1);

                            vit[i].add(new Pair<byte[],Double>(key, prev.second +p1 + p2));
                        }
                    }   
                }
                
                Collections.sort(vit[i]);
                if(vit[i].size() > depth){
                    vit[i] = new ArrayList<>(vit[i].subList(vit[i].size() - depth - 1, vit[i].size() ));
                }
                if(i > 0){
                    vit[i-1].clear(); //No need to keep this around since we carry state with us; Clearing to reclaim memory; consider refactoring to make vit one dimensional.
                }

                ps.clear();
                
                for(int j = 0; j < Math.min(NUM_SOLUTIONS,vit[i].size()); j++){ // Number of return
                    Pair<byte[],Double> max = vit[i].get(vit[i].size()-j-1);
                    ps.println("["+(j+1)+"] " + df.format(max.second) + "   -----------------------------\n" 
                            + unscrub(max.first,0,max.first.length,findSpaces?" ":"") + "\n" 
                            + unscrub(decode(ciph,0,max.first,0,max.first.length),0,max.first.length,findSpaces?" ":""));
                }
                ps.setPos(0);
                sp.setValue(i+1);
            }
            return vit[ciph.length-1];
        }
        
        public static ArrayList<Pair<byte[],Pair<Integer,Double>>> search(byte[] ciph, byte[] k, int n){
            ArrayList<Pair<byte[],Pair<Integer,Double>>> results = new ArrayList<Pair<byte[],Pair<Integer,Double>>>();
            
            for(int i = 0; i < ciph.length - k.length + 1; i++ ){
                byte[] p = decode(ciph,i,k,0,k.length);
                Pair<byte[],Pair<Integer,Double>> r = new Pair<byte[],Pair<Integer,Double>>(p,new Pair<>(i,(lProb(p) + lProb(k))/p.length));//+lProb(k)));

                results.add(r);
            }
            Collections.sort(results);
            if(results.size() > n){
                results = new ArrayList<>(results.subList(results.size() - n - 1, results.size() ));
            }
            return results;
        }
        public static void guess(String cipher, String key, int n){ //Output the n most likely positions for s and resulting plaintext
            byte[] k = scrub(key.getBytes(),key.length(),"");
            byte[] ciph = scrub(cipher.getBytes(),cipher.length(),"");
            
            ArrayList<Pair<byte[],Pair<Integer,Double>>> results = search(ciph,k,n);
            gps.clear();
            for(int j = 0; j < results.size(); j++){
                Pair<byte[],Pair<Integer,Double>> max = results.get(results.size()-j-1);
                gps.println(String.format("%.3e",max.second.second) + " --- " + max.second.first + " :: " + unscrub(max.first,0,max.first.length,findSpaces?" " : ""));
            }
        }
        //static HashMap<byte[],Double> probs = new HashMap<byte[],Double>();
        public static double lProb(String str){
            return lProb(scrub(str.getBytes(),str.length()));
        }
        public static double lProb(byte[] str){
            return lProb(str,0,str.length);
        }
        public static double lProb(byte[] s, int start, int end){ //Returns log probability of s given chars leading up to s
            if(end - start < 0) {
                System.out.println("Error!");
                try{
                    throw new Exception("Error!");
                }catch(Exception e){
                    System.err.println(Arrays.toString(s) + " " + start + " " + end);
                    e.printStackTrace();
                }
                return 0;
            }
            
            if(end-start > order + 1){
                double ret = 0;
                for(int i = start; i < end - order; i++){
                    ret += lProb(s,i,i+order+1);
                }
                return ret;
            }else{
                double ret = 0;

                double f1 = getFreq(s,start,end);
                //System.out.println(f1 + " " + RKC.unscrub(s, end,"") + " " + start );
                if(f1 == 0){
                    ret += EPSILON + lProb(s,start+1,end);
                    return ret;
                }else{
                    double f2 = getFreq(s,start,end - 1) + 1; //+1 for backoff smoothing
                    ret +=  Math.log(f1 / f2);
                    return ret;
                }
            }
        }
        public static int getFreq(byte[] key,int start, int end){
            if(freq.contains(key,start,end)){
                return freq.get(key,start,end);
            }else{
                return 0;
            }
        }

        
        public static byte enc(byte p, byte k){
            return (byte)((p + k) % 26);
        }
        
        public static char enc(char c, char k){
            System.out.println(scrub((""+c).getBytes(),1,"")[0] + " " + scrub((""+k).getBytes(),1,"")[0]);
            return unscrub(new byte[]{dec(scrub((""+c).getBytes(),1,"")[0],scrub((""+k).getBytes(),1,"")[0])},0,1,"").charAt(0);
        }
        
        public static byte dec(byte c, byte k){
            
            return (byte)((26 +  c - k) % 26);
        }
        
        
        public static char dec(char c, char k){
            System.out.println(scrub((""+c).getBytes(),1,"")[0] + " " + scrub((""+k).getBytes(),1,"")[0]);
            return unscrub(new byte[]{enc(scrub((""+c).getBytes(),1,"")[0],scrub((""+k).getBytes(),1,"")[0])},0,1,"").charAt(0);
        }
        
        
        public static byte[] encode(byte[] p, byte[] k){
            byte[] ret = new byte[p.length];

            if(k.length == 0){
                return ret;
            }

            for(int i = 0; i < p.length; i++){
                ret[i] = enc(p[i],k[i%k.length]);
            }
            return ret;
        }
        public static byte[] decode(byte[] c, int cstart, byte[] k, int kstart, int length){
            byte[] ret = new byte[length];
            
            if(k.length == 0){
                return ret;
            }
            
            for(int cnt = 0, i = cstart, j = kstart; cnt < length; cnt++, i++, j++){
                ret[cnt] = dec(c[i],k[j%k.length]);
            }
            return ret;
        }

        

    
    }
}
class StringEnum{
    public static final int MAX_SIZE = 100;
    byte[] alphabet;
    byte[] indexes = new byte[MAX_SIZE]; // up to 100 chars
    
    public StringEnum(byte[] alpha, int len){
        alphabet = alpha;
        Arrays.fill(indexes,(byte)(-1));
        for(int i = MAX_SIZE-1; i > MAX_SIZE - len; i--){
            indexes[i] = (byte)(alphabet.length - 1);
        }
    }
    
    public byte[] next(){
        
        indexes[MAX_SIZE-1] ++;
        int check = MAX_SIZE-1;
        while(indexes[check] == alphabet.length){
            indexes[check] = 0;
            indexes[check - 1]++;
            check --;
        }
        while(indexes[check] != -1){
            check --;
        }     
        byte[] ret = new byte[MAX_SIZE - check - 1];
        for(int i = check + 1; i < MAX_SIZE; i++){
            ret[i] = alphabet[indexes[i]];
        }
        return ret;
    }
    
}

class Pair<S,T extends Comparable<? super T>> implements Comparable<Pair<S,T>>{
    public S first;
    public T second;
    public Pair(S a, T b){
        first = a;
        second = b;
    }

    @Override
    public int compareTo(Pair<S, T> o) {
        return second.compareTo(o.second);
    }
}

abstract class PrintString{
    abstract void print(String s);
    abstract void println(String s);
    abstract void clear();
    abstract int getPos();
    abstract void setPos(int p);
    public final static PrintString STANDARD_OUT = new PrintString(){

        @Override
        public void print(String s) {
            System.out.print(s);
        }

        @Override
        public void println(String s) {
            System.out.println(s);
        }

        @Override
        public void clear() {
            // Do nothing
        }

        @Override
        public int getPos() {
            return 0;
        }

        @Override
        public void setPos(int p) {
            // Do nothing
        }
    };
}

interface ShowProgress{
    void setMax(int i);
    void setValue(int i);
    void setMsg(String s);
}
class FrequencyTree{
    Node head;
    
    public FrequencyTree(byte[] alphabet){
        Node.init(alphabet.length);
        head = new Node();
    }
    public FrequencyTree(){}
    public static final int TAIL = 0xABABABAB;
    public static final int HEAD = 0xBABABABA;
    public void save(String fname) throws IOException{
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(fname))));
        dos.writeInt(HEAD);
        dos.writeInt(Node.alphaSize);
        head.save(dos);
        dos.writeInt(TAIL);
        dos.flush();
        dos.close();
    }
    //File format: [int versionNo][int AlphaSize][Head Node ...]
    public static FrequencyTree load(String fname) throws IOException{
        FrequencyTree ret = new FrequencyTree();
        
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fname))));
        
        int fileHeader = dis.readInt();
        if(fileHeader != HEAD) {
            dis.close();
            throw new IOException("Invalid file format");
        }
        Node.setAlphaSize(dis.readInt());
        ret.head = Node.load(dis);
        int check = dis.readInt();
        dis.close();
        if(check == TAIL){
            System.out.println("Successfully read file");
        }
        return ret;
    }

    public int get(byte[] b){return head.get(b,0, b.length);}
    public int get(String s){return get(s.getBytes());}
    public int get(byte[] b, int start, int end){return head.get(b, start, end);}
    public void increment(byte[] b){head.increment(b, 0, b.length);}
    public void increment(String s){increment(s.getBytes());}
    public void increment(byte[] b, int start, int end){head.increment(b, start, end);}
    public boolean contains(byte[] b){return head.contains(b,0,b.length);}
    public boolean contains(String s){return contains(s.getBytes());}
    public boolean contains(byte[] b, int start, int end){return head.contains(b,start,end);}
    
    public Iterable<byte[]> Keys(final int size){
        return new Iterable<byte[]>(){
            @Override
            public Iterator<byte[]> iterator() {
                return new Iterator<byte[]>(){
                    byte[] curr = new byte[size];
                    {//Instance initializer
                        for(int i = 0; i < size; i++){
                            curr[i] = 0;
                        }
                    }
                    @Override
                    public boolean hasNext() {
                        byte[] b = Arrays.copyOf(curr, size);
                        b[b.length-1]++;
                        return head.next(b,0) != null;
                    }

                    @Override
                    public byte[] next() {
                        curr[curr.length-1]++;
                        curr = head.next(curr,0);
                        return Arrays.copyOf(curr, size);
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }
            
        };
    }
 
    
    static final class Node{
        int value = 0;
        Node[] children;
        
        private static int alphaSize = -1;
        static long numNodes = 0;
        public static void init(int alphaSize){
            Node.alphaSize = alphaSize;
        }
        public static void setAlphaSize(int n){
            Node.alphaSize = n;
        }
        public void save(DataOutputStream dos) throws IOException{
            if(value == 0){
                System.out.println("ERROR: Value should never equal 0");
            }
            dos.writeInt(value);
            if(children == null){
                dos.write(0);
            }else{
                dos.write(1);
                for(int i = 0; i < children.length; i++){
                    if(children[i] != null){
                        children[i].save(dos);
                    }else{
                        dos.writeInt(0);
                    }
                }
            }
        }
        public static Node load(DataInputStream dis) throws IOException{
            int tmp = dis.readInt();
            if(tmp == 0){
                return null;
            }
            
            Node ret = new Node();
            ret.value = tmp;

            if(dis.read() != 0){ //children != null
                ret.children = new Node[Node.alphaSize];
                for(int i = 0; i < alphaSize; i++){
                    ret.children[i] = load(dis);
                }
            }else{
                
                ret.children = null;
            }
            return ret;
        }
        
        public Node(){
            numNodes ++;
        }

        public final byte[] next(byte[] b, int idx){
            if(children == null){return null;}
            //System.out.println(b.length  + " " + idx);
            if(idx == b.length - 1){
                while(b[idx] < children.length && children[b[idx]] == null){
                    b[idx]++;
                }
                if(b[idx] == children.length){
                    b[idx] = 0;
                    return null;
                }else{
                    return b;
                }
            }else{
                byte[] ret = null;
                
                while(b[idx] < children.length && (children[b[idx]] == null || ((ret = children[b[idx]].next(b, idx+1)) == null))){
                    b[idx] ++;
                    
                }
                if(b[idx] == children.length){
                    b[idx] = 0;
                    return null;
                }else{
                    return ret;
                }
            }
        }
        public final boolean contains(byte[] b, int idx, int end){
            if(idx == end){
                return true;
            }
            if(children == null || children[b[idx]]==null){
                return false;
            }
            return children[b[idx]].contains(b,idx+1, end);
        }
        public final int get(byte[] b, int idx, int end){
            try{
            if(idx == end){
               // System.out.println(value);
                return value; //will throw exception if null
            }
            if(children == null) return 0;
            return children[b[idx]].get(b, idx+1, end);
            }catch(Exception e){
                e.printStackTrace();
                return 0;
            }
        }

        public final void increment(byte[] b, int idx, int end){
            value += 1;
            if(idx >= end){
                return;
            }
            if(children == null){children = new Node[alphaSize];}
            if(children[b[idx]] == null){
                children[b[idx]] = new Node();
            }
            children[b[idx]].increment(b,idx+1, end);
        }
    }
    
}
