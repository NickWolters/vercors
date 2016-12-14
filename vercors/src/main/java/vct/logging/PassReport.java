package vct.logging;

import java.util.ArrayList;
import java.util.HashSet;
import hre.util.TestReport;
import hre.util.TestReport.Verdict;
import vct.col.ast.ProgramUnit;

public class PassReport {
  
  private ArrayList<Message> entries=new ArrayList<Message>();
  
  public void add(Message m){
    entries.add(m);
    if (m.isFatal()) fatal++;
    for(MessageVisitor v:visitors){
      m.accept(v);
    }
  }
 
  private ProgramUnit input;
  private ProgramUnit output;
  
  private int fatal=0;
  
  public PassReport(ProgramUnit in){
    input=in;
  }
  
  public PassReport(ProgramUnit in,TestReport report){
    input=in;
    output=in;
    if(report.getVerdict()!=Verdict.Pass){
      fatal=1;
    }
  }
  
  public ProgramUnit setOutput(ProgramUnit pu){
    return output=pu;
  }
  
  public ProgramUnit getOutput(){
    return output;
  }
  
  public ProgramUnit getInput(){
    return input;
  }
  
  public int getFatal(){
    return fatal;
  }
  
  public void fatal(String format,Object ... args){
    System.err.printf(format+"%n",args);
    fatal++;
  }
  
  private HashSet<MessageVisitor> visitors=new HashSet<MessageVisitor>();
  
  public void add(MessageVisitor visitor){
    visitors.add(visitor);
  }
  public void remove(MessageVisitor visitor){
    visitors.remove(visitor);
  }

  public void listFatals() {
    System.err.printf("fatal count is %d%n",fatal);
    for(Message m:entries){
      if (m.isFatal()){
        System.err.printf("fatal entry %s%n",m.getClass());
        System.err.printf("fatal entry %s%n",m);
      }
    }
  }
}
