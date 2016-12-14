package vct.col.util;

import vct.col.ast.OperatorExpression;
import vct.col.ast.ProgramUnit;
import vct.col.ast.RecursiveVisitor;

public class WandScanner extends RecursiveVisitor<Object> {

	public WandScanner(ProgramUnit source) {
		super(source);
	}

	public void visit(OperatorExpression e){
		switch(e.getOperator()){
		case Wand:{
			Warning("found magic wand.");
			break;
		}
		default:
			super.visit(e);
		}
	}
}
