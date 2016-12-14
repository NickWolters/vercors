// -*- tab-width:2 ; indent-tabs-mode:nil -*-
package vct.col.ast;

import java.util.*;

import static hre.lang.System.*;

/**
 * This AST node stores method invokations on objects.
 * A function call is seen as invoking a static method on a class. 
 * 
 * @author sccblom
 *
 */
public class MethodInvokation extends ExpressionNode {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  public final ASTNode object;
  public final String method;
  private Method definition;
  private ASTNode args[];
  public final ClassType dispatch;
  
  public MethodInvokation(ASTNode object,String method,ASTNode ... args){
    this.object=object;
    this.method=method;
    this.args=Arrays.copyOf(args,args.length);
    dispatch=null;
  }
  
  public MethodInvokation(ASTNode object,ClassType dispatch,String method,ASTNode ... args){
    this.object=object;
    this.method=method;
    this.args=Arrays.copyOf(args,args.length);
    this.dispatch=dispatch;
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
 

  public int getArity() { return args.length; }
    
  public ASTNode getArg(int i){ return args[i]; }

  /**
   * Collect the results of visiting the arguments.
   * @param visitor The visitor which must be accepted by the arguments.
   * @param template Any array of the correct type.
   * @return Array of results of visiting the arguments.
   */
  public <E> E[] map_args(ASTVisitor<E> visitor,E[] template) {
    int N=args.length;
    E res[]=Arrays.copyOf(template,N);
    for(int i=0;i<N;i++){
      args[i].accept(visitor);
      res[i]=visitor.getResult();
    }
    return res;
  }

  public ASTNode[] getArgs() {
    return Arrays.copyOf(args,args.length);
  }
  
  public void setDefinition(Method m){
    definition=m;
  }
  public Method getDefinition(){
    return definition;
  }

  /**
   * Check if this invokation is an instantiation.
   */
  public boolean isInstantiation() {
    if (definition==null){
      Abort("invokation of unknown method");
    }
    return definition.kind==Method.Kind.Constructor;
  }

}

