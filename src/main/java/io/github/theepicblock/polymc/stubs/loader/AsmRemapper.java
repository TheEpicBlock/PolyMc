package io.github.theepicblock.polymc.stubs.loader;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.BiFunction;

public class AsmRemapper extends ClassVisitor {
    private final String targetClassname;
    private final BiFunction<String, String, String> methodRemapper;

    public AsmRemapper(ClassVisitor classVisitor, String targetClassname, BiFunction<String, String, String> methodRemapper) {
        super(Opcodes.ASM9, classVisitor);
        this.targetClassname = targetClassname;
        this.methodRemapper = methodRemapper;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, this.targetClassname, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return super.visitMethod(access, methodRemapper.apply(name, descriptor), descriptor, signature, exceptions);
    }
}
