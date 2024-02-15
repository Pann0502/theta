/*
 *  Copyright 2024 Budapest University of Technology and Economics
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
package hu.bme.mit.theta.analysis.unit;

import static hu.bme.mit.theta.core.type.booltype.BoolExprs.True;

import hu.bme.mit.theta.analysis.expr.ExprState;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.booltype.BoolType;

public final class UnitState implements ExprState {

    private static final UnitState INSTANCE = new UnitState();

    private UnitState() {
    }

    public static UnitState getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isBottom() {
        return false;
    }

    @Override
    public Expr<BoolType> toExpr() {
        return True();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
