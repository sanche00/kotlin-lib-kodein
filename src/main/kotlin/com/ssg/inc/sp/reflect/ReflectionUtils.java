package com.ssg.inc.sp.reflect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;


public class ReflectionUtils {

    final static Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);

    public static Stream<Class> findAllClassesUsingClassLoader(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .flatMap(line ->getClass(line, packageName).stream());
    }

    public static Stream<Class> findAllClasses(String basePacakge) {
        return Arrays.stream(ClassLoader.getSystemClassLoader().getDefinedPackages())
                .filter(x->x.getName().startsWith(basePacakge))
                .flatMap(x->findAllClassesUsingClassLoader(x.getName()));
    }

    static Optional<Class> getClass(String className, String packageName) {
        try {
            return Optional.of(Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.'))));
        } catch (ClassNotFoundException e) {
            logger.warn("load class Error ".concat("className"), e);
            return Optional.empty();
        }
    }
}
