// -*- tab-width:2 ; indent-tabs-mode:nil -*-
package vct.col.ast;

import java.util.*;


public class BlockStatement extends ASTNode implements ASTSequence<BlockStatement> {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  private ArrayList<ASTNode> block=new ArrayList<ASTNode>();
  
  public void add_statement(ASTNode s){
    add(s);
  }
  
  public int getLength(){ return block.size(); }
  
  public ASTNode getStatement(int i){ return block.get(i); }
  
  public ASTNode[] getStatements(){
    return block.toArray(new ASTNode[0]);
  }
  public boolean isEmpty(){
    return block.isEmpty();
  }

  @Override
  public Iterator<ASTNode> iterator() {
    return block.iterator();
  }

  public void chain(ASTNode item){
    if (item instanceof BlockStatement){
      for(ASTNode n:((BlockStatement)item).block){
        n.clearParent();
        add(n);
      }
    } else {
      add(item);
    }
  }
  
  @Override
  public BlockStatement add(ASTNode item) {
    if (item!=null) {
      // this requires major rewriting elsewhere!
      //if (item instanceof ExpressionNode && !(item instanceof MethodInvokation)){
      //  hre.System.Failure("expressions must be wrapped in a Expression special to make them statements");
      //}
      block.add(item);
      item.setParent(this);
    }
    return this;
  }
  
  public BlockStatement append(ASTNode item) {
    if (item!=null) {
      if (item instanceof BlockStatement){
        for(ASTNode n:(BlockStatement)item){
          n.clearParent();
          this.add(n);
        }
      } else {
        this.add(item);
      }
    }
    return this;
  }

  @Override
  public int size() {
    return block.size();
  }

  @Override
  public ASTNode get(int i) {
    return block.get(i);
  }

  
  
  @Override
  public <T> void accept_simple(ASTVisitor<T> visitor){
    try {
      visitor.visit(this);
    } catch (Throwable t){
      if (thrown.get()!=t){
        System.err.printf("Triggered by %s:%n",getOrigin());
        thrown.set(t);
    }
      throw t;
    }
  }
  
  @Override
  public <T> T accept_simple(ASTMapping<T> map){
    try {
      return map.map(this);
    } catch (Throwable t){
      if (thrown.get()!=t){
        System.err.printf("Triggered by %s:%n",getOrigin());
        thrown.set(t);
     }
      throw t;
    }
  }
 
 

	public void prepend(ASTNode item) {
		block.add(0,item);
	  item.setParent(this);
  }

}

