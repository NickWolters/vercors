// -*- tab-width:2 ; indent-tabs-mode:nil -*-
package vct.col.ast;

import java.util.*;

public class RecordType extends Type {

  @Override
  public <R,A> R accept_simple(ASTMapping1<R,A> map,A arg){
    return map.map(this,arg);
  }

  private String name[];
  private Type type[];
  public RecordType(String name[],Type type[]){
    this.name=Arrays.copyOf(name,name.length);
    this.type=Arrays.copyOf(type,type.length);
  }

  public int getFieldCount(){ return name.length; }
  
  public String getFieldName(int i){ return name[i]; }

  public Type getFieldType(int i){return type[i]; }
  
  
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
 
  @Override
  public boolean supertypeof(ProgramUnit context, Type t) {
    // TODO Auto-generated method stub
    return false;
  }
  @Override
  public <T> T accept_simple(TypeMapping<T> map){
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

