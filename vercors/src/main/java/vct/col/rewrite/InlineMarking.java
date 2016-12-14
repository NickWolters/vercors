package vct.col.rewrite;

import hre.ast.InlineOrigin;
import hre.ast.Origin;
import vct.col.ast.ASTNode;
import vct.col.ast.ProgramUnit;
import vct.col.ast.RecursiveVisitor;

public class InlineMarking extends RecursiveVisitor<Object> {

  private Origin location;
  
  public InlineMarking(ProgramUnit share,Origin location) {
    super(share);
    this.location=location;
  }
  
  @Override
  public void enter(ASTNode n){
    super.enter(n);
    Origin o=n.getOrigin();
    if (o!=null){
      n.clearOrigin();
      n.setOrigin(new InlineOrigin(location,o));
    }
  }

}
