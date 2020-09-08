package restdoc.remoting.common.body;

import restdoc.remoting.protocol.RemotingSerializable;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * ReportClientExportInterfacesBody
 */
public class ClientExposedInterfacesBody extends RemotingSerializable {

    private Map<String, ExposedInterface> exposedInterfaces;

    public Map<String, ExposedInterface> getExposedInterfaces() {
        return exposedInterfaces;
    }

    @Override
    public String toString() {
        return "ReportClientExportInterfacesBody{" +
                "exportInterfaces=" + exposedInterfaces +
                '}';
    }

    public void setExposedInterfaces(Map<String, ExposedInterface> exposedInterfaces) {
        this.exposedInterfaces = exposedInterfaces;
    }

    public static class ExposedInterface {
        private String name;
        private List<ExposedMethod> exposedMethods;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<ExposedMethod> getExposedMethods() {
            return exposedMethods;
        }

        public void setExposedMethods(List<ExposedMethod> exposedMethods) {
            this.exposedMethods = exposedMethods;
        }
    }

    public static class ExposedMethod {
        private final transient Method method;
        private final String paramDesc;
        private final String[] compatibleParamSignatures;
        private final Class<?>[] parameterClasses;
        private final Class<?> returnClass;
        private final Type[] returnTypes;
        private final String methodName;
        private final boolean generic;

        public ExposedMethod(Method method, String paramDesc, String[] compatibleParamSignatures,
                             Class<?>[] parameterClasses, Class<?> returnClass,
                             Type[] returnTypes, String methodName, boolean generic) {
            this.method = method;
            this.paramDesc = paramDesc;
            this.compatibleParamSignatures = compatibleParamSignatures;
            this.parameterClasses = parameterClasses;
            this.returnClass = returnClass;
            this.returnTypes = returnTypes;
            this.methodName = methodName;
            this.generic = generic;
        }

        public Method getMethod() {
            return method;
        }

        public String getParamDesc() {
            return paramDesc;
        }

        public String[] getCompatibleParamSignatures() {
            return compatibleParamSignatures;
        }

        public Class<?>[] getParameterClasses() {
            return parameterClasses;
        }

        public Class<?> getReturnClass() {
            return returnClass;
        }

        public Type[] getReturnTypes() {
            return returnTypes;
        }

        public String getMethodName() {
            return methodName;
        }

        public boolean isGeneric() {
            return generic;
        }
    }
}
