package vct.silver;

import hre.ast.Origin;
import vct.logging.ExceptionMessage;
import vct.logging.MessageVisitor;
import vct.logging.TaskBegin;
import vct.logging.TaskEnd;
import vct.logging.TaskPhase;
import vct.logging.VerCorsError;
import vct.logging.VerificationResult;
import vct.util.Configuration;

public class ErrorDisplayVisitor implements MessageVisitor {

  @Override
  public void visit(ExceptionMessage e) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(TaskBegin begin) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(TaskEnd end) {
    long duration=(end.nanoTime()-end.begin.nanoTime())/1000000L;
    if(Configuration.progress.get() && duration>1L){
      System.err.printf("task %s took %d ms%n",end.begin.description,duration);
    }

    
  }

  @Override
  public void visit(TaskPhase phase) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(VerificationResult result) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(VerCorsError error) {
    System.err.printf("reporting %s error%n",error.code);
    error.main.report("error","%s:%s",error.code,error.sub);
    for(Origin o:error.aux){
      o.report("auxiliary","caused by");
    }
  }

}
