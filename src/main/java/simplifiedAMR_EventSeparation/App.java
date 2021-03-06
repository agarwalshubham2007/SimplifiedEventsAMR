package simplifiedAMR_EventSeparation;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.dom.ChildNode;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
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
import simplifiedAMR_Tree.*;

public class App 
{
	
	public static int leftPos = 0;
	public static int rightPos = 1;
	//HashMap that maintains all verbs in the sentence
    public static HashMap<String, String> verbsMap = new HashMap<String, String>();
    // arraylist having root node of all events in a sentence
    public static List<TreeNode> eventRootList = new ArrayList<TreeNode>();
	
    public static void main( String[] args ) throws ParserConfigurationException, SAXException, IOException, FileNotFoundException
    {
//    	File inputFile = new File("/Users/Shubham/Documents/workspace/SimplifiedAMR-EventSeparation/src/main/java/simplifiedAMR_EventSeparation/sample.xml");
    	File inputFile = new File("/Users/Shubham/Documents/workspace/SimplifiedAMR-EventSeparation/src/main/java/simplifiedAMR_EventSeparation/amr-bank-v1.6.xml");
    	
    	ObjectMapper mapper = new ObjectMapper();
        JsonNode sentencesJson = mapper.createArrayNode();
    	
    	
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
                
                //make parse tree of AMR : ParseTree.java
                ParseTree.makeParseTree(root, amr);
                
                //resolve ARG-Of in parse tree
                ParseTree.resolveArgOf(root);

                // printing the parsed tree
                printParseTree(root);
                
                System.out.println("Continue(Y/N)");
//                int a = System.in.read();
                
                // storing POS of sentence
                sentencePOS(sentence);
                
                SimplifiedEvents.makeSimplifiedEvents(root, null, false);
                
                //print simplified events
                printSimplifiedEvents();
                
                //print separated events to file in json
                File outputFile = new File("/Users/Shubham/Documents/workspace/SimplifiedAMR-EventSeparation/src/main/java/simplifiedAMR_EventSeparation/simplifiedEvents.json");
                
               
                JsonNode sentenceJson = mapper.createObjectNode();
                ((ObjectNode) sentenceJson).put("sentence", sentence);
                JsonNode eventsJson = mapper.createArrayNode();
                
                for(int i=0;i<eventRootList.size();i++){
                	((ArrayNode) eventsJson).add(createEventJsonObject(eventRootList.get(i), mapper));
                }
                
                ((ObjectNode) sentenceJson).put("events", eventsJson);
                ((ArrayNode) sentencesJson).add(sentenceJson);
                
                mapper.writeValue(outputFile, sentencesJson);
                
                // empty the eventRootList for current sentence
                eventRootList.clear();
                
                
            }
        }
        
    }

	/*private static void resolveArgOf(TreeNode root) {
		// if this is leaf node RETURN
		if(root.childEdge.size()==0) return;
		
		
		// check if this node has child ARG-Of
		for(int i=0;i<root.childEdge.size();i++){
			if(isEdgeArgOf(root, i)){	
				System.out.println("HOORAH " + root.childNode.get(i).word + " is ARG-OF node");
				//clone this node and make it child of ARG-of
				TreeNode cloneRoot = new TreeNode();
				cloneNodeForArgOf(root, cloneRoot);
				root.childNode.get(i).childNode.add(cloneRoot);
				root.childNode.get(i).childEdge.add(root.childEdge.get(i).substring(0, 4)+" ");
			}
			resolveArgOf(root.childNode.get(i));
		}
	}

	private static void cloneNodeForArgOf(TreeNode root, TreeNode cloneRoot) {
		if(root.childEdge.size()>0){
			cloneRoot.word = root.word;
			cloneRoot.alias = root.alias;
			cloneRoot.frameNum = root.frameNum;
			cloneRoot.childrenCount = root.childrenCount;
			for(int j=0;j<root.childEdge.size();j++){
				if(!isEdgeArgOf(root, j)){
					cloneRoot.childEdge.add(root.childEdge.get(j));
					cloneRoot.childNode.add(root.childNode.get(j));
//					cloneNodeForArgOf(root.childNode.get(j), cloneRoot.childNode.get(cloneRoot.childNode.size()-1));
				}
			}
		}
		else if(root.childEdge.size()==0){
			cloneRoot.word = root.word;
			cloneRoot.alias = root.alias;
			cloneRoot.frameNum = root.frameNum;
			cloneRoot.childrenCount = root.childrenCount;
		}
	}

	private static boolean isEdgeArgOf(TreeNode root, int i) {
		if(root.childEdge.get(i).substring(0, 3).equals("ARG") && root.childEdge.get(i).substring(root.childEdge.get(i).length()-3, root.childEdge.get(i).length()-1).equals("of")){
			System.out.println("ARGOF EDGE YES!!");
			return true;
		}
		else { 
			System.out.println("ARGOF EDGE NO!! -- " + root.childEdge.get(i) + " " + root.childEdge.get(i).length());
			return false;
		}
	}*/

	private static JsonNode createEventJsonObject(TreeNode nd, ObjectMapper mapper) {
		JsonNode eventJson = mapper.createObjectNode();
		((ObjectNode) eventJson).put("node", nd.word);
		for(int i=0;i<nd.childEdge.size();i++){
			if(nd.childNode.get(i).childEdge.size()==0)
				((ObjectNode) eventJson).put(nd.childEdge.get(i), nd.childNode.get(i).word);
			else{
				((ObjectNode) eventJson).put(nd.childEdge.get(i), createEventJsonObject(nd.childNode.get(i), mapper));
			}
		}
		
		return eventJson;
	}

	private static void printSimplifiedEvents() {
		System.out.println("Number of events: " + eventRootList.size());
	
		for(int i=0;i<eventRootList.size();i++){
			System.out.println("*******************************");
			printParseTree(eventRootList.get(i));
			System.out.println("*******************************");
		}
	}

	/*private static void makeSimplifiedEvents(TreeNode root, TreeNode eventNode, boolean checkArraylist) {
		// if current node is event
		if(isNodeEvent(root)){
			System.out.println("Yes " + root.word +" is an event" );
			// make new event root node
			TreeNode eventRoot = new TreeNode(root.word, root.alias);
			eventRootList.add(eventRoot);
			System.out.println(eventRootList.size());
			// for every child node of current node
			int ctr=0;
			for(int i=0;i<root.childEdge.size();i++){
				// if child node is not event
				if(!isNodeEvent(root.childNode.get(i))){
					System.out.println(root.childNode.get(i).word+" is not an event");
					eventRoot.childEdge.add(root.childEdge.get(i));
					eventRoot.childNode.add(new TreeNode(root.childNode.get(i).word, root.childNode.get(i).alias));
					makeSimplifiedEvents(root.childNode.get(i),eventRoot.childNode.get(ctr), true);
					ctr++;
				}
				else{
					makeSimplifiedEvents(root.childNode.get(i),null, false);
				}
			}
		}
		// if current node is not event 
		// ***** ARG-Of not considered yet! *****
		else{
			// current node has to be made child of latest event in eventRootList
			if(checkArraylist){
				int ctr=0;
				for(int i=0;i<root.childEdge.size();i++){
					if(!isNodeEvent(root.childNode.get(i))){
						eventNode.childEdge.add(root.childEdge.get(i));
						eventNode.childNode.add(new TreeNode(root.childNode.get(i).word, root.childNode.get(i).alias));
						makeSimplifiedEvents(root.childNode.get(i),eventNode.childNode.get(ctr), true);
						ctr++;
					}
					else{
						makeSimplifiedEvents(root.childNode.get(i),null, false);
					}
				}
			}
			else{
				for(int i=0;i<root.childEdge.size();i++)
					makeSimplifiedEvents(root.childNode.get(i), null, false);
			}
		}
	}

	private static boolean isNodeEvent(TreeNode root) {
		for(int i=0;i<root.childEdge.size();i++){
			if(root.childEdge.get(i).substring(0, 3).equals("ARG")){
				return true;
			}
		}
		return false;
	}*/

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

}
