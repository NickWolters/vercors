package vct.col.rewrite;

import vct.col.ast.ClassType;
import vct.col.ast.ProgramUnit;
import vct.col.ast.Type;

import java.util.Map;

public class MultiSubstitution extends AbstractRewriter {

  Map<String,Type> type_map;
  
  public MultiSubstitution(ProgramUnit source,Map<String,Type> type_map) {
    super(source);
    this.type_map=type_map;
  }

  public void visit(ClassType t){
    String name[]=t.getNameFull();
    if (name.length==1){
      Type res=type_map.get(name[0]);
      if (res!=null) {
        result=res;
        return;
      }
    }
    super.visit(t);
  }
}
