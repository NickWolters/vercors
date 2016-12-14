package vct.col.rewrite;

import java.util.Arrays;

import vct.col.ast.Contract;
import vct.col.ast.DeclarationStatement;
import vct.col.ast.Method;
import vct.col.ast.ProgramUnit;
import vct.col.ast.Type;
import vct.util.ClassName;

public class FilterClass extends AbstractRewriter {

  private String[] name;
  
  public FilterClass(ProgramUnit source,String name[]){
    super(source);
    this.name=Arrays.copyOf(name,name.length);
  }
  
  public void visit(Method m){
    if (ClassName.prefixOf(name,current_class().getFullName()) || m.kind==Method.Kind.Predicate) {
      // Copy classes that start with the given prefix and all predicates.
      // TODO: copy visible and needed predicates only. 
      super.visit(m);
    } else {
      // Take headers of all other classes.
      // TODO: limit to headers to a minimal set of used classes.
      String name=m.getName();
      DeclarationStatement args[]=rewrite(m.getArgs());
      Contract mc=m.getContract();
      Contract c=null;
      if (mc!=null){
        c=rewrite(mc);
      }
      Method.Kind kind=m.kind;
      Type rt=rewrite(m.getReturnType());
      result=create.method_kind( kind, rt, c, name, args, null);
    }
  }
}
