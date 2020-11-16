package com.viaoa.javaparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;

public class OAJavaParser {
	protected SimpleNode root;

	public void parse(String fname) throws Exception {
		parse(new File(fname));
	}

	public void parse(File file) throws Exception {
		if (file == null || !file.exists()) {
			return;
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);

			JavaParser parser = new JavaParser(fis);

			parser.CompilationUnit();

			root = (SimpleNode) parser.jjtree.rootNode();

			// System.out.println(root.toFormattedString(0));
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public SimpleNode getCompilationUnit() {
		if (root == null) {
			return null;
		}
		return root.getChild(JavaParserTreeConstants.JJTCOMPILATIONUNIT);
	}

	public SimpleNode getPackage() {
		SimpleNode node = getCompilationUnit();
		if (node == null) {
			return null;
		}
		return node.getChild(JavaParserTreeConstants.JJTPACKAGEDECLARATION);
	}

	public SimpleNode[] getImports() {
		SimpleNode node = getCompilationUnit();
		if (node == null) {
			return null;
		}
		return node.getChildren(JavaParserTreeConstants.JJTIMPORTDECLARATION);
	}

	public SimpleNode getClassOrInterface() {
		SimpleNode node = getCompilationUnit();
		if (node == null) {
			return null;
		}
		node = node.getChild(JavaParserTreeConstants.JJTTYPEDECLARATION);
		if (node == null) {
			return null;
		}
		node = node.getChild(JavaParserTreeConstants.JJTCLASSORINTERFACEDECLARATION);
		if (node == null) {
			return null;
		}
		node = node.getChild(JavaParserTreeConstants.JJTCLASSORINTERFACEBODY);
		return node;
	}

	public SimpleNode[] getClassOrInterfaceBodyDeclarations() {
		SimpleNode node = getClassOrInterface();
		if (node == null) {
			return null;
		}
		SimpleNode[] nodes = node.getChildren(JavaParserTreeConstants.JJTCLASSORINTERFACEBODYDECLARATION);
		return nodes;
	}

	/**
	 * This will return all Field nodes. see A_OUTLINE.txt in the javacc directory
	 */
	public SimpleNode[] getFields() {
		SimpleNode[] nodes = getClassOrInterfaceBodyDeclarations();
		if (nodes == null) {
			return null;
		}

		LinkedList<SimpleNode> list = new LinkedList<SimpleNode>();
		for (int i = 0; nodes != null && i < nodes.length; i++) {
			for (int j = 0; nodes[i].children != null && j < nodes[i].children.length; j++) {
				if (((SimpleNode) nodes[i].children[j]).getId() == JavaParserTreeConstants.JJTFIELDDECLARATION) {
					list.add(nodes[i]);
					break;
				}
			}
		}
		SimpleNode[] ns = new SimpleNode[list.size()];
		list.toArray(ns);

		return ns;
	}

	// 20101006 test
	public SimpleNode[] getFields_TestRandD() {
		if (root == null) {
			return null;
		}

		int[] idPath = new int[] {
				JavaParserTreeConstants.JJTFIELDDECLARATION,
				JavaParserTreeConstants.JJTTYPE
		};

		SimpleNode[] nodes = root.getAllMathchingChildren(idPath);

		// from node, it will either be a field of one of these:
		/*
		JavaParserTreeConstants.JJTPRIMITIVETYPE  - ex: boolean
		    or
		JavaParserTreeConstants.JJTREFERENCETYPE,         - ex: String, Order, Company 
		JavaParserTreeConstants.JJTCLASSORINTERFACETYPE
		*/

		return nodes;
	}

}
