package vct.demo;

import hre.ast.FileOrigin;
/**
 * This class demonstrates how to use the context class.
 * 
 * @author sccblom
 *
 */
public class ContextDisplay {

  public static void main(String args[]){
    FileOrigin.add(args[0],true);
    int begin=Integer.parseInt(args[1]);
    int end=Integer.parseInt(args[2]);
    FileOrigin o=new FileOrigin(args[0],begin,1,end,1);
    System.out.printf("origin %s is%n",o);
    o.printContext(System.out,2,2);
  }
}
