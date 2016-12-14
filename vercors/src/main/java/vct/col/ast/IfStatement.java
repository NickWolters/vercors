// -*- tab-width:2 ; indent-tabs-mode:nil -*-
package vct.col.ast;

import hre.ast.MessageOrigin;

import java.util.*;

public class IfStatement extends ASTNode {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  public IfStatement(){
  }
  
  public IfStatement(ASTNode cond,ASTNode true_branch,ASTNode false_branch){
    addClause(cond,true_branch);
    if(false_branch!=null){
      addClause(else_guard,false_branch);
    }
  }
  
  

  public static final ASTNode else_guard=new ConstantExpression(true,new MessageOrigin("else guard"));

  private static class Case {
    public ASTNode guard;
    public ASTNode  statement;
  }
  
  private ArrayList<Case> cases=new ArrayList<Case>();
  
  public void addClause(ASTNode guard,ASTNode s){
    Case clause=new Case();
    clause.guard=guard;
    if (guard != else_guard) {
      guard.setParent(this);
    }
    clause.statement=s;
    s.setParent(this);
    cases.add(clause);
  }

  public int getCount(){ return cases.size(); }

  public ASTNode getGuard(int i){ return cases.get(i).guard; }
  
  public ASTNode getStatement(int i){ return cases.get(i).statement; }

  
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

