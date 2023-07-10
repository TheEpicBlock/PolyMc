package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.*;
import org.apache.commons.lang3.NotImplementedException;

public record BinaryOp(StackEntry a, StackEntry b, Op op, Type type) implements StackEntry {
    public boolean canBeSimplified() {
        return (a.canBeSimplified() && b.canBeSimplified()) || (a.isConcrete() && b.isConcrete());
    }

    @Override
    public StackEntry simplify(VirtualMachine vm) throws MethodExecutor.VmException {
        var entryA = this.a;
        if (entryA.canBeSimplified()) entryA = entryA.simplify(vm);
        var entryB = this.b;
        if (entryB.canBeSimplified()) entryB = entryB.simplify(vm);

        if (entryA.isConcrete() && entryB.isConcrete()) {
            switch (this.type) {
                case INT -> {
                    int a = entryA.extractAs(Integer.class);
                    int b = entryB.extractAs(Integer.class);
                    int result = switch (this.op) {
                        case ADD  -> a+b;
                        case SUB  -> a-b;
                        case MUL  -> a*b;
                        case DIV  -> a/b;
                        case AND  -> a&b;
                        case OR   -> a|b;
                        case XOR  -> a^b;
                        case REM  -> a%b;
                        case USHR -> a>>>b;
                        case SHR  -> a>>b;
                        case SHL  -> a<<b;
                        default -> throw new UnsupportedOperationException("Can't do an "+op+" operation on "+type);
                    };
                    return new KnownInteger(result);
                }
                case LONG -> {
                    long a = entryA.extractAs(Long.class);
                    long b = entryB.extractAs(Long.class);
                    long result = switch (this.op) {
                        case ADD  -> a+b;
                        case SUB  -> a-b;
                        case MUL  -> a*b;
                        case DIV  -> a/b;
                        case AND  -> a&b;
                        case OR   -> a|b;
                        case XOR  -> a^b;
                        case REM  -> a%b;
                        case USHR -> a>>>b;
                        case SHR  -> a>>b;
                        case SHL  -> a<<b;
                        default -> throw new UnsupportedOperationException("Can't do an "+op+" operation on "+type);
                    };
                    return new KnownLong(result);
                }
                case FLOAT -> {
                    float a = entryA.extractAs(Float.class);
                    float b = entryB.extractAs(Float.class);
                    if (this.op == Op.CMPL || this.op == Op.CMPG) {
                        if (Float.isNaN(a) || Float.isNaN(b)) {
                            return new KnownInteger(this.op == Op.CMPG ? 1 : -1);
                        } else if (Math.abs(a) == Math.abs(b) && Math.abs(b) == 0.0f) { // negative 0 and positive 0 are considered equal
                            return new KnownInteger(0);
                        } else {
                            return new KnownInteger(Float.compare(a, b));
                        }
                    }
                    float result = switch (this.op) {
                        case ADD  -> a+b;
                        case SUB  -> a-b;
                        case MUL  -> a*b;
                        case DIV  -> a/b;
                        case REM  -> a%b;
                        default -> throw new UnsupportedOperationException("Can't do an "+op+" operation on "+type);
                    };
                    return new KnownFloat(result);
                }
                case DOUBLE -> {
                    double a = entryA.extractAs(Double.class);
                    double b = entryB.extractAs(Double.class);
                    if (this.op == Op.CMPL || this.op == Op.CMPG) {
                        if (Double.isNaN(a) || Double.isNaN(b)) {
                            return new KnownInteger(this.op == Op.CMPG ? 1 : -1);
                        } else if (Math.abs(a) == Math.abs(b) && Math.abs(b) == 0.0d) { // negative 0 and positive 0 are considered equal
                            return new KnownInteger(0);
                        } else {
                            return new KnownInteger(Double.compare(a, b));
                        }
                    }
                    double result = switch (this.op) {
                        case ADD  -> a+b;
                        case SUB  -> a-b;
                        case MUL  -> a*b;
                        case DIV  -> a/b;
                        case REM  -> a%b;
                        default -> throw new UnsupportedOperationException("Can't do an "+op+" operation on "+type);
                    };
                    return new KnownDouble(result);
                }
            }
        }

        return new BinaryOp(entryA, entryB, op, type);
    }

    @Override
    public <T> T extractAs(Class<T> type) {
        // TODO better error handling here
        throw new UnsupportedOperationException("");
    }

    @Override
    public JsonElement toJson() {
        throw new NotImplementedException();
    }

    public enum Op {
        ADD,
        SUB,
        MUL,
        DIV,
        AND,
        OR,
        XOR,
        REM,
        USHR,
        SHR,
        SHL,
        /**
         * Has the same semantics as fcmpl and dcmpl
         */
        CMPL,
        /**
         * Has the same semantics as fcmpg and dcmpg
         */
        CMPG,
    }

    public enum Type {
        INT,
        LONG,
        FLOAT,
        DOUBLE
    }
}