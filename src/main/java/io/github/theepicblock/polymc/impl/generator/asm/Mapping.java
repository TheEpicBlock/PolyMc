package io.github.theepicblock.polymc.impl.generator.asm;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;

import net.fabricmc.loader.api.MappingResolver;
import net.fabricmc.mapping.tree.TinyMappingFactory;

public class Mapping {
    private HashMap<String, String> classNames = new HashMap<>();
    private HashMap<String, ClassDef> classInfos = new HashMap<>();

    public Mapping(BufferedReader reader, String inputNamespace, String outputNamespace) throws IOException {
        var tree = TinyMappingFactory.loadWithDetection(reader, true);
        for (var clazz : tree.getClasses()) {
            classNames.put(clazz.getName(inputNamespace), clazz.getName(outputNamespace));
            var fieldNames = new HashMap<String, String>();
            clazz.getFields().forEach(field -> fieldNames.put(field.getName(inputNamespace), field.getName(outputNamespace)));
            var methodNames = new HashMap<String, String>();
            clazz.getMethods().forEach(method -> methodNames.put(method.getName(inputNamespace), method.getName(outputNamespace)));

            classInfos.put(clazz.getName(inputNamespace), new ClassDef(fieldNames, methodNames));
        }
    }
    
    public static Mapping intermediaryToObfFromClasspath() {
        var url = MappingResolver.class.getClassLoader().getResource("mappings/mappings.tiny");
        try (var tiny = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return new Mapping(tiny, "intermediary", "official");
        } catch (IOException e) {
            // TODO better solution plsinput
            throw new RuntimeException(e);
        }
    }

    public String getClassname(String input) {
        return classNames.getOrDefault(input, input);
    }

    record ClassDef(HashMap<String, String> fieldNames, HashMap<String, String> methodNames) {
    }
}
