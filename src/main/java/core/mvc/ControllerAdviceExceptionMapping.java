package core.mvc;

import com.google.common.collect.Maps;
import core.annotation.ControllerAdvice;
import core.mvc.tobe.ArgumentMatcher;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ControllerAdviceExceptionMapping implements ExceptionMapping {

    private static final Class<ControllerAdvice> CONTROLLER_ADVICE_ANNOTATION = ControllerAdvice.class;
    private static final Class<core.annotation.ExceptionHandler> EXCEPTION_HANDLER_ANNOTATION = core.annotation.ExceptionHandler.class;

    private ArgumentMatcher argumentMatcher;
    private Object[] basePackage;
    private Map<Class<? extends Throwable>, ExceptionHandler> handlers = Maps.newHashMap();

    public ControllerAdviceExceptionMapping(ArgumentMatcher argumentMatcher, Object... basePackage) {
        this.argumentMatcher = argumentMatcher;
        this.basePackage = basePackage;
    }

    @Override
    public void initialize() {
        Reflections reflections = new Reflections(basePackage);

        Set<Class<?>> controllerAdviceClasses = reflections.getTypesAnnotatedWith(CONTROLLER_ADVICE_ANNOTATION, true);
        for (Class<?> controllerAdviceClass : controllerAdviceClasses) {
            registerExceptionHandler(controllerAdviceClass);
        }
    }

    @Override
    public ExceptionHandler getHandler(Class<? extends Exception> exceptionClass) {
        return handlers.get(exceptionClass);
    }

    private void registerExceptionHandler(Class<?> controllerAdviceClass) {
        List<Method> exceptionHandlerMethods = getExceptionHandlerMethods(controllerAdviceClass);
        for (Method exceptionHandlerMethod : exceptionHandlerMethods) {
            Class<? extends Throwable> exceptionClass = exceptionHandlerMethod.getAnnotation(EXCEPTION_HANDLER_ANNOTATION).exception();

            handlers.put(exceptionClass, new DefaultExceptionHandler(exceptionHandlerMethod, argumentMatcher));
        }
    }

    private List<Method> getExceptionHandlerMethods(Class<?> controllerAdviceClass) {
        return Arrays.stream(controllerAdviceClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(EXCEPTION_HANDLER_ANNOTATION))
                .collect(Collectors.toList());
    }

}
