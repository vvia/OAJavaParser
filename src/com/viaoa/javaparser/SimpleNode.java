package com.viaoa.javaparser;

/**
 **** NOTE !!!!!!!!!!!!!!!! this has been customized and should not be overwritten by the javacc code generator
 *
 */
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class SimpleNode implements Node {
	protected Node parent;
	protected Node[] children;
	protected int id;
	protected JavaParser parser;
	protected Token beginToken;
	protected Token endToken; // dont include this, is used as the first token for another node
	int modifiers;

	public final String NL = System.getProperty("line.separator");

	public SimpleNode(int i) {
		id = i;
		// System.out.println("New " + JavaParserTreeConstants.jjtNodeName[id]);
	}

	public SimpleNode(JavaParser p, int i) {
		this(i);
		parser = p;
	}

	public int getId() {
		return id;
	}

	public Token getBeginToken() {
		return beginToken;
	}

	public Token getEndToken() {
		return endToken;
	}

	public void jjtOpen() {
		/* test
		if ("ZZZ".equals(parser.token.image)) {
			int xx = 4;
			xx++;
		}
		*/
		//System.out.println("Open " + JavaParserTreeConstants.jjtNodeName[id] + ": " + parser.token.image + "  " + parser.token);
		if (parser != null) {
			this.beginToken = parser.token;
		}
	}

	public void jjtClose() {
		//System.out.println("Close " + JavaParserTreeConstants.jjtNodeName[id] + ": " + this.beginToken.next.image);
		if (parser != null) {
			this.endToken = parser.token;
		}
	}

	public void jjtSetParent(Node n) {
		parent = n;
	}

	public Node jjtGetParent() {
		return parent;
	}

	public void jjtAddChild(Node n, int i) {
		if (children == null) {
			children = new Node[i + 1];
		} else if (i >= children.length) {
			Node c[] = new Node[i + 1];
			System.arraycopy(children, 0, c, 0, children.length);
			children = c;
		}
		children[i] = n;
	}

	public Node jjtGetChild(int i) {
		return children[i];
	}

	public int jjtGetNumChildren() {
		return (children == null) ? 0 : children.length;
	}

	public String toString() {
		return JavaParserTreeConstants.jjtNodeName[id];
	}

	public SimpleNode getChild(int id) {
		if (this.id == id) {
			return this;
		}
		if (children == null) {
			return null;
		}
		for (int i = 0; i < children.length; i++) {
			SimpleNode n = (SimpleNode) children[i];
			if (n.id == id) {
				return n;
			}
		}
		return null;
	}

	public SimpleNode[] getChildren(int id) {
		LinkedList<SimpleNode> list = new LinkedList<SimpleNode>();
		for (int i = 0; children != null && i < children.length; i++) {
			SimpleNode n = (SimpleNode) children[i];
			if (n.id == id) {
				list.add((SimpleNode) children[i]);
			}
		}
		SimpleNode[] nodes = new SimpleNode[list.size()];
		list.toArray(nodes);
		return nodes;
	}

	/**
	 * Recursively searches this nodes children to find all with matching id. This does do a recursive node search.
	 *
	 * @param id see ParserConstants for list
	 * @see #getNodes(int)
	 */
	public SimpleNode[] getAllChildren(int id) {
		LinkedList<SimpleNode> list = new LinkedList<SimpleNode>();
		getAllChildren(list, id);
		SimpleNode[] nodes = new SimpleNode[list.size()];
		list.toArray(nodes);
		return nodes;
	}

	protected void getAllChildren(LinkedList<SimpleNode> list, int id) {
		for (int i = 0; children != null && i < children.length; i++) {
			SimpleNode n = (SimpleNode) children[i];
			if (n.id == id) {
				list.add((SimpleNode) children[i]);
			}
		}
		for (int i = 0; children != null && i < children.length; i++) {
			SimpleNode n = (SimpleNode) children[i];
			n.getAllChildren(list, id);
		}
	}

	/**
	 * Recursively searches this nodes children to find all with matching id[] structure, returning the bottom-most node.
	 *
	 * @param id see ParserConstants for list
	 * @see #getNodes(int)
	 */
	public SimpleNode[] getAllMathchingChildren(int[] idPath) {
		ArrayList<SimpleNode> list = new ArrayList<SimpleNode>(30);
		getAllMathchingChildren(list, idPath, 0);
		SimpleNode[] nodes = new SimpleNode[list.size()];
		list.toArray(nodes);
		return nodes;
	}

	protected void getAllMathchingChildren(ArrayList<SimpleNode> list, int[] idPath, int idPos) {
		for (int i = 0; children != null && i < children.length; i++) {
			SimpleNode n = (SimpleNode) children[i];
			if (n.id == idPath[idPos]) {
				if (idPos == idPath.length - 1) {
					list.add(n);
				} else {
					n.getAllMathchingChildren(list, idPath, idPos + 1);
				}
			} else {
				n.getAllMathchingChildren(list, idPath, idPos);
			}
			// could have another match under node
			if (idPos > 0) {
				n.getAllMathchingChildren(list, idPath, 0); // start at '0' to look or new "full" match
			}
		}
	}

	/**
	 * Used to parse Node tree and update an OAObjectDef.
	 *
	 * @param bSecondTime true if this is the "second" pass over the tree.
	 */
	/*qq
	public void modelUpdate(OAModelUpdate mu, boolean bSecondTime) {
	    for (int i=0; children != null && i < children.length; i++) {
	        children[i].modelUpdate(mu, bSecondTime);
	    }
	}
	**/
	public String toString(Token beginToken, Token endToken) {
		Token token = beginToken;
		StringBuffer sb = new StringBuffer(128);
		for (int i = 0; token != null; i++) {
			String s = "";
			Token sToken = token.specialToken;
			for (; sToken != null;) {
				s = sToken.image + s;
				sToken = sToken.specialToken;
			}
			if (token.image != null) {
				sb.append(s + token.image);
			}
			if (token == endToken) {
				break;
			}
			token = token.next;
		}
		return new String(sb);
	}

	/**
	 * removes any leading indentation, adds 4 spaces indentations after "}" + NL
	 */
	public String toFormattedString() {
		return toFormattedString(0);
	}

	public String toFormattedString(int indent) {
		Token token = beginToken;
		StringBuffer sb = new StringBuffer(192);

		boolean bol = true; // begin of line, skip all spaces and tabs
		boolean boc = true; // begin of code, skip all leading

		for (int j = 0; j < indent; j++) {
			sb.append(' ');
		}

		for (int i = 0; token != null; i++) {
			String s = "";

			if (token.kind == JavaParserConstants.RBRACE && indent > 0) {
				indent -= 4;
			}

			// "rewind" special tokens
			Token sToken = token.specialToken;
			for (; sToken != null;) {

				// Indent Comments correctly
				if (sToken.kind == JavaParserConstants.FORMAL_COMMENT || sToken.kind == JavaParserConstants.MULTI_LINE_COMMENT) {
					StringTokenizer st = new StringTokenizer(sToken.image, "\n\r", true);
					String sNew = "";
					boolean bFirst = true;
					int indent2 = indent;
					while (st.hasMoreTokens()) {
						String line = st.nextToken();
						if (!st.hasMoreTokens()) {
							indent2 -= 4;
						}
						int x = line.length();
						if (x == 1 && (line.equals("\r") || line.equals("\n"))) {
							continue;
						}
						/*
						for (int k=0; k<x; k++) {
						    char ch = line.charAt(k);
						    if (ch != ' ') {
						        if (k > indent2) line = line.substring(k-indent2);
						        else {
						            for (;k<indent2; k++) line = " " + line;
						        }
						        break;
						    }
						}
						*/
						if (sNew.length() > 0) {
							sNew += NL;
						}
						sNew += line;
						if (bFirst) {
							indent2 += 4;
						}
						bFirst = false;
					}
					sToken.image = sNew;
				}

				if (sToken.specialToken == null) {
					break;
				}

				sToken = sToken.specialToken;
			}

			for (; sToken != null; sToken = sToken.next) {
				if (sToken.image == null) {
					continue;
				}
				if (boc) {
					if (sToken.kind < 9) {
						continue;
					}
					boc = false;
				}
				if (!bol || (!sToken.image.equals(" ") && !sToken.image.equals("\t"))) {
					s += sToken.image;
				}
				if (sToken.kind == 3 || sToken.kind == JavaParserConstants.SINGLE_LINE_COMMENT) {
					for (int j = 0; j < indent; j++) {
						s += " ";
					}
					bol = true;
				}
			}

			if (token.image != null) {
				sb.append(s + token.image);
			}
			if (token == endToken) {
				break;
			}
			token = token.next;
			bol = false;
			boc = false;
			if (token.kind == JavaParserConstants.LBRACE) {
				indent += 4;
			}
		}
		return new String(sb);
	}
}
