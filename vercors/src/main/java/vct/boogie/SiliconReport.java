// -*- tab-width:2 ; indent-tabs-mode:nil -*-
package vct.boogie;

import hre.ast.Origin;
import hre.ast.TrackingTree;
import hre.io.Message;
import hre.io.ModuleShell;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static hre.lang.System.Progress;

/**
 * Create a test report for a Boogie run from the output file.
 * 
 * @author sccblom
 *
 */
public class SiliconReport extends hre.util.TestReport {

  private boolean finished=false;
  private int error_count=0;
  private int verified_count=0; 
  private int timeout_count=0;
  
	private static Document parseXmlFile(File file){
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			//parse using builder to get DOM representation of the XML file
			return db.parse(file);
			

		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		System.exit(1);
		return null;
	}

  //public BoogieReport(InputStream stream,String filename,TrackingTree tree) throws IOException {
  public SiliconReport(ModuleShell shell, File boogie_xml_file, TrackingTree tree) throws IOException {
    String line;
    for(;;){
      Message msg=shell.recv();
      if (msg.getFormat().equals("exit %d")) break;
      System.err.printf(msg.getFormat(),msg.getArgs());
      System.err.println();
      if (msg.getFormat().equals("stderr: %s") || msg.getFormat().equals("stdout: %s")){
        line=(String)msg.getArg(0);
      } else {
        continue;
      }
      if (line.matches(".*Verification failed with.*")){
        setVerdict(Verdict.Error);
      } else if (line.matches(".*Verification succesfull.*")){
        setVerdict(Verdict.Pass);
      }
    }
    Progress("parsing %s\n",boogie_xml_file);
    Document dom=parseXmlFile(boogie_xml_file);
    Progress("interpreting %s\n",boogie_xml_file);
    parseDocument(dom,tree);
    Progress("finished %s\n",boogie_xml_file);
  }
  
  private void parseDocument(Document dom,TrackingTree tree){
		//get the root elememt
		Element docEle = dom.getDocumentElement();
		
		//get a nodelist
		NodeList error_list = docEle.getElementsByTagName("error");
		for(int i = 0 ; i < error_list.getLength();i++) {	
		  Element error = (Element)error_list.item(i);
		  String id=error.getAttribute("id");
		  int line=Integer.parseInt(error.getAttribute("line"));
		  int column=Integer.parseInt(error.getAttribute("column"));
		  Origin o=tree.getOrigin(line,column);
		  System.err.printf("  %s at %s\n",id,o);
		  o.report("error", id);
		}
	}
  public boolean getFinished(){ return finished; }
  public int getVerified() { return verified_count; }
  public int getErrors() { return error_count; }
  public int getTimeouts() { return timeout_count; }
  
}

