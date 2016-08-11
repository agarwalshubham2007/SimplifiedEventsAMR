package simplifiedAMR_EventSeparation;
import simplifiedAMR_ParseTree.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.dom.ChildNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class App 
{
	
	public static int leftPos = 0;
	public static int rightPos = 1;
	//HashMap that maintains all verbs in the sentence
    public static HashMap<String, String> verbsMap = new HashMap<String, String>();
	
    public static void main( String[] args ) throws ParserConfigurationException, SAXException, IOException, FileNotFoundException
    {
//    	File inputFile = new File("/Users/Shubham/Documents/workspace/SimplifiedAMR-EventSeparation/src/main/java/simplifiedAMR_EventSeparation/sample.xml");
    	File inputFile = new File("/Users/Shubham/Documents/workspace/SimplifiedAMR-EventSeparation/src/main/java/simplifiedAMR_EventSeparation/amr-bank-v1.6.xml");
    	
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();
        System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
        NodeList nList = doc.getElementsByTagName("sntamr");
        
        for (int temp = 0; temp < nList.getLength(); temp++) {
        	Node nNode = nList.item(temp);
            System.out.println("\nCurrent Element :" + nNode.getNodeName());
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            	// resetting leftNode and rightNode values
            	leftPos = 0;
            	rightPos = 1;
            	
                Element eElement = (Element) nNode;
                
                // amr and sentence store the obvious entities respectively
                String sentence = eElement.getElementsByTagName("sentence").item(0).getTextContent();
                System.out.println("Sentence : " + sentence);
                
                String amr = eElement.getElementsByTagName("amr").item(0).getTextContent().trim().replaceAll("\n", "");
                amr = amr.replaceAll("(?m)(^ *| +(?= |$))", "").replaceAll("(?m)^$([\r\n]+?)(^$[\r\n]+?^)+", "$1");

                System.out.println("AMR : " + amr);
                TreeNode root = new TreeNode();
                
                //make parse tree of AMR
                makeParseTree(root, amr);

                // printing the parsed tree
                printParseTree(root);
                
                System.out.println("Continue(Y/N)");
//                int a = System.in.read();
                
                // storing POS of sentence
                sentencePOS(sentence);
                
                makeSimplifiedEvents(root);
                
             }
        }
        
    }

	private static void makeSimplifiedEvents(TreeNode root) {
		
	}

	private static void sentencePOS(String sentence) {
		// getting POS
        Properties props = new Properties();
        
        props.setProperty("annotators","tokenize, ssplit, pos, lemma");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotation = new Annotation(sentence);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence1 : sentences) {
            for (CoreLabel token: sentence1.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                String lemma = token.getString(CoreAnnotations.LemmaAnnotation.class);
                System.out.println(lemma);
                System.out.println(word + "/" + pos);
                
                if(isPOSVerb(pos)){
                	verbsMap.put(word, null);
                }
            }
        }
	}

	private static boolean isPOSVerb(String pos) {
		if(pos=="VB" || pos=="VBD" || pos=="VBG" || pos=="VBN" || pos=="VBP" || pos=="VBZ")
			return true;
		else
			return false;
	}

	private static void printParseTree(TreeNode root) {
		if(root.word==null) return;
		System.out.println("Node: "+root.word);
		System.out.println("# Children edges:"+ root.childEdge.size());
		for(int i=0;i<root.childEdge.size();i++)
        	System.out.println(root.childEdge.get(i));
		System.out.println("# Children nodes:"+ root.childNode.size());
		for(int i=0;i<root.childNode.size();i++)
        	System.out.println(root.childNode.get(i).word);
		
		System.out.println();
		
		for(int i=0;i<root.childNode.size();i++)
        	printParseTree(root.childNode.get(i));
	}

	private static void makeParseTree(TreeNode root, String amr) {
			while(rightPos<amr.length()){
				if(amr.charAt(leftPos) == '('){
					String temp;
					while(true){
						if(amr.charAt(rightPos) != '(' && amr.charAt(rightPos) != ')'){
							rightPos++;
						}
						else {break;}
					}
					
					temp=amr.substring(leftPos+1, rightPos);
					
					if(amr.charAt(rightPos)=='('){
						String parts[] = temp.split(":");
						if(parts.length==2){
							//extract root node and fill the object fields
							root.word = parts[0];
							fillNodeObjectFields(root, root.word);
							
							root.childEdge.add(parts[1]);
							leftPos=rightPos;
							rightPos++;
							TreeNode node = new TreeNode();
							makeParseTree(node, amr);
							root.childNode.add(node);
						}
						else if(parts.length>=3){
							root.word = parts[0];
							fillNodeObjectFields(root, root.word);
							for(int i=1;i<=parts.length-2;i++){
								String subEdges[] = parts[i].split(" ");
								root.childEdge.add(subEdges[0]);
								root.childNode.add(new TreeNode(subEdges[1]));
							}
							root.childEdge.add(parts[parts.length-1]);
							leftPos=rightPos;
							rightPos++;
							TreeNode node = new TreeNode();
							makeParseTree(node, amr);
							root.childNode.add(node);
						}
					}
					else{
						String parts[] = temp.split(":");
						if(parts.length==1){
							root.word = amr.substring(leftPos+1, rightPos);
							fillNodeObjectFields(root, root.word);
							rightPos++;
							leftPos=rightPos;
							return;
						}
						else if(parts.length>=2){
							root.word = parts[0];
							fillNodeObjectFields(root, root.word);
							for(int i=1;i<=parts.length-1;i++){
								String subEdges[] = parts[i].split(" ");
								root.childEdge.add(subEdges[0]);
								root.childNode.add(new TreeNode(subEdges[1]));
							}
							rightPos++;
							leftPos=rightPos;
							return;
						}
					}
				}
				else if(amr.charAt(leftPos) == ')'){
					rightPos++;
					leftPos=rightPos;
					return;
				}
				else{
					String temp;
					while(amr.charAt(rightPos)!='(' && amr.charAt(rightPos)!=')')
						rightPos++;
					temp = amr.substring(leftPos, rightPos);
					if(amr.charAt(rightPos)=='('){
						String parts[] = temp.split(":");
						if(parts.length==2){
							root.childEdge.add(parts[1]);
							leftPos=rightPos;
							rightPos++;
							TreeNode node = new TreeNode();
							makeParseTree(node, amr);
							root.childNode.add(node);
						}
						else if(parts.length>=3){
							for(int i=1;i<=parts.length-2;i++){
								String subEdges[] = parts[i].split(" ");
								root.childEdge.add(subEdges[0]);
								root.childNode.add(new TreeNode(subEdges[1]));
							}
							root.childEdge.add(parts[parts.length-1]);
							leftPos=rightPos;
							rightPos++;
							TreeNode node = new TreeNode();
							makeParseTree(node, amr);
							root.childNode.add(node);
						}
					}
					else{
						String parts[] = temp.split(":");
						for(int i=1;i<parts.length;i++){
							String subEdges[] = parts[i].split(" ");
							root.childEdge.add(subEdges[0]);
							root.childNode.add(new TreeNode(subEdges[1]));
						}
						return;
					}
				}
			}
	}

	private static void fillNodeObjectFields(TreeNode root, String word) {
		String parts[] = word.split("[/-]");
		if(parts.length==1){
			root.alias = parts[0].trim();
			root.word = parts[0].trim();
		}
		else if(parts.length==2){
			root.alias = parts[0].trim();
			root.word = parts[1].trim();
		}
		else if(parts.length >=3){
			root.alias = parts[0].trim();
			root.word = "";
			for(int i=1;i<parts.length-1;i++)
				root.word += parts[i].trim()+" ";
			
			String frameStr = parts[parts.length-1].trim();
			if(isNumber(parts[parts.length-1].trim().charAt(0)))
				root.frameNum = Integer.parseInt(frameStr);
		}
	}

	private static boolean isNumber(char charAt) {
		switch(charAt){
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9': return true;
		default: return false;
		}
	}
}
