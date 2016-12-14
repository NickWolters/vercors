package vct.col.ast;

import java.util.ArrayList;
import java.util.Iterator;

public class ASTList implements ASTSequence<ASTList> {

  private ArrayList<ASTNode> list=new ArrayList<ASTNode>();
  
  @Override
  public Iterator<ASTNode> iterator() {
    return list.iterator();
  }

  @Override
  public ASTList add(ASTNode item) {
    if (item!=null) list.add(item);
    return this;
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public ASTNode get(int i) {
    return list.get(i);
  }

}
