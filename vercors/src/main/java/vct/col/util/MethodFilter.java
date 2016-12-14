package vct.col.util;

import vct.col.ast.ASTNode;
import vct.col.ast.Method;
import hre.util.Function;

public class MethodFilter implements Function<ASTNode,Method> {

  private Method.Kind kind=null;
  
  public MethodFilter(){}
  
  public MethodFilter(Method.Kind kind){
    this.kind=kind;
  }
  @Override
  public Method apply(ASTNode e) {
    if(e instanceof Method) {
      Method m=(Method)e;
      if (kind==null || m.kind==kind)
        return m ; 
      else 
        return null;
    } else {
      return null;
    }
  }

}
