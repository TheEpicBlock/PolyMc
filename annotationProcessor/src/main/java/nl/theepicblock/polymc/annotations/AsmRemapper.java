package nl.theepicblock.polymc.annotations;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

public class AsmRemapper extends ClassVisitor {
    private final String targetClassname;
    private final Map<MethodRef, String> methodRemapMap;

    public AsmRemapper(ClassVisitor classVisitor, String targetClassname, Map<MethodRef, String> methodRemapMap) {
        super(Opcodes.ASM9, classVisitor);
        this.targetClassname = targetClassname;
        this.methodRemapMap = methodRemapMap;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, this.targetClassname, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return super.visitMethod(access, methodRemapMap.getOrDefault(new MethodRef(name, descriptor), name), descriptor, signature, exceptions);
    }
}