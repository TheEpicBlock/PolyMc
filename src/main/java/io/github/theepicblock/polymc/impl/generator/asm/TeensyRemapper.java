package io.github.theepicblock.polymc.impl.generator.asm;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;

/**
 * Remaps from a certain namespace into the runtime namespace
 */
public class TeensyRemapper {
    private final MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
    /**
     * The namespace of the input files
     */
    private final String sourceNamespace;

    public TeensyRemapper(String sourceNamespace) {
        this.sourceNamespace = sourceNamespace;
    }

    private @InternalName @Nullable String mapClassname(@InternalName @Nullable String input) {
        if (input == null) return null;
        return resolver.mapClassName(sourceNamespace, input.replace("/", ".")).replace(".", "/");
    }

    public ClassNode remapPls(InputStream input) throws IOException {
        var reader = new ClassReader(input);

        var asmVersion = Opcodes.ASM9;
        var classNode = new ClassNode(asmVersion);
        reader.accept(new ClassVisitor(asmVersion, classNode) {
            private @BinaryName String classNameBin;

            @Override
            public void visit(int version, int access, @InternalName String name, String signature, String superName, String[] interfaces) {
                var newClassName = mapClassname(name);
                this.classNameBin = newClassName.replace("/", ".");
                super.visit(version, access, mapClassname(name), signature, mapClassname(superName), interfaces);
            }

            @Override
            public void visitOuterClass(String owner, String name, String descriptor) {
                var newDesc = Mapping.remapDescriptor(s -> resolver.mapClassName(sourceNamespace, s), descriptor);
                super.visitOuterClass(mapClassname(owner), mapClassname(name), newDesc);
            }

            @Override
            public void visitInnerClass(String name, String outerName, String innerName, int access) {
                super.visitInnerClass(mapClassname(name), mapClassname(outerName), innerName, access);
            }

            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                var newFieldname = resolver.mapFieldName(sourceNamespace, this.classNameBin, name, descriptor);
                return super.visitField(access, newFieldname, mapClassname(descriptor), signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String methodName, String descriptor, String signature, String[] exceptions) {
                var newMethodName = resolver.mapMethodName(sourceNamespace, this.classNameBin, methodName, descriptor);
                var newDesc = Mapping.remapDescriptor(s -> resolver.mapClassName(sourceNamespace, s), descriptor);
                var methVisitor = classNode.visitMethod(access, newMethodName, newDesc, signature, exceptions);
                return new MethodVisitor(asmVersion, methVisitor) {
                    @Override
                    public void visitTypeInsn(int opcode, @InternalName String type) {
                        super.visitTypeInsn(opcode, mapClassname(type));
                    }

                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                        var newFieldname = resolver.mapFieldName(sourceNamespace, classNameBin, name, descriptor);
                        super.visitFieldInsn(opcode, owner, newFieldname, mapClassname(descriptor));
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        var newOwner = mapClassname(owner);
                        var newMethodName = resolver.mapMethodName(sourceNamespace, newOwner.replace("/", "."), methodName, descriptor);
                        var newDesc = Mapping.remapDescriptor(s -> resolver.mapClassName(sourceNamespace, s), descriptor);
                        super.visitMethodInsn(opcode, newOwner, newMethodName, newDesc, isInterface);
                    }

                    @Override
                    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                        // TODO
                        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
                    }

                    @Override
                    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
                        var newDesc = Mapping.remapDescriptor(s -> resolver.mapClassName(sourceNamespace, s), descriptor);
                        super.visitMultiANewArrayInsn(newDesc, numDimensions);
                    }

                    @Override
                    public void visitTryCatchBlock(Label start, Label end, Label handler, @Nullable String type) {
                        super.visitTryCatchBlock(start, end, handler, mapClassname(type));
                    }

                    @Override
                    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                        super.visitLocalVariable(name, mapClassname(descriptor), signature, start, end, index);
                    }
                };
            }

            @Override
            public void visitEnd() {
                classNode.visitEnd();
            }
        }, 0);
        reader.accept(classNode, 0);

        return classNode;
    }
}
