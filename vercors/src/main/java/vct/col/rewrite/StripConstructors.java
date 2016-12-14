package vct.col.rewrite;

import vct.col.ast.Method;
import vct.col.ast.ProgramUnit;

public class StripConstructors extends AbstractRewriter {

  public StripConstructors(ProgramUnit source) {
    super(source);
  }

  public void visit(Method m){
    if(m.kind==Method.Kind.Constructor){
      result=null;
    } else{
      super.visit(m);
    }
  }
}
