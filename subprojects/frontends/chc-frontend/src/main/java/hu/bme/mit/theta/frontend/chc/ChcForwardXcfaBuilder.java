package hu.bme.mit.theta.frontend.chc;

import hu.bme.mit.theta.chc.frontend.dsl.gen.CHCBaseVisitor;
import hu.bme.mit.theta.chc.frontend.dsl.gen.CHCParser;
import hu.bme.mit.theta.core.decl.Decls;
import hu.bme.mit.theta.core.decl.VarDecl;
import hu.bme.mit.theta.core.stmt.AssignStmt;
import hu.bme.mit.theta.core.stmt.HavocStmt;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.xcfa.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hu.bme.mit.theta.core.type.booltype.BoolExprs.Bool;
import static hu.bme.mit.theta.frontend.chc.ChcUtils.*;

public class ChcForwardXcfaBuilder extends CHCBaseVisitor<Object> implements ChcXcfaBuilder {
    private XcfaProcedure.Builder builder;
    private final XcfaLocation initLocation = XcfaLocation.create("Init");
    private final XcfaLocation errorLocation = XcfaLocation.create("Err");
    private final Map<String, UPred> locations = new HashMap<>();

    public ChcForwardXcfaBuilder() {
        locations.put(initLocation.getName(), new UPred(initLocation, new ArrayList<>()));
        locations.put(errorLocation.getName(), new UPred(initLocation, new ArrayList<>()));
    }

    @Override
    public XCFA.Builder buildXcfa(CHCParser parser) {
        XCFA.Builder xcfaBuilder = XCFA.builder();
        xcfaBuilder.setDynamic(true);
        XcfaProcess.Builder procBuilder = XcfaProcess.builder();
        builder = XcfaProcedure.builder();
        builder.setName("benchmark");
        builder.addLoc(initLocation);
        builder.setInitLoc(initLocation);
        builder.addLoc(errorLocation);
        builder.setErrorLoc(errorLocation);
        XcfaLocation finalLocation = XcfaLocation.create("Final");
        builder.addLoc(finalLocation);
        builder.setFinalLoc(finalLocation);
        builder.setRetType(Bool());

        visit(parser.benchmark());

        procBuilder.addProcedure(builder);
        procBuilder.setMainProcedure(builder);
        xcfaBuilder.addProcess(procBuilder);
        xcfaBuilder.setMainProcess(procBuilder);
        return xcfaBuilder;
    }

    @Override
    public Object visitFun_decl(CHCParser.Fun_declContext ctx) {
        String name = ctx.symbol().getText();
        if (ctx.symbol().quotedSymbol() != null) name = name.replaceAll("\\|", "");
        int i = 0;
        List<VarDecl<?>> vars = new ArrayList<>();
        for (CHCParser.SortContext sort : ctx.sort()) {
            String varName = name + "_" + i++;
            Type type = transformSort(sort);
            VarDecl<?> var = Decls.Var(varName, type);
            vars.add(var);
            builder.createVar(var, null);
            transformConst(Decls.Const(varName, type), true);
        }
        XcfaLocation location = XcfaLocation.create(name);
        locations.put(name, new UPred(location, vars));
        locations.put(ctx.symbol().getText(), new UPred(location, vars));
        builder.addLoc(location);
        return super.visitFun_decl(ctx);
    }

    @Override
    public Object visitChc_assert(CHCParser.Chc_assertContext ctx) {
        XcfaLocation from;
        XcfaLocation to;
        List<XcfaLabel> labels = new ArrayList<>();

        if (ctx.chc_tail() != null) {
            from = getTailFrom(ctx.chc_tail());
            to = getHeadTo(ctx.chc_head());
            Map<String, VarDecl<?>> vars = createVars(builder, ctx.var_decl());
            labels.addAll(getIncomingAssignments(ctx.chc_tail(), vars));
            labels.addAll(getTailConditionLabels(ctx.chc_tail(), vars));
            labels.addAll(getTargetAssignments(ctx.chc_head(), vars));
        } else {
            String locName;
            if (ctx.chc_head() != null) {
                locName = ctx.chc_head().u_pred_atom().u_predicate().getText();
            } else {
                locName = ctx.u_predicate().getText();
            }
            from = initLocation;
            to = locations.get(locName).location;
        }
        XcfaEdge edge = XcfaEdge.of(from, to, labels);
        builder.addEdge(edge);
        return super.visitChc_assert(ctx);
    }

    @Override
    public Object visitChc_query(CHCParser.Chc_queryContext ctx) {
        XcfaLocation from = getTailFrom(ctx.chc_tail());
        Map<String, VarDecl<?>> vars = createVars(builder, ctx.var_decl());
        List<XcfaLabel> labels = new ArrayList<>();
        labels.addAll(getIncomingAssignments(ctx.chc_tail(), vars));
        labels.addAll(getTailConditionLabels(ctx.chc_tail(), vars));
        XcfaEdge edge = XcfaEdge.of(from, errorLocation, labels);
        builder.addEdge(edge);
        return super.visitChc_query(ctx);
    }

    private List<XcfaLabel> getIncomingAssignments(CHCParser.Chc_tailContext tail, Map<String, VarDecl<?>> localVars) {
        List<XcfaLabel> labels = new ArrayList<>();
        UPred from = locations.get(getTailFrom(tail).getName());
        tail.u_pred_atom().forEach(u_pred -> {
            List<? extends VarDecl<?>> params = u_pred.symbol().stream().map(symbol -> localVars.get(symbol.getText())).toList();
            localVars.values().forEach(var -> { if (!params.contains(var)) labels.add(XcfaLabel.Stmt(HavocStmt.of(var))); });
            labels.addAll(getParamAssignments(params, from.vars));
        });
        return labels;
    }

    private List<XcfaLabel> getTargetAssignments(CHCParser.Chc_headContext head, Map<String, VarDecl<?>> localVars) {
        List<? extends VarDecl<?>> params = head.u_pred_atom().symbol().stream().map(symbol -> localVars.get(symbol.getText())).toList();
        UPred to = locations.get(getHeadTo(head).getName());
        return getParamAssignments(to.vars, params);
    }

    private XcfaLocation getTailFrom(CHCParser.Chc_tailContext tail) {
        XcfaLocation from;
        if (tail.u_pred_atom() != null && !tail.u_pred_atom().isEmpty()) {
            if (tail.u_pred_atom().size() != 1)
                throw new UnsupportedOperationException("Non-linear CHCs are not supported with forward transformation, try using the --chc-transformation BACKWARD flag.");
            from = locations.get(tail.u_pred_atom().get(0).u_predicate().getText()).location;
        } else {
            from = initLocation;
        }
        return from;
    }

    private XcfaLocation getHeadTo(CHCParser.Chc_headContext head) {
        return locations.get(head.u_pred_atom().u_predicate().getText()).location;
    }

    private List<XcfaLabel> getParamAssignments(List<? extends VarDecl<?>> lhs, List<? extends VarDecl<?>> rhs) {
        List<XcfaLabel> labels = new ArrayList<>();
        for (int i = 0; i < lhs.size(); ++i) {
            labels.add(XcfaLabel.Stmt(AssignStmt.create(lhs.get(i), rhs.get(i).getRef())));
        }
        return labels;
    }

    static class UPred {
        final XcfaLocation location;
        final List<VarDecl<?>> vars;

        UPred(XcfaLocation location, List<VarDecl<?>> vars) {
            this.location = location;
            this.vars = vars;
        }
    }
}
