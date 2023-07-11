package io.github.theepicblock.polymc.impl.generator.asm;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.function.Function;

public class Mapping {
    public static ClassDef EMPTY_CLASS = new ClassDef(new HashMap<>(), new HashMap<>());
    /** 
     *  A map from classname in the input namespace, to classname in the output namespace
     */
    private final HashMap<String, String> classI2O = new HashMap<>();
    /** 
     *  A map of {@link ClassDef}s by their name in the input namespace
     */
    private final HashMap<String, ClassDef> classInfoByI = new HashMap<>();
    /** 
     *  A map of {@link ClassDef}s by their name in the output namespace
     */
    private final HashMap<String, ClassDef> classInfoByO = new HashMap<>();

    public Mapping(BufferedReader reader, String inputNamespace, String outputNamespace) throws IOException {
        var tree = TinyMappingFactory.loadWithDetection(reader, true);
        for (var clazz : tree.getClasses()) {
            classI2O.put(clazz.getName(inputNamespace).replace("/", "."), clazz.getName(outputNamespace).replace("/", "."));
            var fieldNames = new HashMap<String, String>();
            clazz.getFields().forEach(field -> fieldNames.put(field.getName(inputNamespace), field.getName(outputNamespace)));
            var methodNames = new HashMap<String, MethodDef>();
            clazz.getMethods().forEach(method -> methodNames.put(method.getName(inputNamespace)+method.getDescriptor(inputNamespace), new MethodDef(method.getName(outputNamespace), method.getDescriptor(outputNamespace))));

            var def = new ClassDef(fieldNames, methodNames);
            classInfoByI.put(clazz.getName(inputNamespace), def);
            classInfoByO.put(clazz.getName(outputNamespace), def);
        }
    }
    
    public static Mapping runtimeToObfFromClasspath() {
        var url = MappingResolver.class.getClassLoader().getResource("mappings/mappings.tiny");
        try (var tiny = new BufferedReader(new InputStreamReader(url.openStream()))) {
            var runtime = FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace();
            return new Mapping(tiny, runtime, "official");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This operates on binary classnames where the package is seperated by dots, aka java.lang.String
     */
    @NotNull
    public String getClassname(String input) {
        return classI2O.getOrDefault(input, input);
    }

    @NotNull
    public String remapDescriptor(String desc) {
        return remapDescriptor(this::getClassname, desc);
    }

    public static String remapDescriptor(Function<String, String> mapper, String desc) {
        var output = new StringBuilder();

        int startJavaClass = -1;
        for (int i = 0; i < desc.length(); i++) {
            char c = desc.charAt(i);
            switch (c) {
                case 'L' -> {
                    startJavaClass = i;
                }
                case ';' -> {
                    var clazz = desc.substring(startJavaClass+1, i);
                    output.append("L");
                    output.append(mapper.apply(clazz.replace("/", ".")).replace(".", "/"));
                    output.append(";");
                    startJavaClass = -1;
                }
                default -> {
                    if (startJavaClass == -1) output.append(c);
                }
            }
        }

        return output.toString();
    }

    /**
     * This operates on classnames where the package is seperated by slashes, aka java/lang/String
     */
    @NotNull
    public ClassDef getClassByInputName(String classname) {
        return classInfoByI.getOrDefault(classname, EMPTY_CLASS);
    }

    /**
     * This operates on classnames where the package is seperated by slashes, aka java/lang/String
     */
    @NotNull
    public ClassDef getClassByOutputName(String classname) {
        return classInfoByO.getOrDefault(classname, EMPTY_CLASS);
    }

    public record ClassDef(HashMap<String, String> fieldNames, HashMap<String, MethodDef> methodNames) {
        public String getFieldName(String input) {
            return fieldNames.getOrDefault(input, input);
        }

        public MethodDef getMethodName(String name, String desc) {
            return methodNames.getOrDefault(name+desc, new MethodDef(name, desc));
        }
    }

    public record MethodDef(String name, String desc) {

    }
}
