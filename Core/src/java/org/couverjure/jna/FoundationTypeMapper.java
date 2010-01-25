package org.couverjure.jna;

import com.sun.jna.*;
import org.couverjure.core.*;

import java.util.HashMap;
import java.util.Map;

public class FoundationTypeMapper extends DefaultTypeMapper {
    private static FoundationTypeMapper singleton = new FoundationTypeMapper();

    private Architecture architecture = Architecture.X86_64;
    private FromNativeConverter idFromNativeConverter;
    private ToNativeConverter idToNativeConverter;
    private Map<Class, FromNativeConverter> fromConverters;
    private Map<Class, ToNativeConverter> toConverters;

    public static FoundationTypeMapper getTypeMapper() {
        return singleton;
    }

    public FoundationTypeMapper(Architecture arch) {
        this.architecture = arch;
        this.toConverters = new HashMap<Class, ToNativeConverter>();
        this.fromConverters = new HashMap<Class, FromNativeConverter>();
        switch (this.architecture) {
            case PPC:
            case I386:
                throw new UnsupportedOperationException("32-bit architectures not yet supported.");
            case X86_64:
                ToNativeConverter toConverter = new FPToConverter64();
                toConverters.put(FoundationPointer.class, toConverter);
                toConverters.put(Selector.class, toConverter);
                toConverters.put(Method.class, toConverter);
                toConverters.put(ClassID.class, toConverter);
                toConverters.put(RefCountedID.class, toConverter);
                toConverters.put(ID.class, toConverter);
                FromNativeConverter fpFromConverter = new FPFromConverter64();
                fromConverters.put(FoundationPointer.class, fpFromConverter);
                fromConverters.put(Selector.class, new SelectorFromConverter64());
                fromConverters.put(Method.class, new MethodFromConverter64());
                fromConverters.put(ClassID.class, new ClassIDFromConverter64());
                fromConverters.put(ID.class, new IDFromConverter64());
                break;
        }
    }

    public FoundationTypeMapper() {
        this(Architecture.X86_64);
    }

    class FPConverter64 {
        public Class nativeType() {
            return Long.TYPE;
        }
    }

    class FPFromConverter64 extends FPConverter64 implements FromNativeConverter {
        public Object fromNative(Object o, FromNativeContext fromNativeContext) {
            return new FoundationPointer((Long) o);
        }
    }

    class FPToConverter64 extends FPConverter64 implements ToNativeConverter {
        public Object toNative(Object o, ToNativeContext toNativeContext) {
            if (o != null) {
            return ((FoundationPointer) o).getAddress();
            } else {
                return 0;
            }
        }
    }

    class IDFromConverter64 extends FPFromConverter64 {
        public Object fromNative(Object o, FromNativeContext fromNativeContext) {
            return new RefCountedID((Long) o);
        }
    }

    class ClassIDFromConverter64 extends FPFromConverter64 {
        public Object fromNative(Object o, FromNativeContext fromNativeContext) {
            return new ClassID((Long) o);
        }
    }

    class SelectorFromConverter64 extends FPFromConverter64 {
        public Object fromNative(Object o, FromNativeContext fromNativeContext) {
            return new Selector((Long) o);
        }
    }

    class MethodFromConverter64 extends FPFromConverter64 {
        public Object fromNative(Object o, FromNativeContext fromNativeContext) {
            return new Method((Long) o);
        }
    }

    /*class IDToConverter64 extends FPToConverter64 {
        public Object toNative(Object o, ToNativeContext toNativeContext) {
            return ((RefCountedId64) o).getNativeId();
        }
    }*/

    @Override
    public FromNativeConverter getFromNativeConverter(Class javaType) {
        FromNativeConverter fc = fromConverters.get(javaType);
        if (fc == null) {
            return super.getFromNativeConverter(javaType);
        } else {
            return fc;
        }
    }

    @Override
    public ToNativeConverter getToNativeConverter(Class javaType) {
        ToNativeConverter tc = toConverters.get(javaType);
        if (tc == null) {
            return super.getToNativeConverter(javaType);
        } else {
            return tc;
        }
    }
}
