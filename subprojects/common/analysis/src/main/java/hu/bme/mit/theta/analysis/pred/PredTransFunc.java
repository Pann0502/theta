/*
 *  Copyright 2017 Budapest University of Technology and Economics
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hu.bme.mit.theta.analysis.pred;

import static com.google.common.base.Preconditions.checkNotNull;
import static hu.bme.mit.theta.core.type.booltype.BoolExprs.And;

import java.util.Collection;
import java.util.Collections;

import hu.bme.mit.theta.analysis.TransFunc;
import hu.bme.mit.theta.analysis.expr.ExprAction;
import hu.bme.mit.theta.analysis.pred.PredAbstractors.PredAbstractor;
import hu.bme.mit.theta.core.utils.BasicVarIndexing;

public final class PredTransFunc implements TransFunc<PredState, ExprAction, PredPrec> {

	private final PredAbstractor predAbstractor;

	private PredTransFunc(final PredAbstractor predAbstractor) {
		this.predAbstractor = checkNotNull(predAbstractor);
	}

	public static PredTransFunc create(final PredAbstractor predAbstractor) {
		return new PredTransFunc(predAbstractor);
	}

	@Override
	public Collection<? extends PredState> getSuccStates(final PredState state, final ExprAction action,
														 final PredPrec prec) {
		checkNotNull(state);
		checkNotNull(action);
		checkNotNull(prec);

		final Collection<PredState> succStates = predAbstractor.createStatesForExpr(
				And(state.toExpr(), action.toExpr()), BasicVarIndexing.all(0), prec, action.nextIndexing());
		return succStates.isEmpty() ? Collections.singleton(PredState.bottom()) : succStates;
	}

}
