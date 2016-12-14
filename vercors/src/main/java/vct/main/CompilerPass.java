package vct.main;

import vct.col.ast.ProgramUnit;
import vct.logging.PassAddVisitor;
import vct.logging.PassReport;

public abstract class CompilerPass {

  private String description;
  
  public String getDescripion(){
    return description;
  }
  
  public CompilerPass(String description){
    this.description=description;
  }
  
  
  public PassReport apply_pass(PassReport inrep, String ... args){
    ProgramUnit arg=inrep.getOutput();
    PassReport report=new PassReport(arg);
    report.add(new PassAddVisitor(inrep));
    report.setOutput(apply(report,arg,args));
    return report;
  }
  
  protected ProgramUnit apply(PassReport report,ProgramUnit arg, String ... args){
    return apply(arg,args);
  }
  
  protected ProgramUnit apply(ProgramUnit arg, String ... args){
    throw new hre.lang.HREError("Class %s failed to override both apply method.",getClass());
  }

}
