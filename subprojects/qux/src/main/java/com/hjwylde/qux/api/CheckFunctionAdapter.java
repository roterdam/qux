package com.hjwylde.qux.api;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.hjwylde.qux.tree.StmtNode;
import com.hjwylde.qux.util.Identifier;

/**
 * TODO: Documentation
 *
 * @author Henry J. Wylde
 */
public class CheckFunctionAdapter extends FunctionAdapter {

    private boolean visitedCode;
    private boolean visitedEnd;

    public CheckFunctionAdapter(FunctionVisitor next) {
        super(next);
    }

    public final boolean hasVisitedEnd() {
        return visitedEnd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitCode() {
        checkState(!visitedCode, "may only call visitCode() once");
        checkState(!visitedEnd, "must call visitCode() before visitEnd()");

        visitedCode = true;

        super.visitCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitEnd() {
        checkState(visitedCode, "must call visitCode() before visitEnd()");
        checkState(!visitedEnd, "may only call visitEnd() once");

        visitedEnd = true;

        super.visitEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitParameter(Identifier var) {
        checkState(!visitedCode, "must call visitParameter(String, Type) before visitCode()");
        checkState(!visitedEnd, "must call visitParameter(String, Type) before visitEnd()");
        checkNotNull(var, "var cannot be null");

        super.visitParameter(var);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitStmtAssign(StmtNode.Assign stmt) {
        checkState(visitedCode, "must call visitCode() before visitStmtAssign(StmtNode.Assign)");
        checkState(!visitedEnd, "must call visitStmtAssign(StmtNode.Assign) before visitEnd()");
        checkNotNull(stmt, "stmt cannot be null");

        super.visitStmtAssign(stmt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitStmtExpr(StmtNode.Expr stmt) {
        checkState(visitedCode, "must call visitCode() before visitStmtExpr(StmtNode.Expr)");
        checkState(!visitedEnd, "must call visitStmtExpr(StmtNode.Expr) before visitEnd()");
        checkNotNull(stmt, "stmt cannot be null");

        super.visitStmtExpr(stmt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitStmtFor(StmtNode.For stmt) {
        checkState(visitedCode, "must call visitCode() before visitStmtFor(StmtNode.For)");
        checkState(!visitedEnd, "must call visitStmtFor(StmtNode.For) before visitEnd()");
        checkNotNull(stmt, "stmt cannot be null");

        super.visitStmtFor(stmt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitStmtIf(StmtNode.If stmt) {
        checkState(visitedCode, "must call visitCode() before visitStmtIf(StmtNode.If)");
        checkState(!visitedEnd, "must call visitStmtIf(StmtNode.If) before visitEnd()");
        checkNotNull(stmt, "stmt cannot be null");

        super.visitStmtIf(stmt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitStmtPrint(StmtNode.Print stmt) {
        checkState(visitedCode, "must call visitCode() before visitStmtPrint(StmtNode.Print)");
        checkState(!visitedEnd, "must call visitStmtPrint(StmtNode.Print) before visitEnd()");
        checkNotNull(stmt, "stmt cannot be null");

        super.visitStmtPrint(stmt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitStmtReturn(StmtNode.Return stmt) {
        checkState(visitedCode, "must call visitCode() before visitStmtReturn(StmtNode.Return)");
        checkState(!visitedEnd, "must call visitStmtReturn(StmtNode.Return) before visitEnd()");
        checkNotNull(stmt, "stmt cannot be null");

        super.visitStmtReturn(stmt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitStmtWhile(StmtNode.While stmt) {
        checkState(visitedCode, "must call visitCode() before visitStmtWhile(StmtNode.While)");
        checkState(!visitedEnd, "must call visitStmtWhile(StmtNode.While) before visitEnd()");
        checkNotNull(stmt, "stmt cannot be null");

        super.visitStmtWhile(stmt);
    }
}
