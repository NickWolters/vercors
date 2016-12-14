package vct.col.ast;

import java.util.Map;

public class ActionBlock extends ASTNode {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }
  
  public final ASTNode history;
  public final ASTNode fraction;
  public final ASTNode process;
  public final ASTNode action;
  public final ASTNode block;
  public final Map<String,ASTNode> map;
  
  public ActionBlock(ASTNode history,ASTNode fraction,ASTNode process,ASTNode action,Map<String,ASTNode> map, ASTNode block){
    this.history=history;
    this.fraction=fraction;
    this.process=process;
    this.action=action;
    this.map=map;
    this.block=block;
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
 

}
