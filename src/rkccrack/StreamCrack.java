/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rkccrack;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import rkccrack.RKCCrack.RKC;
import sun.misc.BASE64Decoder;
/**
 *
 * @author Kevin Hulin
 */
public class StreamCrack extends JPanel implements ComponentListener{
    
    
    //Taken from http://www.artofmanliness.com/wp-content/themes/revolution_magazine-20/images/800px-Morse_code_tree3.png
    public static final HashMap<String,String> MorseCodeLookup = new HashMap<String,String>();
    static{
        MorseCodeLookup.put(".", "E");
        MorseCodeLookup.put("..", "I");
        MorseCodeLookup.put("...", "S");
        MorseCodeLookup.put("....", "H");
        MorseCodeLookup.put(".....", "5");
        MorseCodeLookup.put("....-", "4");
        MorseCodeLookup.put("...-", "V");
        MorseCodeLookup.put("...-.", "/S"); //Understood
        MorseCodeLookup.put("...-..-", "$");
        MorseCodeLookup.put("...-.-", "[END]");//"OK"
        MorseCodeLookup.put("...--", "3");
        MorseCodeLookup.put("..-", "U");
        MorseCodeLookup.put("..-.", "F");
        MorseCodeLookup.put("..-..", "/E");
        MorseCodeLookup.put("..--", "/U");
        MorseCodeLookup.put("..--.", "/D");
        MorseCodeLookup.put("..--..", "?");
        MorseCodeLookup.put("..--.-", "_");
        MorseCodeLookup.put("..---", "2");
        
        MorseCodeLookup.put(".-", "A");
        MorseCodeLookup.put(".-.", "R");
        MorseCodeLookup.put(".-..", "L");
        MorseCodeLookup.put(".-...", "[WAIT]");
        MorseCodeLookup.put(".-..-", "/E");
        MorseCodeLookup.put(".-..-.", "\"");
        MorseCodeLookup.put(".-.-", "/A");
        MorseCodeLookup.put(".-.-.", "+");
        MorseCodeLookup.put(".-.-.-", ".");
        MorseCodeLookup.put(".--", "W");
        MorseCodeLookup.put(".--.", "P");
        MorseCodeLookup.put(".--..", "/p");
        MorseCodeLookup.put(".--.-", "/A");
        MorseCodeLookup.put(".--.-.", "@");
        MorseCodeLookup.put(".---", "J");
        MorseCodeLookup.put(".---.", "/J");
        MorseCodeLookup.put(".----", "1");
        MorseCodeLookup.put(".----.", "'");
        
        MorseCodeLookup.put("-", "T");
        MorseCodeLookup.put("-.", "N");
        MorseCodeLookup.put("-..", "D");
        MorseCodeLookup.put("-...", "B");
        MorseCodeLookup.put("-....", "6");
        MorseCodeLookup.put("-....-", "-");
        MorseCodeLookup.put("-...-", "=");
        MorseCodeLookup.put("-..-", "X");
        MorseCodeLookup.put("-..-.", "/");
        MorseCodeLookup.put("-.-", "K"); //OK TO BEGIN TRANSMISSION
        MorseCodeLookup.put("-.-.", "C");
        MorseCodeLookup.put("-.-..", "/C");
        MorseCodeLookup.put("-.-.-", "[BEGIN]");// "AK"     
        MorseCodeLookup.put("-.-.-.", ";");
        MorseCodeLookup.put("-.-.--", "!");
        MorseCodeLookup.put("-.--", "Y");
        MorseCodeLookup.put("-.--.", "/H");
        MorseCodeLookup.put("-.--.-", "(");
        
        MorseCodeLookup.put("--", "M");
        MorseCodeLookup.put("--.", "G");
        MorseCodeLookup.put("--..", "Z");
        MorseCodeLookup.put("--...", "7");
        MorseCodeLookup.put("--..--", ",");
        MorseCodeLookup.put("--.-", "Q");
        MorseCodeLookup.put("--.-.", "/G");
        MorseCodeLookup.put("--.--", "/N");
        MorseCodeLookup.put("---", "O");
        MorseCodeLookup.put("---.", "/O");
        MorseCodeLookup.put("---..", "8");
        MorseCodeLookup.put("---...", ":");
        MorseCodeLookup.put("----", "[CH]");
        MorseCodeLookup.put("----.", "9");
        MorseCodeLookup.put("-----", "0");        
        

        
    }
    public static final int MIN_CIPHER_LENGTH = 2; //Handled in Cipher Solver class
    public static final int MAX_DECODE_DEPTH = 3;
    
    Font monoFont = new Font(Font.MONOSPACED, 1,11);
    JTextArea out = new JTextArea();
    JTextArea in = new JTextArea();
    JSplitPane sp;
    public StreamCrack(){
        
        
        setLayout(new BorderLayout());
        
        sp = new JSplitPane();
        sp.setOrientation(JSplitPane.VERTICAL_SPLIT);
        sp.setTopComponent(new JScrollPane(out));
        sp.setBottomComponent(new JScrollPane(in));
        

        
        out.setEditable(false);
        out.setLineWrap(true);
        out.setFont(monoFont);
        
        in.getDocument().addDocumentListener(new DocumentListener(){

            @Override
            public void insertUpdate(DocumentEvent e) {
                gogo();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                gogo();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                gogo();
            }
            
            public void gogo(){
                
                System.out.println(in.getText());
                if(in.getText().length() > MIN_CIPHER_LENGTH){
                    String result = crack(in.getText());
                    if(result != null){
                        System.out.println("Result: " + result);
                    }
                    out.setCaretPosition(out.getText().length());
                }
            }
        
        });
        
        add(sp,BorderLayout.CENTER);
        
        //setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(800,800);
        setVisible(true);

        //setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        addComponentListener(this);
    }
    
    public String crack(String cipher){
        double maxProb = Double.NEGATIVE_INFINITY;
        Solution sol = null;
        for(Cipher c : decode(cipher)){
            Solution s = c.solve(out);
            if(s != null){
                if(s.prob > maxProb){
                    maxProb = s.prob;
                    sol = s;
                }
            }
        }
        if(sol != null){
            out.append("\n");
            out.append(sol.toString());
            out.append("\n");
            return sol.getValueString();
        }else{
            out.append("No Solutions Found\n");
            return null;
        }
    }
    public boolean isBase64Char(char c){
        return Character.isDigit(c) || Character.isLetter(c) || c == '+' || c == '/' || c == '=';
    }
    public boolean isHexDigit(char c){
        return Character.isDigit(c) || "ABCDEFabcdef".contains(""+c);
    }
    public HashSet<Cipher>decode(String s){
        return decode(s, 0);
    }
    public HashSet<Cipher> decode(String s, int depth){
        
        HashSet<Cipher> ret = new HashSet<Cipher>();
        s = s.replace("\n"," ").replace("\t"," ").replace("\r"," ").replace("\\s"," ");
        boolean isNumbers = true;
        boolean isHex = true;
        boolean isAlpha = true;
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean isMorseCode = true;
        boolean isSpaced = false;
        boolean isBase64 = true;
        boolean isBinary = true;
        boolean isOctal = true;
        
        for(char c : s.toCharArray()){
            if(c == ' ' ){
                isSpaced = true;
                continue;
            }
            if(!Character.isLetter(c)){
                isAlpha = false;
            }
            if(!Character.isDigit(c)){
                isNumbers = false;
            }
            if(!isBase64Char(c)){
                isBase64 = false;
            }
            if(!Character.isDigit(c) || c == '9' || c == '8'){
                isOctal = false;
            }
            if(Character.isUpperCase(c)){
                hasUpperCase = true;
            }
            if(Character.isLowerCase(c)){
                hasLowerCase = true;
            }
            if(!isHexDigit(c)){
                isHex = false;
            }
            if(c != '1' && c != '0'){
                isBinary = false;
            }
            if(c != '.' && c != '-'){
                isMorseCode = false;
            }            
        }
                
        System.out.println("Possible encodings: \n"
                + (isAlpha ? "ASCII\n" : "")
                + (isNumbers ? "Numeric\n" : "")
                + (isBase64 ? "Base64\n" : "")
                + (isOctal ? "Ocatal\n" : "")
                + (isHex ? "Hexadecimal\n" : "")
                + (isBinary ? "Binary\n" : "")
                + (isMorseCode ? "Morse Code\n" : ""));
        
        
        if(isNumbers){
            ArrayList<ArrayList<Integer>> al = new ArrayList<ArrayList<Integer>>(); //Possible decodings
        
            if(isSpaced){
                if(isBinary){
                    ArrayList<Integer> t = new ArrayList<Integer>();
                    for(String ss : s.split(" ")){
                        t.add(Integer.parseInt(ss,2));
                    }
                    al.add(t);
                }
                if(isOctal){
                    ArrayList<Integer> t = new ArrayList<Integer>();
                    for(String ss : s.split(" ")){
                        t.add(Integer.parseInt(ss,8));
                    }
                    al.add(t);
                }
                ArrayList<Integer> t = new ArrayList<Integer>();
                
                //Decimal
                for(String ss : s.split(" ")){
                    t.add(Integer.parseInt(ss));
                }
                al.add(t);


            }else{
                if(isBinary){
                    
                    if(s.length() % 8 == 0){ //8 bits == 1 char
                        ArrayList<Integer> t = new ArrayList<Integer>();
                        for(int i = 0; i < s.length(); i += 8){
                            t.add(Integer.parseInt(s.substring(i,i+8),2));
                        }
                        al.add(t);
                     
                    }
                    if(s.length() % 5 == 0){ //0 - 31 ~ 26 A-Z
                        ArrayList<Integer> t = new ArrayList<Integer>();
                        for(int i = 0; i < s.length(); i += 5){
                            t.add(Integer.parseInt(s.substring(i,i+5),2));
                        }
                        al.add(t);
                    }
                    if(s.length() % 4 == 0){ //Nibbles?
                        ArrayList<Integer> t = new ArrayList<Integer>();
                        for(int i = 0; i < s.length(); i += 4){
                            t.add(Integer.parseInt(s.substring(i,i+4),2));
                        }
                        al.add(t);
                    }
                    
                    //Possible morse code
                    if(depth < MAX_DECODE_DEPTH){
                        ret.addAll(decode(s.replace("1",".").replace("0","-"),depth+1));
                        ret.addAll(decode(s.replace("0",".").replace("1","-"),depth+1));
                    }
                }
                
                if(s.length() % 2 == 0){ //Split to numbers on even edges
                    ArrayList<Integer> t = new ArrayList<Integer>();
                    for(int i = 0; i < s.length(); i += 2){
                        t.add(Integer.parseInt(s.substring(i,i+2)));
                    }
                    al.add(t);
                }
            }
            
            
            //Decode numbers
            for(ArrayList<Integer> t : al){
                boolean isAscii = true;
                boolean isLetters = true;
                for(int tt : t){
                    try{
                        if(tt < 0 || tt >= 26){
                            isLetters = false;
                        }
                        if(String.valueOf((char)tt).matches("[^\\p{Print}]")){
                            isAscii = false;
                        }
                    }catch(Exception e){
                        isAscii = false; isLetters = false;
                        break;
                    }
                }
                if(isLetters){
                    StringBuilder ss = new StringBuilder();
                    for(int tt : t){
                        ss.append((char)('A' + tt));
                    }
                    if(depth < MAX_DECODE_DEPTH){
                        ret.addAll(decode(ss.toString(),depth+1));
                    }
                }
                if(isAscii){
                    StringBuilder ss = new StringBuilder();
                    for(int tt : t){
                        ss.append((char)tt);
                    }
                    if(depth < MAX_DECODE_DEPTH){
                        ret.addAll(decode(ss.toString(),depth+1));
                    }
                }
                if(depth < MAX_DECODE_DEPTH){
                    
                    StringBuilder ss = new StringBuilder(); //Mod 256
                    for(int tt : t){
                        ss.append((char)(tt % 256));
                    }
                    ret.addAll(decode(ss.toString(),depth+1));
                    
                    ss = new StringBuilder(); //Mod 26
                    for(int tt : t){
                        ss.append((char)(tt % 26) + 'A');
                    }
                    ret.addAll(decode(ss.toString(),depth+1));
                }
            }
        }
        
        if(isMorseCode){
            if(isSpaced){
                StringBuilder dec = new StringBuilder();
                for(String l : s.split(" ")){
                    if(MorseCodeLookup.containsKey(l)){
                        dec.append(MorseCodeLookup.get(l));
                    }else{
                        dec.append("<" + l + ">");
                    }
                }
                if(depth < MAX_DECODE_DEPTH){
                    ret.addAll(decode(dec.toString(),depth+1));
                }
            }else{
                ret.add(new MorseCodeCipher(s));
            }
        }
        
        if(isAlpha){
            ret.add(new AlphaCipher(s.toString()));
            if(depth < MAX_DECODE_DEPTH){//Check the reverse
                ret.addAll(decode(new StringBuffer(s).reverse().toString(),depth+1));
            }
        }
        
        if(isBase64){
            if(depth < MAX_DECODE_DEPTH){
                try{
                    BASE64Decoder d = new BASE64Decoder();
                    byte[] decodedBytes = d.decodeBuffer(s);
                    ret.addAll(decode(new String(decodedBytes),depth+1));

                }catch(Exception e){
                    //Not base64
                }
            }
        }
        
        return ret;
    }
    

    public static void main(String[] args){
        StreamCrack sc = new StreamCrack();
    }
    
    @Override
    public void componentResized(ComponentEvent e) {
        sp.setDividerLocation(.9);
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
}

abstract class Cipher<T>{
    String name = "Cipher";
    ArrayList<Solver> solvers = new ArrayList<Solver>();
    T cipher;
    
    @Override
    public boolean equals(Object o){
        if(o instanceof Cipher){
            return this.cipher.equals(((Cipher)o).cipher);
        }
        return false;
    }
    @Override
    public int hashCode(){
        return cipher.hashCode();
    }
    public Solution solve(JTextArea ta ){
        double maxProb = Double.NEGATIVE_INFINITY;
        Solution sol = null;
        for(Solver s : solvers){
            try{
                
                Solution t = s.solve(cipher);
                if(t != null){
                    ta.append(s.name + "\t::\t" + t.prob + "\t::\t" + t.getKeyString() + "\t::\t" + t.getValueString());
                    ta.append("\n");
                    if(t.prob > maxProb){
                        maxProb = t.prob;
                        sol = t;
                    }
                }
            }catch(Exception e){
                ta.append("An execption occured in " + s.name + "\n");
                e.printStackTrace();
                ta.append(e.getMessage() + " :: " + e.getLocalizedMessage());
                ta.append("\n");
            }
        }
        return sol;
    }
    
}

class AlphaCipher extends Cipher<String>{
    public AlphaCipher(String cipher){
        name = "Alpha Cipher";
        this.cipher = cipher;
        
        solvers.add(new CaesarCipherSolver());
        solvers.add(new VigenereCipherSolver());
        solvers.add(new AtbashCipherSolver());
        solvers.add(new ScytaleCipherSolver());
        solvers.add(new RailFenceCipherSolver());
    }
}
class NumberCipher extends Cipher<int[]>{
    
    public NumberCipher(int[] cipher){
        name = "Number Cipher";
        this.cipher = cipher;
        
        
    }
}

class MorseCodeCipher extends Cipher<String>{
    
    public MorseCodeCipher(String s){
        name = "Morse Code Cipher";
        this.cipher = s;
    }
}

abstract class Solution<T,K>{
    String name = "DEFAULT NAME";
    T value;
    K key;
    double prob;
    public Solution(T value, K key, double prob){
        this.value = value;
        this.key = key;
        this.prob = prob;
    }
    abstract public String getValueString();
    abstract public String getKeyString();
    public String toString(){
        return "++++ " + name + " (Prob: " + prob + ") ++++\n" 
                + "Key: " + getKeyString() + "\n"
                + "Plain text: " + getValueString() + " \n ";
    }
}

class CaesarSolution extends Solution<String,Integer>{
    public CaesarSolution(String value, Integer key, double prob){
        super(value,key,prob);
        name = "Caesar Solution";
    }
    @Override
    public String getValueString() {
        return value;
    }
    
    @Override
    public String getKeyString(){
        return "" + key;
    }
}

class VigenereSolution extends Solution<String,String>{
    public VigenereSolution(String value, String key, double prob){
        super(value,key,prob);
        name = "Vigenere Solution";
    }

    @Override
    public String getValueString() {
        return value;
    }

    @Override
    public String getKeyString() {
        return key;
    }
}

class AtbashSolution extends Solution<String,Object>{
    public AtbashSolution(String value, String key, double prob){
        super(value,key,prob);
        name = "Atbash Solution";
    }
    
    @Override
    public String getValueString() {
        return value;
    }

    @Override
    public String getKeyString() {
        return "";
    }
    
}

abstract class Solver<T,U>{
    String name = "Solver";
    int min_length = 10;
    abstract Solution solve(T p);
    abstract String decrypt(T c, U key);
}
class AtbashCipherSolver extends Solver<String,Object>{

    public AtbashCipherSolver(){
        super();
        name = "Atbash Cipher Solver";
        min_length = 1;
    }
    
    @Override
    Solution solve(String s) {
        if(s.length() < min_length) return null;
        String sol = decrypt(s,null);
        return new AtbashSolution(sol,null,RKC.lProb(sol));
    }

    @Override
    String decrypt(String s, Object key) {
        s = s.toUpperCase();
        StringBuilder ret = new StringBuilder();
        for(char c : s.toCharArray()){
            if(c == ' '){
                ret.append(c);
            }else{
                ret.append((char)(('Z' - c + 'A')));
            }
        }
        return ret.toString();
    }
    
}
class CaesarCipherSolver extends Solver<String,Integer>{

    public CaesarCipherSolver(){
        super();
        name = "Caesar Cipher Solver";
        min_length = 3;
    }
    @Override
    Solution solve(String s) {
        if(s.length() < min_length) return null;
        System.out.println("===== Caesar Cipher Solver =====");
        int maxIDX = 0;
        double maxProb = Double.NEGATIVE_INFINITY;
        for(int k = 0; k < 26; ++k){
            String p = decrypt(s,k);
            double prob = RKC.lProb(p);
            System.out.println(k + " :: " + prob + " :: " + p);
            if(prob > maxProb){
                maxProb = prob;
                maxIDX = k;
            }
        }

        return new CaesarSolution(decrypt(s,maxIDX),maxIDX,maxProb);
    }

    @Override
    String decrypt(String s, Integer key) {
        s = s.toUpperCase();
        StringBuilder ret = new StringBuilder();
        for(char c : s.toCharArray()){
            if(c == ' '){
                ret.append(c);
            }else{
                ret.append((char)((((c - 'A') - key + 26) % 26) + 'A'));
            }
        }
        return ret.toString();
    }
}

class VigenereCipherSolver extends Solver<String, String>{
    public static final double IC_ENGLISH = 1.73;
    public static final double IC_ACCEPT_DELTA = 0.4;
    public static final int VIGENERE_MAX_KEY_LENGTH = 20;
    
    public VigenereCipherSolver(){
        super();
        name = "Vigenere Cipher Solver";
        min_length = 20;
    }
    
    @Override
    Solution solve(String s) {
        if(s.length() < min_length) return null;
        System.out.println(" +++ Vigenere Solver +++");
        s = s.toUpperCase().replaceAll("\\s", "");
        ArrayList<Integer> keyLenCandiates = new ArrayList<Integer>();
        for(int i = 2; i < Math.min(s.length(), VIGENERE_MAX_KEY_LENGTH); i++){
            //1) Determine key length
            String[] subs = new String[i];
            
            for(int j = 0; j < i; j ++){subs[j] = "";}
            
            for(int j = 0; j < s.length(); j += i){
                for(int k = 0; k < i && j+k < s.length(); k++){
                    subs[k] += s.charAt(j + k); 
                }
            }
            
            
            double icTotal = 0;
            for(int j = 0; j < i; j++){
                double ic = Util.indexOfCoincidence(subs[j]); 
                icTotal += ic;
            }
            icTotal /= (double)i;
            System.out.println("IC(keylen = " + i + ") = " + icTotal  + " (diff: " + Math.abs(icTotal - IC_ENGLISH) + ")");
            if(Math.abs(icTotal - IC_ENGLISH) < IC_ACCEPT_DELTA){
                keyLenCandiates.add(i);
                System.out.println("Candidate keylength: " + i);
            }
        }
        
        String mostProbKey = null;
        String mostProbPT = null;
        double maxKeyProb = Double.NEGATIVE_INFINITY;
        
        for(int keyLength : keyLenCandiates){
            //System.out.println("Key length = " + keyLength);

            //2) Determine offset per key character
            String[] subs = new String[keyLength];
            for(int j = 0; j < keyLength; j ++){subs[j] = "";}
            for(int j = 0; j < s.length(); j += keyLength){
                for(int k = 0; k < keyLength && j+k < s.length(); k++){
                    subs[k] += s.charAt(j + k); 

                }
            }


            String key = "";
            CaesarCipherSolver ccs = new CaesarCipherSolver();
            for(int i = 0; i < keyLength; i++){
                int minIdx = 0;
                double minDistance = Double.MAX_VALUE;
                for(int j = 0; j < 26; j++){
                    String pt = ccs.decrypt(subs[i],j);
                    double p = Util.distributionDistance(pt) - Util.probChars(pt);
                    if(p < minDistance){
                        minDistance = p;
                        minIdx = j;
                    }
                }
                key += (char)('A' + minIdx);
                //System.out.println("Key[" + i + "] = " + (char)('A' + minIdx) + " :: " + minDistance);
            }

            String p = decrypt(s,key);
            double prob = RKC.lProb(p);
            if(prob > maxKeyProb){
                mostProbKey = key;
                maxKeyProb = prob;
                mostProbPT = p;
            }
            //System.out.println("Key: " + key);
            //System.out.println("Plaintext: " + p);
        }
        //System.out.println("Most probable plain text: " + mostProbPT);
        if(mostProbPT == null){ return null; }
        
        return new VigenereSolution(mostProbPT,mostProbKey,maxKeyProb);
       
    }

    @Override
    String decrypt(String s, String key) {
        s = s.toUpperCase();
        StringBuilder ret = new StringBuilder();
        int idx = 0;
        for(char c : s.toCharArray()){
            if(c == ' '){
                ret.append(c);
            }else{
                ret.append((char)((((c - 'A') - (key.charAt(idx%key.length()) - 'A') + 26) % 26) + 'A'));
                idx += 1;
            }
        }
        return ret.toString();
    }

    
}



class ScytaleCipherSolution extends Solution<String,Integer>{
    public ScytaleCipherSolution(String value, Integer key, double prob){
        super(value,key,prob);
        name = "Scytale Cipher Solution";
        
    }
    @Override
    public String getValueString() {
        return value;
    }

    @Override
    public String getKeyString() {
        return "" + key;
    }
    
}

class ScytaleCipherSolver extends Solver<String,Integer>{
    public static final int SCYTALE_MAX_KEY = 30;
    
    public ScytaleCipherSolver(){
        super();
        name = "Scytale Cipher Solver";
        min_length = 5;
    }
    
    @Override
    Solution solve(String s) {
        if(s.length() < min_length) return null;
        System.out.println("===== Scytale Cipher Solver =====");
        int maxKey = 0;
        double maxProb = Double.NEGATIVE_INFINITY;
        for(int i = 1; i < SCYTALE_MAX_KEY; ++i){
            String p = decrypt(s,i);
            double prob = RKC.lProb(p);
            if(prob > maxProb){
                maxProb = prob;
                maxKey = i;
            }
        }
        String p = decrypt(s,maxKey);
        return new ScytaleCipherSolution(p,maxKey,RKC.lProb(p));
    }

    @Override
    String decrypt(String c, Integer key) {
        c = c.replaceAll("\\s","").toUpperCase();
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < key; i++){
            for(int j = i; j < c.length(); j+=key){
                ret.append(c.charAt(j));
            }
        }
        return ret.toString();
    }
    
}


class RailFenceCipherSolution extends Solution<String,Pair<Integer,Integer>>{

    
    public RailFenceCipherSolution(String value, Pair<Integer,Integer> key, double prob){
        super(value,key,prob);
        name = "RailFence Cipher Solution";
        
    }
    @Override
    public String getValueString() {
        return value;
    }

    @Override
    public String getKeyString() {
        return "r=" + key.first + ";o="+key.second;
    }
    
}

class RailFenceCipherSolver extends Solver<String,Pair<Integer,Integer>>{
    public static final int MAX_NUM_RAILS = 10;
    public RailFenceCipherSolver(){
        super();
        name = "RailFence Cipher Solver";
        min_length = 5;
    }
    @Override
    Solution solve(String p) {
        double maxProb = Double.NEGATIVE_INFINITY;
        Pair<Integer,Integer> maxKey = null;
        for(int n = 2; n < MAX_NUM_RAILS; n++){
            for(int offset = 0; offset < (n - 1) * 2; offset++){
                Pair<Integer,Integer> key = new Pair<Integer,Integer>(n,offset);
                String pt = decrypt(p, key);
                double prob = RKC.lProb(pt);
                if(prob > maxProb){
                    maxProb = prob;
                    maxKey = key;
                }
            }
        }
        String pt = decrypt(p,maxKey);
        return new RailFenceCipherSolution(pt,maxKey,RKC.lProb(pt));
    }

    @Override
    String decrypt(String c, Pair<Integer, Integer> key) {
        int numRails = key.first;
        int startIndex = key.second;
        
        /*
         * T               A
         *   E           R   I           E
         *     S       G       L       C
         *       T   N           F   N
         *         I               E
         * 
         *        T
         * A     R E
         *  N   E   S
         *   O H     T
         *    T
         * 
         * TARENESOHTT
         * 
         *  N     T     E
         * O E   E E   L A
         *    M R   S P   S
         *     O     T     E
         * 
         * NTEOEELAMRSPSOTE
         * 
         * R = 5, Startindex = 0
         * Startindex <= (R-1)*2
         * => TAERIESGLCTNFNIE
         * */
        
        if(startIndex >= (numRails - 1) * 2){
            System.err.println("Error: start index > (R - 1) * 2");
            return null;
        }
        
        String[] rails = new String[numRails];

        int direction = 1;
        int rowIndex = startIndex;
        if(startIndex >= numRails - 1){ //Start index > numRails 
            direction = -1;
            rowIndex = (numRails - 1) * 2 - startIndex - 1;
        }
        //System.out.println("Start index: " + startIndex + "; Row index: " + rowIndex);
        int[] numCharsPerRail = new int[numRails];
        for(int i = 0; i < c.length(); i++){ //Simulate rail fence with provided cipher length and start index to determine how many characters per rail
            
            numCharsPerRail[rowIndex] += 1;
            
            if((rowIndex == 0 && direction == -1) || (rowIndex == numRails - 1 && direction == 1)){
                direction = -direction;
            }
            rowIndex += direction;
        }
        
        int idx = 0;
        for(int i = 0; i < numRails; i++){
            rails[i] = c.substring(idx,idx + numCharsPerRail[i]);
            idx += numCharsPerRail[i];
        }
        
        direction = 1;
        rowIndex = startIndex;
        if(startIndex >= numRails - 1){ //Start index > numRails 
            direction = -1;
            rowIndex = (numRails - 1) * 2 - startIndex - 1;
        }
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < c.length(); i++){
            ret.append(rails[rowIndex].charAt(0));
            rails[rowIndex] = rails[rowIndex].substring(1);
            
            if((rowIndex == 0 && direction == -1) || (rowIndex == numRails - 1 && direction == 1)){
                direction = -direction;
            }
            rowIndex += direction;
        }
        
        if(ret.toString().length() != c.length()){
            System.out.println("Text length mismatch");
            return null;
        }
        
        return ret.toString();
        
    }
    
}

class Util{
    public static double probChars(String s){
        double ret = 1;
        for(char c : s.toCharArray()){
            ret += RKC.lProb("" + c);
        }
        return ret;
    }
    public static HashMap<Character,Integer> frequency(String s){
        HashMap<Character,Integer> ret = new HashMap<Character,Integer>();
        //System.out.println(s);
        for(char c : s.toCharArray()){
            if(c == ' ') continue;
            //System.out.println("" + c);
            if(ret.containsKey(c)){
                ret.put(c, ret.get(c) + 1);
            }else{
                ret.put(c,1);
            }
        }
        return ret;
    }
    public static double indexOfCoincidence(String s){
        HashMap<Character,Integer> freq = frequency(s);
        double ret = 0.0;
        //int check = 0;
        for(char c : freq.keySet()){
            ret += freq.get(c) * (freq.get(c) - 1);
            //check += freq.get(c);
        }
        //System.out.println("Check: " + check + " =? " + s.length());
        return ret / (double)(s.length() * (s.length() - 1) / 26.0);
    }
    
    public static double distributionDistance(String s){
        HashMap<Character,Integer> freq = frequency(s);
        double ret = 0;
        for(int i = 0; i < 26; i++){
            if(freq.containsKey((char)('A' + i))){
                ret += Math.abs(Math.exp(RKC.lProb("" + (char)('A' + i))) - (double)freq.get((char)('A' + i)) / (double)s.length());
            }else{
                ret += Math.exp(RKC.lProb("" + (char)('A' + i)));
            }
        }
        return ret;
    }
}