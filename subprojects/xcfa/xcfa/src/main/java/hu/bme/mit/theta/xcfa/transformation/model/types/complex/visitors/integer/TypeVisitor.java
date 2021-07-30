package hu.bme.mit.theta.xcfa.transformation.model.types.complex.visitors.integer;

import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.xcfa.transformation.model.types.complex.CComplexType;
import hu.bme.mit.theta.xcfa.transformation.model.types.complex.integer.CInteger;

import static hu.bme.mit.theta.core.type.inttype.IntExprs.Int;

public class TypeVisitor extends CComplexType.CComplexTypeVisitor<Void, Type> {
	public static final TypeVisitor instance = new TypeVisitor();

	@Override
	public Type visit(CInteger type, Void param) {
		return Int();
	}
}
