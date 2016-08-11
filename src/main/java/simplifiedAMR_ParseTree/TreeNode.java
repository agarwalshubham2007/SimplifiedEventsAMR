package simplifiedAMR_ParseTree;

import java.util.ArrayList;

public class TreeNode {
	public String word;
	public String alias;
	public int frameNum;
	public int childrenCount;
	public ArrayList<String> childEdge;
	public ArrayList<TreeNode> childNode;
	
	public TreeNode() {
		super();
		this.frameNum = -1;
		this.childrenCount = 0;
		this.childEdge = new ArrayList<String>();
		this.childNode = new ArrayList<TreeNode>();
	}
	
	public TreeNode(String word) {
		super();
		this.word = word;
		this.frameNum = -1;
		this.childrenCount = 0;
		this.childEdge = new ArrayList<String>();
		this.childNode = new ArrayList<TreeNode>();
	}

	public TreeNode(String word, String alias, int frameNum) {
		super();
		this.word = word;
		this.alias = alias;
		this.frameNum = frameNum;
		this.childrenCount = 0;
		this.childEdge = new ArrayList<String>();
		this.childNode = new ArrayList<TreeNode>();
	}

	public TreeNode(String word, String alias) {
		super();
		this.word = word;
		this.alias = alias;
		this.frameNum = -1;
		this.childrenCount = 0;
		this.childEdge = new ArrayList<String>();
		this.childNode = new ArrayList<TreeNode>();
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public int getFrameNum() {
		return frameNum;
	}

	public void setFrameNum(int frameNum) {
		this.frameNum = frameNum;
	}

	public int getChildrenCount() {
		return childrenCount;
	}

	public void setChildrenCount(int childrenCount) {
		this.childrenCount = childrenCount;
	}

	public ArrayList<String> getChildEdge() {
		return childEdge;
	}

	public void setChildEdge(ArrayList<String> childEdge) {
		this.childEdge = childEdge;
	}

	public ArrayList<TreeNode> getChildNode() {
		return childNode;
	}

	public void setChildNode(ArrayList<TreeNode> childNode) {
		this.childNode = childNode;
	}
	
	
	
}
