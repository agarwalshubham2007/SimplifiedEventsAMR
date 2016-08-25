package simplifiedAMR_EventSeparation;

import simplifiedAMR_ParseTree.TreeNode;

public class SimplifiedEvents extends App{
	public static void makeSimplifiedEvents(TreeNode root, TreeNode eventNode, boolean checkArraylist) {
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

	public static boolean isNodeEvent(TreeNode root) {
		for(int i=0;i<root.childEdge.size();i++){
			if(root.childEdge.get(i).substring(0, 3).equals("ARG")){
				return true;
			}
		}
		return false;
	}
}
