package fr.inria.astor.core.entities;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;

/**
 * ModificationPoint created from a Suspicious code. That means, the
 * ModificationPoint is a suspicious to have a bug.
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class SuspiciousModificationPoint extends ModificationPoint {

	protected SuspiciousCode suspicious;


	public SuspiciousModificationPoint() {
		super();
	}

	public SuspiciousModificationPoint(SuspiciousCode suspicious, CtElement rootElement, CtClass clonedClass,
			List<CtVariable> context) {
		super(rootElement, clonedClass, context);
		this.suspicious = suspicious;
	}

	public SuspiciousCode getSuspicious() {
		return suspicious;
	}



	public void setSuspicious(SuspiciousCode suspicious) {
		this.suspicious = suspicious;
	}

	public String toString() {
		return "MP=" + ctClass.getQualifiedName() + " line: " + suspicious.getLineNumber() +
				", featureInfo: " + suspicious.getFeatureInfo() +
				", pointed element: " + codeElement.getClass().getSimpleName() + "" + ", code element:" +  codeElement +
				", previous fixing score: " + getPrevious_fix_type();

	}

	@Override
	public ModificationPoint clone() {

		SuspiciousModificationPoint sp = new SuspiciousModificationPoint(suspicious, codeElement, ctClass,
				contextOfModificationPoint);
		sp.identified = this.identified;
		sp.generation = this.generation;
		sp.programVariant = this.programVariant;
		return sp;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SuspiciousModificationPoint other = (SuspiciousModificationPoint) obj;
		if (suspicious == null) {
			return other.suspicious == null;
		} else{
			if(suspicious.equals(other.suspicious) && codeElement.equals(other.codeElement)){
				return true;
			}
		}
		return false;
	}

}
