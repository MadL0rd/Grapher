package calculator2.values.util.actions;

import calculator2.calculator.Element;
import calculator2.calculator.executors.Expression;
import calculator2.calculator.executors.Variable;
import calculator2.values.util.actions.functions.MultiFunc;

import java.util.Objects;
import java.util.Stack;

public class AbstractConst<T> implements Runnable{
    private Expression<T> expression;
    private Variable<T> var;
    public Stack<Element> stack;
    public AbstractConst(Variable<T> var){
        this.var = var;
    }

    public void setExpression(Expression<T> expression) {
        this.expression = expression;
        this.stack = null;
    }

    public AbstractConst<T> setVar(Variable<T> var) {
        this.var = var;
        return this;
    }

    public void run(){
        var.setValue(expression.calculate());
    }

    public Variable<T> getVar(){
        return var;
    }
    private T execute(Expression<T>[] a){
        return var.calculate();
    }
    public MultiFunc<T> getFunc(){
        return this::execute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractConst<?> that = (AbstractConst<?>) o;
        return var.equals(that.var);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var);
    }
}
