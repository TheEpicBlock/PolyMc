package nl.theepicblock.polymc.annotations;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("nl.theepicblock.polymc.annotations.Stub")
@SupportedOptions("PolyMcAnnotationProcessorSrcDir")
@SupportedSourceVersion(SourceVersion.RELEASE_16)
public class Processor extends AbstractProcessor {
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var stubClasses = new ArrayList<Element>();

        // Collect all *classes* with the @Stub annotation
        for (var element : roundEnv.getElementsAnnotatedWith(Stub.class)) {
            if (element.getKind().isClass()) {
                stubClasses.add(element);
            }
        }

        // Check if all the children of the stub class have an @Stub annotation too
        for (var stubClass : stubClasses) {
            for (var child : stubClass.getEnclosedElements()) {
                if (child.getKind() == ElementKind.METHOD && child.getAnnotation(Stub.class) == null) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Element doesn't declare an @Stub annotation", child);
                }
            }
        }


        for (var stubClass : stubClasses) {
            try {
                generateIntermediary(stubClass);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private void generateIntermediary(Element element) throws IOException {
        var stubInfo = element.getAnnotation(Stub.class);

        var intermediaryClassName = "net.minecraft."+stubInfo.intermediary();
        var methodRenameMap = new HashMap<String, String>();

        for (var child : element.getEnclosedElements()) {
            var stub = child.getAnnotation(Stub.class);
            if (stub == null) continue;
            if (child instanceof ExecutableElement method) {
                methodRenameMap.put(
                        createMethodDeclaration(method, method.getSimpleName().toString()),
                        createMethodDeclaration(method, stub.intermediary()));
            }
        }

        var sourceFile = getSourceFile((TypeElement)element);
        var generatedFile = processingEnv.getFiler().createSourceFile(intermediaryClassName, element);

        var sourceString = Files.readString(sourceFile);
        sourceString = sourceString.replace("class "+element.getSimpleName().toString(), "class "+stubInfo.intermediary());

        for (var methodRenameEntry : methodRenameMap.entrySet()) {
            if (!sourceString.contains(methodRenameEntry.getKey())) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Couldn't find method '"+methodRenameEntry.getKey()+"'. Keep in mind this annotation processor is super fragile", element);
                return;
            }

            sourceString = sourceString.replace(methodRenameEntry.getKey(), methodRenameEntry.getValue());
        }

        sourceString = sourceString.replaceAll("@Stub\\(.*?\\)", "");

        var writer = generatedFile.openWriter();
        writer.write(sourceString);
        writer.close();
    }

    private Path getSourceFile(TypeElement e) {
        var sourceDir = processingEnv.getOptions().get("PolyMcAnnotationProcessorSrcDir");
        var source = Path.of(sourceDir);
        return source.resolve(e.getQualifiedName().toString().replace(".", "/")+".java");
    }

    private static String createMethodDeclaration(ExecutableElement method, String methodName) {
        var builder = new StringBuilder();
        for (var modifier : method.getModifiers()) {
            builder.append(modifier);
            builder.append(" ");
        }
        builder.append(method.getReturnType().toString());
        builder.append(" ");
        builder.append(methodName);
        builder.append("(");
        builder.append(method.getParameters().stream().map(param -> param.asType()+" "+param.getSimpleName()).collect(Collectors.joining(", ")));
        builder.append(")");
        return builder.toString();
    }

    private static String getDescriptor(ExecutableElement method) {
        var stringBuilder = new StringBuilder();
        stringBuilder.append('(');
        for (var param : method.getParameters()) {
            appendDescriptor(param.asType(), stringBuilder);
        }
        stringBuilder.append(')');
        appendDescriptor(method.getReturnType(), stringBuilder);
        return stringBuilder.toString();
    }

    private static void appendDescriptor(final TypeMirror type, final StringBuilder stringBuilder) {
        var currentType = type;
        while (currentType.getKind() == TypeKind.ARRAY) {
            stringBuilder.append('[');
            currentType = ((ArrayType)currentType).getComponentType();
        }
        switch (type.getKind()) {
            case INT -> stringBuilder.append("I");
            case VOID -> stringBuilder.append("V");
            case BOOLEAN -> stringBuilder.append("Z");
            case BYTE -> stringBuilder.append("B");
            case CHAR -> stringBuilder.append("C");
            case SHORT -> stringBuilder.append("S");
            case DOUBLE -> stringBuilder.append("D");
            case FLOAT -> stringBuilder.append("F");
            case LONG -> stringBuilder.append("L");
            case DECLARED ->  {
                stringBuilder.append("L");
                stringBuilder.append(((DeclaredType)type).asElement().getSimpleName());
                stringBuilder.append(";");
            }
        }
    }
}
